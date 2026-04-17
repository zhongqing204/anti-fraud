<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.content" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入举报内容查询"></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="content" label="举报内容" width="400" />
        <el-table-column prop="files" label="附件" width="200">
          <template v-slot="scope">
            <div v-if="scope.row.files">
              <div v-for="(file, index) in parseFiles(scope.row.files)" :key="index" style="margin-bottom: 5px">
                <el-button 
                  link 
                  type="primary" 
                  @click="downloadFile(file)"
                >
                  <el-icon><Link /></el-icon>
                  下载文件
                </el-button>
              </div>
            </div>
            <span v-else style="color: #999">无附件</span>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户姓名" />
        <el-table-column prop="time" label="举报时间" />
        <el-table-column prop="status" label="处理状态">
          <template v-slot="scope">
            <el-tag v-if="scope.row.status === '已处理'" type="success">{{ scope.row.status }}</el-tag>
            <el-tag v-if="scope.row.status === '待处理'" type="danger">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="处理说明" />
        <el-table-column label="操作" width="200" fixed="right">
          <template v-slot="scope">
            <el-button type="primary" plain @click="handleEdit(scope.row)">处理</el-button>
            <el-button type="danger" plain @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <el-dialog title="处理信息" v-model="data.formVisible" width="40%" destroy-on-close>
      <el-form :model="data.form" label-width="80px" style="padding: 20px">
        <el-form-item prop="status" label="处理状态">
          <el-select v-model="data.form.status" placeholder="请选择状态">
            <el-option value="待处理" label="待处理"></el-option>
            <el-option value="已处理" label="已处理"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="reason" label="处理说明">
          <el-input v-model="data.form.reason" placeholder="请输入处理说明"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="update">确 定</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>

import {reactive, ref} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit, Link} from "@element-plus/icons-vue";

const baseUrl = import.meta.env.VITE_BASE_URL


const data = reactive({
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 5,
  total: 0,
  content: null,
  ids: [],
  rules: {
    status: [
      { required: true, message: '请选择处理状态', trigger: 'blur' }
    ],
  },
})


const load = () => {
  request.get('/report/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.records || []
      data.total = res.data?.total
    }
  })
}
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}
const update = () => {
  request.put('/report/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    }
  })
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/report/delete/' + id).then(res => {
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
    request.delete("/report/delete/batch", {data: data.ids}).then(res => {
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
  data.content = null
  load()
}

const parseFiles = (filesStr) => {
  if (!filesStr) return []
  try {
    return JSON.parse(filesStr)
  } catch (e) {
    return [filesStr]
  }
}

const downloadFile = (filePath) => {
  const url = baseUrl + filePath
  const link = document.createElement('a')
  link.href = url
  link.download = filePath.split('/').pop()
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

load()
</script>