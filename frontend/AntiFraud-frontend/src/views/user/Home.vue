<template>
  <div class="home-page">
    <!-- 轮播图区域 -->
    <div class="carousel-section">
      <el-carousel height="500px" :interval="5000" indicator-position="outside" arrow="always">
        <el-carousel-item v-for="item in data.carouselData" :key="item.id">
          <img 
            :src="baseUrl + item.image" 
            alt="" 
            class="carousel-image"
          >
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 反诈举报中心入口卡片 -->
      <div class="report-center-card">
        <div class="report-center-content">
          <div class="report-icon">🛡️</div>
          <div class="report-text">
            <h2 class="report-title">反诈举报中心</h2>
            <p class="report-subtitle">发现诈骗线索?立即举报,共同守护财产安全</p>
          </div>
          <el-button type="primary" class="report-btn" @click="router.push('/front/report')">
            <span>点击进入</span>
            <span class="btn-arrow">→</span>
          </el-button>
        </div>
        <div class="card-decoration"></div>
      </div>

      <!-- 内容网格布局 -->
      <div class="content-grid">
        <!-- 左侧栏 -->
        <div class="left-column">
          <!-- 反诈活动 -->
          <section class="content-section fade-in">
            <div class="section-header">
              <h3 class="section-title">
                <span class="title-icon">🎯</span>
                反诈活动
              </h3>
              <div class="more-link" @click="router.push('/front/activity')">
                更多 <span class="arrow">›</span>
              </div>
            </div>
            <div class="activity-list">
              <div 
                v-for="item in data.activityData" 
                :key="item.id"
                class="activity-card"
                @click="router.push('/front/activityDetail?id=' + item.id)"
              >
                <div class="activity-image-wrapper">
                  <img :src="getActivityCover(item.cover)" alt="" class="activity-image">
                  <div class="image-overlay"></div>
                </div>
                <div class="activity-info">
                  <h4 class="activity-title line1">{{ item.title }}</h4>
                </div>
              </div>
            </div>
          </section>

          <!-- 反诈热帖 -->
          <section class="content-section hot-posts-section fade-in">
            <div class="section-header">
              <h3 class="section-title">
                <span class="title-icon">🔥</span>
                反诈热帖
              </h3>
              <div class="more-link" @click="router.push('/front/article')">
                更多 <span class="arrow">›</span>
              </div>
            </div>
            <div class="article-list">
              <div 
                v-for="(item, index) in data.articleData" 
                :key="item.id"
                class="article-item"
                @click="router.push('/front/articleDetail?id=' + item.id)"
              >
                <span class="article-index">{{ index + 1 }}</span>
                <span class="article-title">{{ item.title }}</span>
              </div>
            </div>
          </section>
        </div>

        <!-- 右侧栏 -->
        <div class="right-column">
          <!-- 反诈宣传 -->
          <section class="content-section fade-in">
            <div class="section-header">
              <h3 class="section-title">
                <span class="title-icon">📢</span>
                反诈宣传
              </h3>
              <div class="more-link" @click="router.push('/front/publicity')">
                更多 <span class="arrow">›</span>
              </div>
            </div>
            <div class="publicity-grid">
              <div 
                v-for="item in data.publicityData" 
                :key="item.id"
                class="publicity-card"
                @click="router.push('/front/publicityDetail?id=' + item.id)"
              >
                <div class="publicity-image-wrapper">
                  <img :src="getCoverUrl(item.cover)" alt="" class="publicity-image">
                  <div class="image-overlay"></div>
                </div>
                <div class="publicity-info">
                  <h4 class="publicity-title line1">{{ item.title }}</h4>
                  <div class="publicity-meta">
                    <span class="meta-item">
                      <el-icon><Clock /></el-icon>
                      {{ formatTime(item.createTime) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- 反诈视频 -->
          <section class="content-section fade-in">
            <div class="section-header">
              <h3 class="section-title">
                <span class="title-icon">🎬</span>
                反诈视频
              </h3>
              <div class="more-link" @click="router.push('/front/video')">
                更多 <span class="arrow">›</span>
              </div>
            </div>
            <div class="video-grid">
              <div 
                v-for="item in data.videoData" 
                :key="item.id"
                class="video-card"
                @click="router.push('/front/videoDetail?id=' + item.id)"
              >
                <div class="video-image-wrapper">
                  <img :src="getCoverUrl(item.cover)" alt="" class="video-image">
                  <div class="play-icon">▶</div>
                  <div class="image-overlay"></div>
                </div>
                <div class="video-info">
                  <h4 class="video-title line1">{{ item.title }}</h4>
                  <div class="video-meta">
                    <span class="meta-item">
                      <el-icon><Clock /></el-icon>
                      {{ formatTime(item.createTime) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const baseUrl = import.meta.env.VITE_BASE_URL || 'http://localhost:8080'

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  carouselData: [],
  articleData: [],
  activityData: [],
  publicityData: [],
  videoData: [],
})

const loadCarousel = () => {
  request.get('/carousel/selectAll').then(res => {
    if (res.code === '200') {
      data.carouselData = res.data || []
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadCarousel()

const getCoverUrl = (cover) => {
  if (!cover) return ''
  if (cover.startsWith('http://') || cover.startsWith('https://')) {
    return cover
  }
  return baseUrl + cover
}

const getActivityCover = (cover) => {
  if (!cover) return ''
  if (cover.startsWith('http://') || cover.startsWith('https://')) {
    return cover
  }
  return baseUrl + cover
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const loadArticle = () => {
  request.get('/article/selectTop10').then(res => {
    if (res.code === '200') {
      data.articleData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadArticle()

const loadActivity = () => {
  request.get('/activity/selectTop4').then(res => {
    if (res.code === '200') {
      data.activityData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadActivity()

const loadPublicity = () => {
  request.get('/publicity/selectTop4').then(res => {
    if (res.code === '200') {
      data.publicityData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadPublicity()

const loadVideo = () => {
  request.get('/video/selectTop4').then(res => {
    if (res.code === '200') {
      data.videoData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadVideo()

</script>

<style scoped>
/* 全局样式 */
.home-page {
  background: 
    url('https://images.unsplash.com/photo-1557683316-973673baf926?w=1920&q=80') center/cover,
    linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 1px;
}

.home-page::before {
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

/* 轮播图区域 */
.carousel-section {
  position: relative;
  overflow: hidden;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.carousel-section :deep(.el-carousel__item) {
  overflow: hidden;
}

.carousel-section :deep(.el-carousel__item:hover .carousel-image) {
  transform: scale(1.05);
}

/* 主内容区域 */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* 反诈举报中心卡片 */
.report-center-card {
  background: 
    linear-gradient(135deg, rgba(102, 126, 234, 0.95) 0%, rgba(118, 75, 162, 0.95) 100%),
    url('https://images.unsplash.com/photo-1563986768609-322da13575f3?w=800&q=80') center/cover;
  border-radius: 20px;
  padding: 40px;
  margin-bottom: 40px;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.4);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  animation: slideUp 0.6s ease-out;
  backdrop-filter: blur(10px);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.report-center-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 50px rgba(102, 126, 234, 0.4);
}

.card-decoration {
  position: absolute;
  top: -50%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: 
    radial-gradient(circle, rgba(255,255,255,0.2) 0%, transparent 70%),
    url('https://images.unsplash.com/photo-1558591710-4b4a1ae0f04d?w=400&q=80') center/cover;
  border-radius: 50%;
  opacity: 0.3;
}

.report-center-content {
  display: flex;
  align-items: center;
  gap: 30px;
  position: relative;
  z-index: 1;
}

.report-icon {
  font-size: 60px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.report-text {
  flex: 1;
}

.report-title {
  color: white;
  font-size: 28px;
  font-weight: bold;
  margin: 0 0 8px 0;
}

.report-subtitle {
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
  margin: 0;
}

.report-btn {
  padding: 15px 35px;
  font-size: 16px;
  border-radius: 30px;
  background: white;
  color: #667eea;
  border: none;
  font-weight: bold;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.report-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.2);
}

.btn-arrow {
  transition: transform 0.3s ease;
}

.report-btn:hover .btn-arrow {
  transform: translateX(5px);
}

/* 内容网格 */
.content-grid {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 30px;
}

.left-column,
.right-column {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* 内容区块 */
.content-section {
  background: 
    linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 249, 250, 0.95) 100%);
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.content-section:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 24px;
}

.more-link {
  color: #666;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 4px;
}

.more-link:hover {
  color: #667eea;
}

.arrow {
  transition: transform 0.3s ease;
}

.more-link:hover .arrow {
  transform: translateX(3px);
}

/* 活动卡片 */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.activity-card {
  border-radius: 12px;
  overflow: hidden;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.activity-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.activity-image-wrapper {
  position: relative;
  overflow: hidden;
}

.activity-image {
  width: 100%;
  height: 180px;
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
  background: linear-gradient(to bottom, transparent 60%, rgba(0,0,0,0.3));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.activity-card:hover .image-overlay {
  opacity: 1;
}

.activity-info {
  padding: 12px;
}

.activity-title {
  font-size: 15px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

/* 文章列表 */
.article-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.article-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.3s ease;
}

.article-item:hover {
  background: #e8ecf1;
  transform: translateX(5px);
}

.article-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.article-title {
  font-size: 14px;
  color: #333;
  flex: 1;
}

/* 宣传和视频网格 */
.publicity-grid,
.video-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.publicity-card,
.video-card {
  border-radius: 12px;
  overflow: hidden;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.publicity-card:hover,
.video-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.publicity-image-wrapper,
.video-image-wrapper {
  position: relative;
  overflow: hidden;
}

.publicity-image,
.video-image {
  width: 100%;
  height: 230px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.publicity-card:hover .publicity-image,
.video-card:hover .video-image {
  transform: scale(1.05);
}

.play-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #667eea;
  opacity: 0;
  transition: all 0.3s ease;
}

.video-card:hover .play-icon {
  opacity: 1;
}

.publicity-info,
.video-info {
  padding: 12px;
}

.publicity-title,
.video-title {
  font-size: 15px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
}

.publicity-meta,
.video-meta {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 渐入动画 */
.fade-in {
  animation: fadeIn 0.6s ease-out;
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
@media (max-width: 1200px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .left-column {
    order: 2;
  }
  
  .right-column {
    order: 1;
  }
}
</style>