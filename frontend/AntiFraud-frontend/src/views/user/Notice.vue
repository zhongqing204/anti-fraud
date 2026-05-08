<template>
  <div class="notice-page">
    <!-- 顶部搜索栏 -->
    <div class="search-header">
      <div class="search-container">
        <el-input 
          prefix-icon="Search" 
          v-model="data.title" 
          @keyup.enter="load" 
          clearable 
          @clear="load" 
          placeholder="请输入公告标题查询" 
          class="search-input"
        ></el-input>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-wrapper">
      <div class="notice-table-container">
        <el-table 
          :data="data.noticeList" 
          stripe 
          @row-click="viewNotice" 
          :header-cell-style="{ cursor: 'default' }"
          class="notice-table"
        >
          <el-table-column prop="title" label="公告标题">
            <template #default="scope">
              <span class="notice-title">{{ scope.row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="发布时间" width="200" align="center">
            <template #default="scope">
              <div class="time-cell">
                <el-icon><Clock /></el-icon>
                <span>{{ scope.row.time }}</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    
    <!-- 公告详情对话框 -->
    <el-dialog title="公告详情" v-model="data.detailVisible" width="60%" top="5vh" class="notice-dialog">
      <div class="dialog-header">
        <h3 class="dialog-title">{{ data.currentNotice.title }}</h3>
        <p class="dialog-time">发布时间：{{ data.currentNotice.time }}</p>
      </div>
      <div v-html="data.currentNotice.content" class="dialog-content"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import request from "@/utils/request.js";

const data = reactive({
  noticeList: [],
  title: null,
  detailVisible: false,
  currentNotice: {}
});

const loadNotice = () => {
  request.get('/notice/selectAll', {
    params: {
      title: data.title
    }
  }).then(res => {
    if (res.code === '200') {
      data.noticeList = res.data || [];
    }
  });
};

const viewNotice = (row) => {
  data.currentNotice = row;
  data.detailVisible = true;
};

const load = () => {
  loadNotice();
};

onMounted(() => {
  loadNotice();
});
</script>

<style scoped>
/* 全局样式 */
.notice-page {
  background: 
    url('https://images.unsplash.com/photo-1450101499163-c8848c66ca85?w=1920&q=80') center/cover,
    linear-gradient(135deg, rgba(156, 39, 176, 0.15) 0%, rgba(103, 58, 183, 0.15) 100%);
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 0;
}

.notice-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 30% 20%, rgba(156, 39, 176, 0.2) 0%, transparent 50%),
    radial-gradient(circle at 70% 80%, rgba(103, 58, 183, 0.2) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(233, 30, 99, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

/* 搜索头部 */
.search-header {
  background: transparent;
  padding: 30px 20px;
}

.search-container {
  max-width: 1200px;
  margin: 0 auto;
}

.search-input {
  max-width: 500px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 内容区域 */
.content-wrapper {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
}

.notice-table-container {
  background: 
    linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(245, 240, 255, 0.98) 100%);
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  animation: fadeIn 0.6s ease-out;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.notice-table {
  border-radius: 8px;
  overflow: hidden;
}

.notice-title {
  display: inline-block;
  cursor: pointer;
  transition: color 0.3s ease;
  font-weight: 500;
}

.notice-title:hover {
  color: #667eea;
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 5px;
  justify-content: center;
  color: #666;
}

/* 对话框样式 */
.notice-dialog :deep(.el-dialog__header) {
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 15px;
}

.dialog-header {
  text-align: center;
  margin-bottom: 25px;
}

.dialog-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
}

.dialog-time {
  color: #999;
  margin: 0;
  font-size: 14px;
}

.dialog-content {
  line-height: 1.8;
  max-height: 60vh;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-container {
    flex-direction: column;
  }
  
  .search-input {
    max-width: 100%;
  }
}
</style>