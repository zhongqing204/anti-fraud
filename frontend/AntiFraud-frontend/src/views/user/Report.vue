<template>
  <div style="width: 80%; margin: 20px auto">
    <!-- 页面标题 -->
    <div style="font-size: 24px; font-weight: bold; margin-bottom: 20px; color: #303133;">
      <el-icon style="margin-right: 8px; vertical-align: middle;"><Warning /></el-icon>
      反诈举报中心
    </div>

    <!-- 举报指南卡片 -->
    <div class="card" style="padding: 20px; margin-bottom: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white;">
      <div style="font-size: 18px; font-weight: bold; margin-bottom: 10px;">
        <el-icon><InfoFilled /></el-icon> 举报须知
      </div>
      <div style="line-height: 1.8; font-size: 14px;">
        <div>• 请如实填写举报内容，提供详细信息有助于我们快速处理</div>
        <div>• 可上传相关证据图片（截图、聊天记录等），最多9张</div>
        <div>• 我们将在3-7个工作日内完成核查并反馈处理结果</div>
        <div>• 严禁恶意举报，违者将承担相应法律责任</div>
      </div>
    </div>

    <!-- 举报表单卡片 -->
    <div class="card" style="padding: 30px; margin-bottom: 20px;">
      <div style="font-size: 18px; font-weight: bold; margin-bottom: 20px; color: #303133;">
        <el-icon style="margin-right: 5px;"><Edit /></el-icon>
        填写举报信息
      </div>
      
      <el-form label-width="100px" label-position="top">
        <!-- 举报类型 -->
        <el-form-item label="举报类型" required>
          <el-select v-model="data.category" placeholder="请选择诈骗类型" style="width: 100%;">
            <el-option label="网络诈骗" value="网络诈骗">
              <span style="float: left">网络诈骗</span>
              <span style="float: right; color: #8492a6; font-size: 12px">钓鱼网站、虚假投资等</span>
            </el-option>
            <el-option label="电话诈骗" value="电话诈骗">
              <span style="float: left">电话诈骗</span>
              <span style="float: right; color: #8492a6; font-size: 12px">冒充公检法、客服等</span>
            </el-option>
            <el-option label="短信诈骗" value="短信诈骗">
              <span style="float: left">短信诈骗</span>
              <span style="float: right; color: #8492a6; font-size: 12px">中奖信息、虚假链接等</span>
            </el-option>
            <el-option label="邮件诈骗" value="邮件诈骗">
              <span style="float: left">邮件诈骗</span>
              <span style="float: right; color: #8492a6; font-size: 12px">钓鱼邮件、病毒附件等</span>
            </el-option>
            <el-option label="其他" value="其他">
              <span style="float: left">其他类型</span>
              <span style="float: right; color: #8492a6; font-size: 12px">不属于以上分类</span>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 举报详情 -->
        <el-form-item label="详细描述" required>
          <el-input 
            type="textarea" 
            :rows="8" 
            v-model="data.content" 
            placeholder="请详细描述您遇到的诈骗情况，包括：&#10;1. 诈骗发生的时间和地点&#10;2. 诈骗分子的联系方式（电话、微信、QQ等）&#10;3. 诈骗手段和过程&#10;4. 涉及的金额（如有）&#10;5. 其他重要信息"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <!-- 证据上传 -->
        <el-form-item label="上传证据">
          <div style="border: 2px dashed #dcdfe6; border-radius: 8px; padding: 20px; text-align: center; background: #fafafa;">
            <el-upload
                :action="baseUrl + '/file/upload'"
                :on-success="handleFileUpload"
                :on-remove="handleFileRemove"
                :file-list="data.fileList"
                multiple
                :limit="9"
                accept=".jpg,.jpeg,.png,.gif,.webp,.bmp"
                list-type="picture-card"
                :on-exceed="handleExceed"
            >
              <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%;">
                <el-icon :size="40" color="#8c939d"><Plus /></el-icon>
                <div style="margin-top: 10px; color: #8c939d; font-size: 14px;">点击或拖拽上传图片</div>
                <div style="margin-top: 5px; color: #c0c4cc; font-size: 12px;">支持 JPG、PNG、GIF 格式，最多9张</div>
              </div>
            </el-upload>
          </div>
          <div style="margin-top: 10px; color: #909399; font-size: 12px;">
            <el-icon><InfoFilled /></el-icon>
            建议上传：聊天记录截图、转账记录、诈骗网站截图、通话记录等证据
          </div>
        </el-form-item>

        <!-- 联系方式（可选） -->
        <el-form-item label="联系方式（选填）">
          <el-input 
            v-model="data.contact" 
            placeholder="请输入您的手机号或邮箱，方便我们向您反馈处理结果"
            maxlength="50"
          />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            我们将严格保护您的个人信息安全
          </div>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button 
            type="primary" 
            @click="submit" 
            :loading="data.submitting"
            size="large"
            style="width: 100%;"
          >
            <el-icon style="margin-right: 5px;"><Promotion /></el-icon>
            提交举报
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 处理流程说明卡片 -->
    <div class="card" style="padding: 25px; margin-bottom: 20px;">
      <div style="font-size: 18px; font-weight: bold; margin-bottom: 20px; color: #303133;">
        <el-icon style="margin-right: 5px;"><Clock /></el-icon>
        处理流程
      </div>
      <el-steps :active="0" finish-status="success" simple>
        <el-step title="提交举报" description="填写并提交举报信息"></el-step>
        <el-step title="审核受理" description="工作人员将在1-2个工作日内审核"></el-step>
        <el-step title="调查处理" description="展开调查并进行处理"></el-step>
        <el-step title="反馈结果" description="向举报人反馈处理结果"></el-step>
      </el-steps>
      <div style="margin-top: 15px; padding: 15px; background: #f4f4f5; border-radius: 4px; color: #606266; font-size: 13px;">
        <el-icon><InfoFilled /></el-icon>
        您可以在"我的举报"中随时查看举报处理进度
      </div>
    </div>

    <!-- 常见诈骗类型说明卡片 -->
    <div class="card" style="padding: 25px;">
      <div style="font-size: 18px; font-weight: bold; margin-bottom: 20px; color: #303133;">
        <el-icon style="margin-right: 5px;"><QuestionFilled /></el-icon>
        常见诈骗类型说明
      </div>
      <el-collapse accordion>
        <el-collapse-item name="1">
          <template #title>
            <div style="font-weight: bold; color: #409EFF;">
              <el-icon><Monitor /></el-icon>
              网络诈骗
            </div>
          </template>
          <div style="line-height: 1.8; color: #606266;">
            <strong>典型场景：</strong><br/>
            • 虚假投资理财平台<br/>
            • 钓鱼网站窃取账号密码<br/>
            • 网络购物诈骗<br/>
            • 刷单返利诈骗<br/>
            • 网络游戏虚拟物品交易诈骗<br/><br/>
            <strong>防范要点：</strong>不轻信高收益投资，不点击不明链接，选择正规交易平台
          </div>
        </el-collapse-item>

        <el-collapse-item name="2">
          <template #title>
            <div style="font-weight: bold; color: #67C23A;">
              <el-icon><Phone /></el-icon>
              电话诈骗
            </div>
          </template>
          <div style="line-height: 1.8; color: #606266;">
            <strong>典型场景：</strong><br/>
            • 冒充公检法人员<br/>
            • 冒充银行或客服<br/>
            • "猜猜我是谁"诈骗<br/>
            • 虚假中奖通知<br/>
            • 绑架勒索电话<br/><br/>
            <strong>防范要点：</strong>核实对方身份，不透露验证码，遇到可疑情况及时报警
          </div>
        </el-collapse-item>

        <el-collapse-item name="3">
          <template #title>
            <div style="font-weight: bold; color: #E6A23C;">
              <el-icon><Message /></el-icon>
              短信诈骗
            </div>
          </template>
          <div style="line-height: 1.8; color: #606266;">
            <strong>典型场景：</strong><br/>
            • 假冒银行发送钓鱼链接<br/>
            • 虚假ETC认证短信<br/>
            • 快递理赔诈骗<br/>
            • 子女求助短信<br/>
            • 积分兑换诈骗<br/><br/>
            <strong>防范要点：</strong>不点击短信中的链接，通过官方渠道核实信息
          </div>
        </el-collapse-item>

        <el-collapse-item name="4">
          <template #title>
            <div style="font-weight: bold; color: #F56C6C;">
              <el-icon><ChatDotRound /></el-icon>
              邮件诈骗
            </div>
          </template>
          <div style="line-height: 1.8; color: #606266;">
            <strong>典型场景：</strong><br/>
            • 钓鱼邮件窃取账号<br/>
            • 携带病毒的附件<br/>
            • 假冒公司高管要求转账<br/>
            • 虚假发票或合同<br/>
            • 海外遗产诈骗<br/><br/>
            <strong>防范要点：</strong>谨慎打开陌生邮件附件，核实发件人身份
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 紧急提示卡片 -->
    <div class="card" style="padding: 20px; margin-top: 20px; background: #fef0f0; border-left: 4px solid #f56c6c;">
      <div style="display: flex; align-items: start;">
        <el-icon :size="24" color="#f56c6c" style="margin-right: 10px; margin-top: 2px;"><WarningFilled /></el-icon>
        <div>
          <div style="font-weight: bold; color: #f56c6c; margin-bottom: 8px;">紧急情况提示</div>
          <div style="color: #606266; line-height: 1.6; font-size: 14px;">
            如果您正在遭受诈骗或已经造成财产损失，请立即：<br/>
            1️⃣ 拨打 <strong style="color: #f56c6c; font-size: 16px;">110</strong> 报警电话<br/>
            2️⃣ 拨打 <strong style="color: #f56c6c; font-size: 16px;">96110</strong> 反电信网络诈骗专用号码<br/>
            3️⃣ 联系银行冻结相关账户<br/>
            4️⃣ 保存所有证据（聊天记录、转账凭证等）
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import {
  Warning,
  InfoFilled,
  Edit,
  Plus,
  Promotion,
  Clock,
  QuestionFilled,
  Monitor,
  Phone,
  Message,
  ChatDotRound,
  WarningFilled
} from "@element-plus/icons-vue";

const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  content: null,
  category: null,
  contact: null,
  fileList: [],
  fileUrls: [],
  submitting: false
})

// 提交举报
const submit = () => {
  // 验证举报类型
  if (!data.category) {
    ElMessage.warning('请选择举报类型')
    return
  }
  
  // 验证举报内容
  if (!data.content || !data.content.trim()) {
    ElMessage.warning('请输入举报内容')
    return
  }
  
  // 验证内容长度
  if (data.content.length < 20) {
    ElMessage.warning('举报内容至少需要20个字符，请详细描述情况')
    return
  }

  // 修复：使用正确的 localStorage key 'xm-user'
  const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
  
  // 检查用户是否登录
  if (!user.id) {
    ElMessage.warning('请先登录')
    return
  }

  data.submitting = true
  
  // 提交举报数据，包含 userName 字段
  request.post('/report/add', {
    userId: user.id,
    userName: user.name,
    category: data.category,
    content: data.content,
    files: data.fileUrls.join(','),
    status: '待处理',
    priority: '一般'
  }).then(res => {
    data.submitting = false
    if (res.code === '200') {
      ElMessage.success('举报提交成功！我们会尽快处理，感谢您的参与')
      // 重置表单
      data.content = null
      data.category = null
      data.contact = null
      data.fileList = []
      data.fileUrls = []
    } else {
      ElMessage.error(res.msg)
    }
  }).catch(err => {
    data.submitting = false
    ElMessage.error('提交失败，请稍后重试')
    console.error(err)
  })
}

// 文件上传成功回调
const handleFileUpload = (response, file, fileList) => {
  if (response.code === '200') {
    data.fileUrls.push(response.data)
    data.fileList = fileList
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

// 文件移除回调
const handleFileRemove = (file, fileList) => {
  // 找到被移除文件的索引
  const index = data.fileList.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    data.fileUrls.splice(index, 1)
  }
  data.fileList = fileList
}

// 超出文件数量限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传9张图片')
}
</script>
