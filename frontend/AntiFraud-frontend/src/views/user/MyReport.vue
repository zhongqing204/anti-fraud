<template>
  <div style="width: 70%; margin: 20px auto">
    <div style="font-size: 18px">我的举报</div>
    <div style="margin-top: 20px; padding: 20px" class="card">
      <el-table stripe :data="data.tableData">
        <el-table-column prop="content" label="举报内容" show-overflow-tooltip />
        <!-- 新增：类型列 -->
        <el-table-column prop="category" label="类型" width="100">
          <template v-slot="scope">
            <el-tag v-if="scope.row.category" type="info" size="small">{{ scope.row.category }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="files" label="附件" width="120">
          <template v-slot="scope">
            <div v-if="scope.row.files">
              <el-button 
                link 
                type="primary" 
                @click="downloadFile(scope.row.files)"
              >
                <el-icon><Link /></el-icon>
                下载文件
              </el-button>
            </div>
            <span v-else style="color: #999">无附件</span>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="举报时间" width="160" />
        <el-table-column prop="status" label="处理状态" width="100">
          <template v-slot="scope">
            <el-tag v-if="scope.row.status === '已处理'" type="success">{{ scope.row.status }}</el-tag>
            <el-tag v-else-if="scope.row.status === '处理中'" type="warning">{{ scope.row.status }}</el-tag>
            <el-tag v-else type="danger">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="处理说明" show-overflow-tooltip />
        <!-- 新增：操作列 - 详情和删除按钮 -->
        <el-table-column label="操作" width="150">
          <template v-slot="scope">
            <el-button link type="primary" @click="viewDetail(scope.row)">详情</el-button>
            <el-button link type="danger" @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div style="margin-top: 20px" class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <!-- 新增：举报详情对话框 -->
    <el-dialog title="举报详情" v-model="data.detailVisible" width="50%" destroy-on-close>
      <div style="padding: 20px;">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="举报类型">
            {{ data.currentReport.category || '未分类' }}
          </el-descriptions-item>
          <el-descriptions-item label="举报内容">
            <div style="white-space: pre-wrap;">{{ data.currentReport.content }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="举报时间">
            {{ data.currentReport.time }}
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag v-if="data.currentReport.status === '已处理'" type="success">{{ data.currentReport.status }}</el-tag>
            <el-tag v-else-if="data.currentReport.status === '处理中'" type="warning">{{ data.currentReport.status }}</el-tag>
            <el-tag v-else type="danger">{{ data.currentReport.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理说明" v-if="data.currentReport.reason">
            {{ data.currentReport.reason }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {Link} from "@element-plus/icons-vue";

const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  detailVisible: false,   // 新增：详情对话框显示状态
  currentReport: {}       // 新增：当前查看的举报详情
})

// 加载用户的举报记录
const load = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  request.get('/report/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userId: user.id
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

// 删除举报记录
const del = (id) => {
  ElMessageBox.confirm('确定要删除这条举报记录吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/report/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}

// 下载附件
const downloadFile = (filesStr) => {
  if (!filesStr) return
  const files = filesStr.split(',').filter(url => url.trim())
  if (files.length > 0) {
    window.open(baseUrl + files[0])
  }
}

// 新增：查看详情方法
const viewDetail = (row) => {
  data.currentReport = JSON.parse(JSON.stringify(row))
  data.detailVisible = true
}
</script>
