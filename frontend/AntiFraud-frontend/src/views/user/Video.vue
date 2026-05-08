<template>
  <div class="video-page">
    <!-- 顶部搜索栏 -->
    <div class="search-header">
      <div class="search-container">
        <el-input 
          prefix-icon="Search" 
          v-model="data.title" 
          @keyup.enter="load" 
          clearable 
          @clear="load" 
          placeholder="请输入视频标题查询" 
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
              <div class="filter-label">时间</div>
              <el-select v-model="data.timeFilter" placeholder="请选择时间范围" clearable class="filter-select" :teleported="false">
                <el-option label="全部" :value="undefined" />
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
      <div class="video-grid">
        <div 
          v-for="item in data.videoData" 
          :key="item.id" 
          class="video-card fade-in"
          @click="router.push('/front/videoDetail?id=' + item.id)"
        >
          <div class="card-image-wrapper">
            <img 
              :src="getCoverUrl(item.cover)" 
              alt="" 
              class="card-image"
            >
            <div class="image-overlay"></div>
            <div class="play-icon">▶</div>
            <div class="duration-badge">{{ item.duration || '00:00' }}</div>
            <div class="view-count-badge">
              <el-icon><VideoPlay /></el-icon>
              {{ formatViewCount(item.viewCount) }}
            </div>
          </div>
          <div class="card-content">
            <h3 class="card-title line1">{{ item.title }}</h3>
            <div class="card-meta">
              <span class="meta-item time">
                <el-icon><Clock /></el-icon>
                {{ item.createTime }}
              </span>
            </div>
            <div class="card-actions">
              <div class="action-item" @click.stop="toggleLike(item)">
                <img :src="likeIcon" alt="" class="action-icon" :style="{ filter: item.liked ? 'none' : 'grayscale(100%)', opacity: item.liked ? 1 : 0.5 }">
                <span :style="{ color: item.liked ? '#409EFF' : '' }">{{ item.likeCount || 0 }}</span>
              </div>
              <div class="action-item" @click.stop="toggleCollect(item)">
                <img :src="collectIcon" alt="" class="action-icon" :style="{ filter: item.collected ? 'none' : 'grayscale(100%)', opacity: item.collected ? 1 : 0.5 }">
                <span :style="{ color: item.collected ? '#F56C6C' : '' }">{{ item.collectCount || 0 }}</span>
              </div>
              <div class="action-item" @click.stop>
                <img :src="commentIcon" alt="" class="action-icon">
                <span>{{ item.commentCount || 0 }}</span>
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
import {VideoPlay, Filter} from '@element-plus/icons-vue'
import likeIcon from '@/assets/images/点赞.png';
import collectIcon from '@/assets/images/收藏.png';
import commentIcon from '@/assets/images/评论.png';

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
  videoData: [],
  filterVisible: false,
  hasFilter: false
})

// 格式化播放次数显示
const formatViewCount = (count) => {
  if (!count) return '0'
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toString()
}

// 加载视频分类
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

// 切换点赞状态
const toggleLike = (item) => {
  request.post('/likes/add', {
    userId: data.user.id,
    videoId: item.id,
    userName: data.user.name,
    videoTitle: item.title
  }).then(res => {
    if (res.code === '200') {
      load()
    }
  })
}

// 切换收藏状态
const toggleCollect = (item) => {
  request.post('/collect/add', {
    userId: data.user.id,
    videoId: item.id,
    userName: data.user.name,
    videoTitle: item.title
  }).then(res => {
    if (res.code === '200') {
      load()
    }
  })
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

// 加载视频列表
const load = () => {
  request.get('/video/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      categoryId: data.categoryId,
    }
  }).then(res => {
    console.log('Video response:', res)
    if (res.code === '200') {
      console.log('Records:', res.data?.records)
      console.log('Total:', res.data?.total)
      let videos = res.data?.records || []
      data.total = res.data?.total || 0
      console.log('data.total set to:', data.total)
      
      const videoIds = videos.map(v => v.id)
      if (videoIds.length > 0) {
        // 查询当前用户的点赞记录
        request.get('/likes/selectAll', {
          params: { userId: data.user.id }
        }).then(likeRes => {
          if (likeRes.code === '200') {
            const userLikes = likeRes.data || []
            const likedVideoIds = new Set(userLikes.filter(l => l.videoId).map(l => l.videoId))
            
            // 查询当前用户的收藏记录
            request.get('/collect/selectAll', {
              params: { userId: data.user.id }
            }).then(collectRes => {
              if (collectRes.code === '200') {
                const userCollects = collectRes.data || []
                const collectedVideoIds = new Set(userCollects.filter(c => c.videoId).map(c => c.videoId))
                
                // 查询每个视频的点赞数、收藏数、评论数
                const likePromises = videoIds.map(videoId => 
                  request.get('/likes/selectAll', { params: { videoId } })
                )
                const collectPromises = videoIds.map(videoId => 
                  request.get('/collect/selectAll', { params: { videoId } })
                )
                const commentPromises = videoIds.map(videoId => 
                  request.get('/comment/selectAll', { params: { videoId } })
                )
                
                Promise.all([...likePromises, ...collectPromises, ...commentPromises]).then(results => {
                  const likeResults = results.slice(0, videoIds.length)
                  const collectResults = results.slice(videoIds.length, videoIds.length * 2)
                  const commentResults = results.slice(videoIds.length * 2)
                  
                  videos.forEach((video, index) => {
                    video.liked = likedVideoIds.has(video.id)
                    video.collected = collectedVideoIds.has(video.id)
                    video.likeCount = likeResults[index].code === '200' ? (likeResults[index].data?.length || 0) : 0
                    video.collectCount = collectResults[index].code === '200' ? (collectResults[index].data?.length || 0) : 0
                    video.commentCount = commentResults[index].code === '200' ? (commentResults[index].data?.length || 0) : 0
                  })
                  
                  data.videoData = videos
                })
              }
            })
          }
        })
      } else {
        data.videoData = videos
      }
    } else {
      ElMessage.error(res.msg)
    }
  })
}

load()
</script>

<style scoped>
/* 全局样式 */
.video-page {
  background: 
    url('https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=1920&q=80') center/cover,
    linear-gradient(135deg, rgba(255, 195, 18, 0.15) 0%, rgba(255, 159, 67, 0.15) 100%);
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 0;
}

.video-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 25% 35%, rgba(255, 195, 18, 0.2) 0%, transparent 50%),
    radial-gradient(circle at 75% 65%, rgba(255, 159, 67, 0.2) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(255, 107, 107, 0.1) 0%, transparent 50%);
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
.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
}

/* 卡片样式 */
.video-card {
  background: 
    linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 248, 245, 0.98) 100%);
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

.video-card:hover {
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

.video-card:hover .card-image {
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

.video-card:hover .image-overlay {
  opacity: 1;
}

.play-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #667eea;
  opacity: 0;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.video-card:hover .play-icon {
  opacity: 1;
}

.duration-badge {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.view-count-badge {
  position: absolute;
  bottom: 10px;
  left: 10px;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-content {
  padding: 15px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
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
  margin-bottom: 10px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-actions {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 13px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-item:hover {
  color: #667eea;
}

.action-icon {
  width: 16px;
  height: 16px;
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
  .video-grid {
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
