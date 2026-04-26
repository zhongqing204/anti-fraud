import axios from "axios";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 30000  // 后台接口超时时间
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    // 如果不是FormData类型，才设置JSON Content-Type
    if (!(config.data instanceof FormData)) {
        config.headers['Content-Type'] = 'application/json;charset=utf-8';
    }
    // 根据当前路径判断使用哪个存储key，避免管理员端和用户端数据冲突
    let userStr = null;
    const currentPath = window.location.pathname;
    if (currentPath.startsWith('/manager')) {
        // 管理员端使用 xm-admin
        userStr = localStorage.getItem("xm-admin");
    } else {
        // 用户端使用 xm-user
        userStr = localStorage.getItem("xm-user");
    }
    
    let user = {};
    try {
        user = JSON.parse(userStr);
    } catch (e) {
        console.error('解析用户信息失败:', e);
        user = {};
    }
    config.headers['token'] = user?.token || ''
    return config
}, error => {
    console.error('请求拦截器错误:', error);
    ElMessage.error('请求发送失败：' + error.message);
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 如果是返回的文件
        if (response.config.responseType === 'blob') {
            return res
        }
        // 当权限验证不通过的时候给出提示
        if (res.code === '401') {
            ElMessage.warning(res.msg || '登录已过期，请重新登录');
            // 根据当前路径清除对应的存储
            const currentPath = window.location.pathname;
            if (currentPath.startsWith('/manager')) {
                localStorage.removeItem('xm-admin');
            } else {
                localStorage.removeItem('xm-user');
            }
            setTimeout(() => {
                router.push('/login');
            }, 1500);
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            try {
                res = res ? JSON.parse(res) : res;
            } catch (e) {
                console.error('解析响应数据失败:', e);
            }
        }
        return res;
    },
    error => {
        console.error('请求错误:', error);

        let errorMsg = '网络异常，请稍后重试';
        
        if (error.response) {
            const status = error.response.status;
            switch (status) {
                case 400:
                    errorMsg = '请求参数错误，请检查输入';
                    break;
                case 401:
                    errorMsg = '未授权，请重新登录';
                    // 根据当前路径清除对应的存储
                    const currentPath = window.location.pathname;
                    if (currentPath.startsWith('/manager')) {
                        localStorage.removeItem('xm-admin');
                    } else {
                        localStorage.removeItem('xm-user');
                    }
                    setTimeout(() => {
                        router.push('/login');
                    }, 1500);
                    break;
                case 403:
                    errorMsg = '拒绝访问，权限不足';
                    break;
                case 404:
                    errorMsg = '请求的资源不存在';
                    break;
                case 500:
                    errorMsg = '服务器内部错误，请联系管理员';
                    break;
                default:
                    errorMsg = `请求失败 (状态码：${status})`;
            }
            
            if (error.response.data && error.response.data.msg) {
                errorMsg = error.response.data.msg;
            }
        } else if (error.code === 'ECONNREFUSED') {
            errorMsg = '后端服务未启动或连接被拒绝，请检查服务器是否运行正常';
        } else if (error.code === 'ERR_NETWORK') {
            errorMsg = '网络错误，可能是跨域问题或服务未启动';
        } else if (error.message) {
            errorMsg = '网络异常：' + error.message;
        }
        
        ElMessage.error(errorMsg);
        return Promise.reject(error)
    }
)

export default request
