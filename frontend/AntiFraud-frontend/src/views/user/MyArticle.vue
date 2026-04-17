<template>
  <div style="width: 70%; margin: 20px auto">
    <div style="font-size: 18px">我的帖子</div>
    <div style="margin-top: 20px; padding: 20px" class="card">
      <div style="margin-bottom: 20px">
        <el-input clearable @clear="reset" v-model="data.title" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入帖子标题查询"></el-input>
        <el-button type="info" plain @click="load">查询</el-button>
        <el-button type="success" plain @click="handleAdd">发布帖子</el-button>
      </div>
      <el-table stripe :data="data.tableData">
        <el-table-column prop="title" label="帖子名称" width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="反诈分类" />
        <el-table-column prop="userName" label="用户名称" />
        <el-table-column prop="time" label="发布时间" />
        <el-table-column prop="status" label="审核状态">
          <template v-slot="scope">
            <el-tag type="warning" v-if="scope.row.status === '待审核'">{{ scope.row.status }}</el-tag>
            <el-tag type="success" v-if="scope.row.status === '审核通过'">{{ scope.row.status }}</el-tag>
            <el-tag type="danger" v-if="scope.row.status === '审核拒绝'">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="审核说明" />
        <el-table-column label="操作" width="100" fixed="right">
          <template v-slot="scope">
            <el-button type="primary" circle :icon="Edit" @click="handleEdit(scope.row)"></el-button>
            <el-button type="danger" circle :icon="Delete" @click="del(scope.row.id)"></el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="data.total" style="margin-top: 20px">
        <el-pagination @current-change="load" layout="total, prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
      </div>
    </div>

    <el-dialog title="发布帖子" v-model="data.formVisible" width="60%" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="80px" style="padding: 20px">
        <el-form-item prop="categoryId" label="分类">
          <el-select v-model="data.form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
                v-for="item in data.categoryData"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item prop="title" label="标题">
          <el-input v-model="data.form.title" placeholder="请输入完整帖子标题(5-31个字)" maxlength="31" show-word-limit></el-input>
        </el-form-item>
        <el-form-item prop="content" label="内容">
          <div style="border: 1px solid #ccc; width: 100%; height: 400px;">
            <Toolbar
              style="border-bottom: 1px solid #ccc"
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              mode="default"
            />
            <Editor
              style="height: 350px; overflow-y: hidden;"
              v-model="data.form.content"
              :defaultConfig="editorConfig"
              mode="default"
              @onCreated="handleCreated"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref, onBeforeUnmount, shallowRef, markRaw} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";
import {Delete, Edit} from "@element-plus/icons-vue";
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const formRef = ref()
const baseUrl = import.meta.env.VITE_BASE_URL
const editorRef = shallowRef()

const toolbarConfig = {}

const editorConfig = { 
  placeholder: '请输入正文（建议200-2000字）',
  MENU_CONF: {
    uploadImage: {
      server: baseUrl + '/files/upload',
      fieldName: 'file',
      maxFileSize: 10 * 1024 * 1024,
      allowedFileTypes: ['image/*'],
      customInsert(res, insertFn) {
        if (res.code === '200') {
          const url = res.data.startsWith('http') ? res.data : baseUrl + res.data
          insertFn(url, '', url)
        } else {
          ElMessage.error(res.msg || '上传失败')
        }
      }
    }
  }
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

const handleCreated = (editor) => {
  editorRef.value = markRaw(editor)
}

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  title: null,
  pageNum: 1,
  pageSize: 5,
  form: {},
  formVisible: false,
  tableData: [],
  total: 0,
  categoryData: [],
  rules: {
    categoryId: [
      { required: true, message: '请选择分类', trigger: 'change' },
    ],
    title: [
      { required: true, message: '请输入帖子标题', trigger: 'blur' },
    ],
    content: [
      { required: true, message: '请输入帖子内容', trigger: 'blur' },
    ],
  }
})

const load = () => {
  request.get('/article/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      userId: data.user.id
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.records || []
      data.total = res.data?.total || 0
    } else {
      ElMessage.error(res.msg)
    }
  })
}
load()

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
  data.form.userId = data.user.id
  data.form.status = '待审核'
  data.form.content = ''
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const add = () => {
  request.post('/article/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('发布成功')
      data.formVisible = false
      data.form.content = ''
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  request.put('/article/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      data.form.content = ''
      load()
    }
  })
}

const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      if (!data.form.content || data.form.content === '<p><br></p>') {
        ElMessage.error('请填写帖子内容')
        return
      }
      data.form.id ? update() : add()
    }
  })
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/article/delete/' + id).then(res => {
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

const reset = () => {
  data.title = null
  load()
}

</script>

<style scoped>

</style>
