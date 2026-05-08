<template>
  <div class="publicity-page">
    <!-- 顶部搜索栏 -->
    <div class="search-header">
      <div class="search-container">
        <el-input 
          prefix-icon="Search" 
          v-model="data.title" 
          @keyup.enter="load" 
          clearable 
          @clear="load" 
          placeholder="请输入宣传标题查询" 
          class="search-input"
        ></el-input>
        <el-popover trigger="manual" :width="300" v-model:visible="data.filterVisible">
          <template #reference>
            <el-button :type="data.hasFilter ? 'primary' : ''" @click="data.filterVisible = !data.filterVisible" class="filter-btn">
              <el-icon><Filter /></el-icon>
              筛选
            </el-button>
          </template>
          <div class="filter-content" @click.stop>
            <div class="filter-item">
              <div class="filter-label">分类</div>
              <el-select v-model="data.categoryId" placeholder="请选择分类" clearable class="filter-select" :teleported="false">
                <el-option label="全部" :value="null" />
                <el-option v-for="item in data.categoryData" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </div>
            <div class="filter-item">
              <div class="filter-label">时间</div>
              <el-select v-model="data.timeFilter" placeholder="请选择时间范围" clearable class="filter-select" :teleported="false">
                <el-option label="全部" :value="null" />
                <el-option label="最近一周" value="week" />
                <el-option label="最近一月" value="month" />
                <el-option label="最近三月" value="threeMonths" />
                <el-option label="最近一年" value="year" />
              </el-select>
            </div>
            <div class="filter-actions">
              <el-button size="small" @click="resetFilter">重置</el-button>
              <el-button type="primary" size="small" @click="applyFilter">确定</el-button>
            </div>
          </div>
        </el-popover>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-wrapper">
      <div class="publicity-grid">
        <div 
          v-for="item in data.newsData" 
          :key="item.id" 
          class="publicity-card fade-in"
          @click="router.push('/front/publicityDetail?id=' + item.id)"
        >
          <div class="card-image-wrapper">
            <img 
              :src="getCoverUrl(item.cover)" 
              alt="" 
              class="card-image"
            >
            <div class="image-overlay"></div>
          </div>
          <div class="card-content">
            <h3 class="card-title line1">{{ item.title }}</h3>
            <div class="card-meta">
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ item.createTime }}
              </span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination 
          @current-change="load" 
          layout="total, prev, pager, next, jumper" 
          :page-size="data.pageSize" 
          v-model:current-page="data.pageNum" 
          :total="data.total" 
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import {Clock, Filter} from '@element-plus/icons-vue'

const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  title: null,
  categoryId: null,
  timeFilter: null,
  categoryData: [],
  pageNum: 1,
  pageSize: 20,
  total: 0,
  newsData: [],
  filterVisible: false,
  hasFilter: false
})

// 加载分类数据
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

// 获取封面图片URL
const getCoverUrl = (cover) => {
  if (!cover) return ''
  if (cover.startsWith('http://') || cover.startsWith('https://')) {
    return cover
  }
  return baseUrl + cover
}

// 重置筛选条件
const resetFilter = () => {
  data.categoryId = null
  data.timeFilter = null
  applyFilter()
}

// 应用筛选条件
const applyFilter = () => {
  data.filterVisible = false
  data.pageNum = 1
  updateFilterStatus()
  load()
}

// 更新筛选状态
const updateFilterStatus = () => {
  data.hasFilter = !!(data.categoryId || data.timeFilter)
}

// 加载数据列表
const load = () => {
  request.get('/publicity/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      categoryId: data.categoryId,
    }
  }).then(res => {
    console.log('Publicity response:', res)
    if (res.code === '200') {
      console.log('Records:', res.data?.records)
      console.log('Total:', res.data?.total)
      data.newsData = res.data?.records || []
      data.total = res.data?.total || 0
      console.log('data.total set to:', data.total)
    } else {
      ElMessage.error(res.msg)
    }
  })
}

load()
</script>

<style scoped>
/* 全局样式 */
.publicity-page {
  background: 
    url('https://images.unsplash.com/photo-1557683316-973673baf926?w=1920&q=80') center/cover,
    linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 0;
}

.publicity-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(102, 126, 234, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(118, 75, 162, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 40% 20%, rgba(255, 107, 107, 0.1) 0%, transparent 50%);
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
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-input {
  flex: 1;
  max-width: 400px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-btn {
  border-radius: 20px;
  padding: 10px 20px;
}

.filter-content {
  padding: 15px;
}

.filter-item {
  margin-bottom: 15px;
}

.filter-label {
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
}

.filter-select {
  width: 100%;
}

.filter-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 15px;
}

/* 内容区域 */
.content-wrapper {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
}

/* 网格布局 */
.publicity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
}

/* 卡片样式 */
.publicity-card {
  background: 
    linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 249, 250, 0.98) 100%);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  animation: fadeIn 0.6s ease-out;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  cursor: pointer;
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

.publicity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.card-image-wrapper {
  position: relative;
  overflow: hidden;
}

.card-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  transition: transform 0.3s ease;
  cursor: pointer;
}

.publicity-card:hover .card-image {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, transparent 60%, rgba(0,0,0,0.3));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.publicity-card:hover .image-overlay {
  opacity: 1;
}

.card-content {
  padding: 15px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 0 0 12px 0;
  cursor: pointer;
  transition: color 0.3s ease;
}

.card-title:hover {
  color: #667eea;
}

.card-meta {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

/* 文本截断 */
.line1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .publicity-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 15px;
  }
  
  .search-container {
    flex-direction: column;
  }
  
  .search-input {
    max-width: 100%;
  }
}
</style>
