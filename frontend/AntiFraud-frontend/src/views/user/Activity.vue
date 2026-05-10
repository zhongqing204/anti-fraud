<template>
  <div class="activity-page">
    <!-- 顶部搜索栏 -->
    <div class="search-header">
      <div class="search-container">
        <el-input 
          prefix-icon="Search" 
          v-model="data.title" 
          @keyup.enter="load" 
          clearable 
          @clear="load" 
          placeholder="请输入活动名称查询" 
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
                <el-option label="全部" :value="undefined" />
                <el-option v-for="item in data.categoryData" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </div>
            <div class="filter-item">
              <div class="filter-label">活动类型</div>
              <el-select v-model="data.activityType" placeholder="请选择活动类型" clearable class="filter-select" :teleported="false">
                <el-option label="全部" :value="undefined" />
                <el-option label="线上" value="线上" />
                <el-option label="线下" value="线下" />
              </el-select>
            </div>
            <!-- 【新增】持续时间类型筛选 -->
            <div class="filter-item">
              <div class="filter-label">持续时间</div>
              <el-select v-model="data.durationType" placeholder="请选择持续时间" clearable class="filter-select" :teleported="false">
                <el-option label="全部" :value="undefined" />
                <el-option label="短期活动" value="short" />
                <el-option label="长期活动" value="long" />
              </el-select>
            </div>
            <div class="filter-item">
              <div class="filter-label">状态</div>
              <el-select v-model="data.status" placeholder="请选择状态" clearable class="filter-select" :teleported="false">
                <el-option label="全部" :value="undefined" />
                <el-option label="未开始" value="未开始" />
                <el-option label="进行中" value="进行中" />
                <el-option label="已结束" value="已结束" />
              </el-select>
            </div>
            <div class="filter-item">
              <div class="filter-label">时间</div>
              <el-select v-model="data.timeFilter" placeholder="请选择时间范围" clearable class="filter-select" :teleported="false">
                <el-option label="全部" :value="undefined" />
                <el-option label="最近一周" value="week" />
                <el-option label="最近一月" value="month" />
                <el-option label="最近三月" value="threeMonths" />
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
    
    <!-- 活动列表 -->
    <div class="content-wrapper">
      <div class="activities-container">
        <div class="section-title">
          <el-icon color="#409EFF"><Calendar /></el-icon>
          <span>全部活动</span>
        </div>
        <div class="activity-list">
          <div 
            v-for="item in data.activityData" 
            :key="item.id" 
            class="activity-card fade-in"
            @click="router.push('/front/activityDetail?id=' + item.id)"
          >
            <div class="card-content">
              <div class="activity-image-wrapper">
                <img :src="baseUrl + item.cover" alt="" class="activity-image">
                <div class="image-overlay"></div>
              </div>
              <div class="activity-info">
                <h3 class="activity-title">{{ item.title }}</h3>
                <p class="activity-desc line2">{{ stripHtml(item.content)?.substring(0, 150) || '暂无内容' }}...</p>
                <div class="activity-meta">
                  <div class="meta-row">
                    <span v-if="item.startTime" class="meta-item">
                      <el-icon><Clock /></el-icon>
                      开始：{{ formatTime(item.startTime) }}
                    </span>
                    <span v-if="item.endTime" class="meta-item">
                      <el-icon><Clock /></el-icon>
                      结束：{{ formatTime(item.endTime) }}
                    </span>
                  </div>
                  <div class="meta-row tags">
                    <!-- 【新增】活动持续时间类型标签 -->
                    <el-tag v-if="item.activityDurationType === 'long'" type="warning" size="small">
                      📅 长期活动（{{ item.durationDays }}天）
                    </el-tag>
                    <el-tag v-else type="info" size="small">
                      ⏱️ 短期活动
                    </el-tag>
                    <el-tag v-if="item.activityType" :type="item.activityType === '线上' ? 'success' : 'primary'" size="small">
                      {{ item.activityType }}
                    </el-tag>
                    <span v-if="item.activityType === '线下' && item.location" class="location-tag">
                      📍 {{ item.location }}
                    </span>
                    <span v-if="item.activityType === '线上'" class="online-tag">
                      💻 线上活动
                    </span>
                    <!-- 【新增】报名人数信息 -->
                    <span v-if="item.maxParticipants && item.maxParticipants > 0" class="participants-tag">
                      👥 {{ item.currentParticipants || 0 }}/{{ item.maxParticipants }}
                    </span>
                  </div>
                </div>
              </div>
              <div class="activity-status">
                <el-tag :type="getStatusType(item.status)" size="large" class="status-tag">
                  {{ item.status || '进行中' }}
                </el-tag>
                <div class="view-hint">点击查看详情</div>
              </div>
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
import {Filter, Star, Calendar} from '@element-plus/icons-vue'

const baseUrl = import.meta.env.VITE_BASE_URL

// 【新增】格式化时间显示
const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

// 【新增】去除HTML标签
const stripHtml = (html) => {
  if (!html) return ''
  const div = document.createElement('div')
  div.innerHTML = html
  return div.textContent || div.innerText || ''
}

// 【新增】获取进行中的活动数量
const getOngoingCount = () => {
  return data.activityData.filter(item => item.status === '进行中').length
}

// 【新增】获取状态标签类型
const getStatusType = (status) => {
  switch(status) {
    case '未开始': return ''
    case '进行中': return 'success'
    case '已结束': return 'info'
    default: return 'success'
  }
}

// 【新增】获取线上活动数量
const getOnlineCount = () => {
  return data.activityData.filter(item => item.activityType === '线上').length
}

// 【新增】获取线下活动数量
const getOfflineCount = () => {
  return data.activityData.filter(item => item.activityType === '线下').length
}

// 【新增】获取热门活动（前3个）
const getHotActivities = () => {
  return data.activityData.slice(0, 3)
}

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  title: null,
  categoryId: null,
  // 【新增】线上线下筛选字段
  activityType: null,
  // 【新增】持续时间类型筛选
  durationType: null,
  status: null,
  timeFilter: null,
  pageNum: 1,
  pageSize: 6,
  total: 0,
  activityData: [],
  categoryData: [],
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

// 重置筛选条件
const resetFilter = () => {
  data.categoryId = null
  data.activityType = null // 【新增】重置线上线下筛选
  data.durationType = null // 【新增】重置持续时间筛选
  data.status = null
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
  data.hasFilter = !!(data.categoryId || data.activityType || data.durationType || data.status || data.timeFilter)
}

// 加载活动列表
const load = () => {
  request.get('/activity/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      categoryId: data.categoryId,
      // 【新增】线上线下筛选参数
      activityType: data.activityType,
      // 【新增】持续时间类型筛选参数
      activityDurationType: data.durationType,
      status: data.status
    }
  }).then(res => {
    console.log('Activity response:', res)
    if (res.code === '200') {
      console.log('Records:', res.data?.records)
      console.log('Total:', res.data?.total)
      let records = res.data?.records || []
      
      // 【新增】前端时间筛选
      if (data.timeFilter) {
        const now = new Date()
        records = records.filter(item => {
          if (!item.startTime) return false
          const startTime = new Date(item.startTime)
          const diffDays = (now - startTime) / (1000 * 60 * 60 * 24)
          
          if (data.timeFilter === 'week') {
            return diffDays <= 7
          } else if (data.timeFilter === 'month') {
            return diffDays <= 30
          } else if (data.timeFilter === 'threeMonths') {
            return diffDays <= 90
          }
          return true
        })
      }
      
      data.activityData = records
      data.total = records.length
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
.activity-page {
  background: 
    url('https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1920&q=80') center/cover,
    linear-gradient(135deg, rgba(67, 233, 123, 0.15) 0%, rgba(56, 249, 215, 0.15) 100%);
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 1px;
}

.activity-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 15% 25%, rgba(67, 233, 123, 0.2) 0%, transparent 50%),
    radial-gradient(circle at 85% 75%, rgba(56, 249, 215, 0.2) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(255, 236, 210, 0.15) 0%, transparent 50%);
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

.activities-container {
  background: 
    linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(240, 255, 240, 0.98) 100%);
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #333;
}

/* 活动列表 */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 活动卡片 */
.activity-card {
  border: 1px solid #e8ecf1;
  border-radius: 12px;
  overflow: hidden;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.activity-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateX(5px);
  border-color: #667eea;
}

.card-content {
  display: flex;
  gap: 20px;
  padding: 20px;
  align-items: center;
}

.activity-image-wrapper {
  position: relative;
  flex-shrink: 0;
  width: 180px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
}

.activity-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.activity-card:hover .activity-image {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, transparent 60%, rgba(0,0,0,0.2));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.activity-card:hover .image-overlay {
  opacity: 1;
}

.activity-info {
  flex: 1;
}

.activity-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
}

.activity-desc {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 10px 0;
}

.activity-meta {
  color: #999;
  font-size: 13px;
}

.meta-row {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.tags {
  display: flex;
  gap: 10px;
  align-items: center;
}

.location-tag,
.online-tag {
  color: #666;
  font-size: 13px;
}

.activity-status {
  flex-shrink: 0;
  text-align: center;
  min-width: 120px;
}

.status-tag {
  padding: 10px 20px;
  font-size: 14px;
  margin-bottom: 8px;
}

.view-hint {
  font-size: 12px;
  color: #999;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 30px;
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

.line2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card-content {
    flex-direction: column;
  }
  
  .activity-image-wrapper {
    width: 100%;
    height: 180px;
  }
  
  .activity-status {
    width: 100%;
  }
  
  .search-container {
    flex-direction: column;
  }
  
  .search-input {
    max-width: 100%;
  }
}
</style>
