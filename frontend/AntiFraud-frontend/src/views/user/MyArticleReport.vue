<template>
  <div class="my-report-page">
    <!-- 页面标题 -->
    <div class="page-title">
      <el-icon class="title-icon"><Warning /></el-icon>
      我的举报记录
    </div>

    <!-- 举报列表 -->
    <div class="report-list">
      <div 
        v-for="item in data.tableData" 
        :key="item.id" 
        class="report-card"
      >
        <!-- 卡片头部 -->
        <div class="card-header">
          <div class="report-icon">
            <el-icon :size="24"><WarningFilled /></el-icon>
          </div>
          <div class="report-title">{{ item.title || '举报内容' }}</div>
          <el-tag 
            v-if="item.status === '已处理'" 
            type="success" 
            class="status-tag"
          >
            {{ item.status }}
          </el-tag>
          <el-tag 
            v-else-if="item.status === '处理中'" 
            type="warning" 
            class="status-tag"
          >
            {{ item.status }}
          </el-tag>
          <el-tag 
            v-else 
            type="danger" 
            class="status-tag"
          >
            {{ item.status }}
          </el-tag>
        </div>

        <!-- 卡片内容 -->
        <div class="card-body">
          <div class="info-row">
            <span class="info-label">举报类型：</span>
            <el-tag type="info" size="small">{{ item.category || '未分类' }}</el-tag>
          </div>
          
          <div class="info-row">
            <span class="info-label">详细原因：</span>
            <span class="info-value">{{ item.content || '-' }}</span>
          </div>
          
          <div class="info-row">
            <span class="info-label">举报时间：</span>
            <span class="info-value">{{ item.time }}</span>
          </div>

          <div v-if="item.reason" class="info-row">
            <span class="info-label">处理说明：</span>
            <span class="info-value">{{ item.reason }}</span>
          </div>

          <div v-if="item.files" class="info-row">
            <span class="info-label">附件：</span>
            <el-button 
              link 
              type="primary" 
              size="small"
              @click="downloadFile(item.files)"
            >
              <el-icon><Link /></el-icon>
              查看附件
            </el-button>
          </div>
        </div>

        <!-- 卡片底部操作按钮 -->
        <div class="card-footer">
          <el-button 
            type="primary" 
            size="default"
            @click="viewDetail(item)"
          >
            查看详情
          </el-button>
          <el-button 
            type="danger" 
            size="default"
            @click="del(item.id)"
          >
            删除
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="data.tableData.length === 0 && !data.loading" description="暂无举报记录" />
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="data.total">
      <el-pagination 
        @current-change="load" 
        layout="total, prev, pager, next, jumper" 
        :page-size="data.pageSize" 
        v-model:current-page="data.pageNum" 
        :total="data.total" 
      />
    </div>

    <!-- 举报详情对话框 -->
    <el-dialog title="举报详情" v-model="data.detailVisible" width="50%" destroy-on-close draggable>
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
import {reactive, ref} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {Link, Warning, WarningFilled} from "@element-plus/icons-vue";

const baseUrl = import.meta.env.VITE_BASE_URL
const loading = ref(false)

const data = reactive({
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  detailVisible: false,
  currentReport: {},
  loading: false
})

const load = () => {
  data.loading = true
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
  }).finally(() => {
    data.loading = false
  })
}
load()

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

const downloadFile = (filesStr) => {
  if (!filesStr) return
  const files = filesStr.split(',').filter(url => url.trim())
  if (files.length > 0) {
    window.open(baseUrl + files[0])
  }
}

const viewDetail = (row) => {
  data.currentReport = JSON.parse(JSON.stringify(row))
  data.detailVisible = true
}
</script>

<style scoped>
.my-report-page {
  max-width: 1200px;
  margin: 30px auto;
  padding: 0 20px;
}

/* 页面标题 */
.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 30px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 32px;
  color: #f56c6c;
}

/* 举报列表 */
.report-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 30px;
}

/* 举报卡片 */
.report-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 16px;
  padding: 25px;
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.08),
    0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
}

.report-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
}

.report-card:hover {
  box-shadow: 
    0 8px 30px rgba(0, 0, 0, 0.12),
    0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.report-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #f56c6c 0%, #e74c3c 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.report-title {
  flex: 1;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.status-tag {
  flex-shrink: 0;
}

/* 卡片内容 */
.card-body {
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  line-height: 1.8;
}

.info-label {
  color: #909399;
  font-size: 14px;
  min-width: 100px;
  flex-shrink: 0;
}

.info-value {
  color: #606266;
  font-size: 14px;
  flex: 1;
  word-break: break-all;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  gap: 12px;
  padding-top: 15px;
  border-top: 1px solid #e4e7ed;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>
