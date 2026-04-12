<template>
  <div>
    <!-- 搜索栏 -->
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.title" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入活动名称查询"></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

     <!-- 操作按钮 -->
    <div class="card" style="margin-bottom: 5px">
      <el-button type="primary" plain @click="handleAdd">新增</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="活动名称" width="200" show-overflow-tooltip align="center" />
        <el-table-column prop="cover" label="活动封面" align="center">
          <template v-slot="scope">
            <div style="display: flex; justify-content: center;">
              <el-image style="width: 50px; height: 50px; border-radius: 5px; display: block" v-if="scope.row.cover"
                      :src="baseUrl + scope.row.cover" :preview-src-list="[baseUrl + scope.row.cover]" preview-teleported></el-image>
            </div>          
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="反诈分类" align="center" />
        <el-table-column prop="content" label="查看内容" align="center">
          <template v-slot="scope">
            <el-button type="primary" @click="viewInit(scope.row.content)">查看内容</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" align="center" />
        <el-table-column prop="status" label="活动状态" align="center">
          <template v-slot="scope">
            <el-tag type="success" v-if="scope.row.status === '进行中'">{{ scope.row.status }}</el-tag>
            <el-tag type="danger" v-if="scope.row.status === '已结束'">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template v-slot="scope">
            <el-button type="warning" plain @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" plain @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
     <!-- 分页组件 -->
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

     <!-- 表单对话框 -->
    <el-dialog title="活动信息" v-model="data.formVisible" width="90%" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="80px" style="padding: 20px">
        <el-form-item prop="title" label="活动标题">
          <el-input v-model="data.form.title" placeholder="请输入帖子标题"></el-input>
        </el-form-item>
        <el-form-item prop="cover" label="活动封面">
          <el-upload
              :action="baseUrl + '/files/upload'"
              :on-success="handleImgUpload"
              list-type="picture"
          >
            <el-button type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item prop="categoryId" label="反诈分类">
          <el-select v-model="data.form.categoryId" placeholder="请选择反诈分类">
            <el-option
                v-for="item in data.categoryData"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item prop="status" label="活动状态">
          <el-radio-group v-model="data.form.status">
            <el-radio-button label="进行中" value="进行中" />
            <el-radio-button label="已结束" value="已结束" />
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="content" label="活动内容" style="width: 100%;">
          <div style="border: 1px solid #ccc; width: 100%; height: 600px;">
            <div id="vditor-container"></div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="帖子内容" v-model="data.viewVisible" width="50%" destroy-on-close>
      <div style="padding: 10px 20px" v-html="data.viewContent"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref, onBeforeUnmount, shallowRef,nextTick} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit} from "@element-plus/icons-vue";
import Vditor from 'vditor'
import 'vditor/dist/index.css'

const formRef = ref()
const vditorRef = shallowRef(null)
const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 5,
  total: 0,
  title: null,
  categoryId: null,
  status: null,
  categoryData: [],
  ids: [],
  rules: {
    title: [
      { required: true, message: '请输入活动标题', trigger: 'blur' },
    ],
    cover: [
      { required: true, message: '请上传活动封面', trigger: 'blur' },
    ],
    categoryId: [
      { required: true, message: '请选择反诈分类', trigger: 'blur' },
    ],
    status: [
      { required: true, message: '请选择活动状态', trigger: 'blur' },
    ],
    content: [
      { required: true, message: '请输入活动内容', trigger: 'blur' },
    ],
  },
  viewContent: null,
  viewVisible: false,
})

//初始化 Vditor 编辑器
const initVditor = (initialValue = '') => {
  if (vditorRef.value) {
    vditorRef.value.destroy()
  }
  
  const vditor = new Vditor('vditor-container', {
    height: 500,
    cache: { enable: false },
    mode: 'ir',
    upload: {
      url: baseUrl + '/files/upload',
      token: data.user.token,
      fieldName: 'file',
      accept: 'image/*,.png,.jpg,.jpeg,.gif,.webp,.mp4,.avi,.mov',
      max: 10 * 1024 * 1024,
      success: (editor, msg) => {
        console.log('Vditor 上传成功回调:', msg)
        try {
          const res = JSON.parse(msg)
          if (res.code === '200') {
            const fileUrl = res.data.startsWith('http') ? res.data : baseUrl + res.data
            vditor.insertValue(`![image](${fileUrl})`)
          } else {
            ElMessage.error(res.msg || '上传失败')
          }
        } catch (e) {
          console.error('解析响应失败:', e)
          ElMessage.error('上传失败')
        }
      },
      error: (msg) => {
        console.error('Vditor 上传错误:', msg)
        ElMessage.error('图片上传失败')
      }
    },
    toolbarConfig: {
      pin: true,
    },
    icon: 'material',
    value: initialValue,
    after: () => {
      console.log('Vditor 编辑器加载完成')
    }
  })
  vditorRef.value = vditor
}

//销毁 Vditor 编辑器
const destroyVditor = () => {
  if (vditorRef.value) {
    vditorRef.value.destroy()
    vditorRef.value = null
  }
}

// 组件卸载时销毁编辑器
onBeforeUnmount(() => {
  destroyVditor()
})

const load = () => {
  request.get('/activity/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      categoryId: data.categoryId,
      status: data.status
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.records || []
      data.total = res.data?.total || 0
    }
  })
}

const loadCategory = () => {
  request.get('/category/selectAll').then(res => {
    if (res.code === '200') {
      data.categoryData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadCategory()

const handleAdd = () => {
  data.form = {}
  data.formVisible = true
  nextTick(() => {
    initVditor('')
  })
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
  nextTick(() => {
    initVditor(row.content || '')
  })
}

const getContent = () => {
  if (vditorRef.value) {
    return vditorRef.value.getValue()
  }
  return ''
}

const add = () => {
    data.form.content = getContent()
  request.post('/activity/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
    data.form.content = getContent()
  request.put('/activity/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const save = () => {
  // 修改：在验证之前先获取 Vditor 编辑器的内容
  if (vditorRef.value) {
    data.form.content = vditorRef.value.getValue()
  }
  
  formRef.value.validate(valid => {
    if (valid) {
      data.form.id ? update() : add()
    } else {
      ElMessage.error('请填写必填项')
    }
  })
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/activity/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success("删除成功")
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}

const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning("请选择数据")
    return
  }
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete("/activity/delete/batch", {data: data.ids}).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}

const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.id)
}

const reset = () => {
  data.title = null
  data.categoryId = null
  data.status = null
  data.pageNum = 1
  load()
}

const viewInit = (content) => {
  data.viewContent = content
  data.viewVisible = true
}

const handleImgUpload = (res) => {
  data.form.cover = res.data
}

load()
</script>


