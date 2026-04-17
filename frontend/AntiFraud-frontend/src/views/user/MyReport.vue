<template>
  <div style="width: 70%; margin: 20px auto">
    <div style="font-size: 18px">我的举报</div>
    <div style="margin-top: 20px; padding: 20px" class="card">
      <el-table stripe :data="data.tableData">
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
        <el-table-column prop="time" label="举报时间" />
        <el-table-column prop="status" label="处理状态">
          <template v-slot="scope">
            <el-tag v-if="scope.row.status === '已处理'" type="success">{{ scope.row.status }}</el-tag>
            <el-tag v-if="scope.row.status === '待处理'" type="danger">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="处理说明" />
        <el-table-column label="操作" width="100" fixed="right">
          <template v-slot="scope">
            <el-button type="danger" circle :icon="Delete" @click="del(scope.row.id)"></el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="data.total" style="margin-top: 20px">
        <el-pagination @current-change="load" layout="total, prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
      </div>
    </div>

  </div>
</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";
import {Delete, Link} from "@element-plus/icons-vue";

const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  title: null,
  pageNum: 1,
  pageSize: 5,
  tableData: [],
  total: 0,
})

const load = () => {
  request.get('/report/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userId: data.user.id
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.records
      data.total = res.data?.total
    } else {
      ElMessage.error(res.msg)
    }
  })
}
load()

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
</script>