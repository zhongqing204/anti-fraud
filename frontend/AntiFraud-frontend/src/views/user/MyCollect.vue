<template>
  <div style="background: #f5f7fa; min-height: 100vh; padding-bottom: 50px">
    <div style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); padding: 30px 0; margin-bottom: 30px">
      <div style="width: 60%; margin: 0 auto">
        <div style="color: white; font-size: 32px; font-weight: bold; margin-bottom: 10px"> 
          <el-icon style="vertical-align: middle; margin-right: 10px"><Star /></el-icon>
          我的收藏
        </div>
        <div style="color: rgba(255,255,255,0.9); font-size: 16px">查看您收藏的内容</div>
      </div>
    </div>

    <div style="width: 60%; margin: 0 auto">
      <div style="margin-bottom: 20px; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.08)">
        <el-radio-group v-model="data.activeTab" @change="handleTabChange" size="large">
          <el-radio-button label="article">反诈论坛</el-radio-button>
          <el-radio-button label="video">反诈视频</el-radio-button>
          <el-radio-button label="publicity">反诈宣传</el-radio-button>
          <el-radio-button label="activity">反诈活动</el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="data.activeTab === 'article'">
        <div v-for="item in data.tableData" :key="item.id" style="margin-bottom: 15px; background: white; border-radius: 10px; padding: 25px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); transition: all 0.3s; cursor: pointer" @mouseenter="$event.currentTarget.style.boxShadow='0 4px 16px rgba(0,0,0,0.12)'" @mouseleave="$event.currentTarget.style.boxShadow='0 2px 8px rgba(0,0,0,0.08)'" @click="goToDetail(item)">
          <div style="display: flex; align-items: center; margin-bottom: 15px">
            <img :src="getAvatarUrl(item.userAvatar)" alt="" style="height: 30px; width: 30px; border-radius: 50%; object-fit: cover">
            <div style="margin-left: 10px;">
              <div style="color: #666">{{ item.userName }}</div>
              <div style="color: #999; font-size: 12px; margin-top: 3px">{{ formatTime(item.time) }}</div>
            </div>
          </div>
          <div style="font-size: 18px; font-weight: bold; margin-bottom: 10px">{{ item.title }}</div>
          <div class="article-content-preview">{{ stripHtml(item.content) }}</div>
          <div style="margin-top: 15px; display: flex; gap: 40px; color: #999; font-size: 14px">
            <div style="display: flex; align-items: center; gap: 5px">
              <img src="@/assets/images/点赞.png" alt="点赞" style="width: 16px; height: 16px">
              <span>{{ item.likeCount || 0 }}</span>
            </div>
            <div style="display: flex; align-items: center; gap: 5px">
              <img src="@/assets/images/收藏.png" alt="收藏" style="width: 16px; height: 16px">
              <span style="color: #F56C6C">{{ item.collectCount || 0 }}</span>
            </div>
            <div style="display: flex; align-items: center; gap: 5px">
              <img src="@/assets/images/评论.png" alt="评论" style="width: 16px; height: 16px">
              <span>{{ item.commentCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else-if="data.activeTab === 'video'">
        <el-row :gutter="20">
          <el-col :span="6" v-for="item in data.tableData" :key="item.id" style="margin-bottom: 20px">
            <div class="video-card" @click="goToDetail(item)">
              <div style="position: relative">
                <img :src="getCoverUrl(item.cover)" alt="" style="height: 180px; width: 100%; object-fit: cover">
                <div style="position: absolute; bottom: 8px; right: 8px; background: rgba(0,0,0,0.7); color: white; padding: 2px 8px; border-radius: 3px; font-size: 12px">
                  {{ item.duration || '00:00' }}
                </div>
                <div style="position: absolute; bottom: 8px; left: 8px; background: rgba(0,0,0,0.7); color: white; padding: 2px 8px; border-radius: 3px; font-size: 12px; display: flex; align-items: center; gap: 5px">
                  <el-icon><VideoPlay /></el-icon>
                  {{ formatViewCount(item.viewCount) }}
                </div>
              </div>
              <div style="padding: 15px">
                <div style="font-size: 16px; font-weight: bold; margin-bottom: 10px" class="line1">{{ item.title }}</div>
                <div style="color: #999; font-size: 12px; margin-bottom: 8px">{{ item.createTime }}</div>
                <div style="display: flex; gap: 15px; color: #999; font-size: 12px">
                  <div style="display: flex; align-items: center; gap: 3px">
                    <img src="@/assets/images/点赞.png" alt="" style="width: 16px; height: 16px">
                    <span>{{ item.likeCount || 0 }}</span>
                  </div>
                  <div style="display: flex; align-items: center; gap: 3px">
                    <img src="@/assets/images/收藏.png" alt="" style="width: 16px; height: 16px">
                    <span>{{ item.collectCount || 0 }}</span>
                  </div>
                  <div style="display: flex; align-items: center; gap: 3px">
                    <img src="@/assets/images/评论.png" alt="" style="width: 16px; height: 16px">
                    <span>{{ item.commentCount || 0 }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <div v-else-if="data.activeTab === 'publicity'">
        <el-row :gutter="20">
          <el-col :span="6" v-for="item in data.tableData" :key="item.id" style="margin-bottom: 20px">
            <div class="publicity-card" @click="goToDetail(item)">
              <img :src="getCoverUrl(item.cover)" alt="" style="height: 180px; width: 100%; object-fit: cover; border-radius: 10px 10px 0 0">
              <div style="padding: 15px">
                <div style="font-size: 16px; font-weight: bold; margin-bottom: 10px" class="line1">{{ item.title }}</div>
                <div style="color: #999; font-size: 12px; display: flex; align-items: center; gap: 5px">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(item.createTime) }}
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <div v-else-if="data.activeTab === 'activity'">
        <div v-for="item in data.tableData" :key="item.id" style="margin-bottom: 15px; background: white; border-radius: 10px; padding: 25px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); transition: all 0.3s; cursor: pointer" @mouseenter="$event.currentTarget.style.boxShadow='0 4px 16px rgba(0,0,0,0.12)'" @mouseleave="$event.currentTarget.style.boxShadow='0 2px 8px rgba(0,0,0,0.08)'" @click="goToDetail(item)">
          <div style="display: flex; align-items: center">
            <img :src="getCoverUrl(item.cover)" alt="" style="height: 120px; width: 180px; border-radius: 5px; object-fit: cover; margin-right: 20px; flex-shrink: 0">
            <div style="flex: 1">
              <div style="font-size: 18px; font-weight: bold; margin-bottom: 10px">{{ item.title }}</div>
              <div class="article-content-preview">{{ stripHtml(item.content) }}</div>
              <div style="margin-top: 10px; color: #999; font-size: 12px">
                <span>{{ formatTime(item.startTime) }} 至 {{ formatTime(item.endTime) }}</span>
                <el-tag size="small" :type="item.status === '进行中' ? 'success' : 'info'" style="margin-left: 10px">{{ item.status }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="data.total" style="margin-top: 30px; background: white; padding: 20px; border-radius: 10px; text-align: center">
        <el-pagination @current-change="load" layout="total, prev, pager, next, jumper" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
      </div>
      <div v-else style="text-align: center; padding: 80px; color: #999; background: white; border-radius: 10px">
        <el-icon :size="48" style="margin-bottom: 20px"><Star /></el-icon>
        <div style="font-size: 18px">暂无收藏记录</div>
        <div style="font-size: 14px; margin-top: 10px; color: #ccc">收藏您感兴趣的内容吧</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import {VideoPlay, Star, Clock} from '@element-plus/icons-vue'

const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  activeTab: 'article',
  pageNum: 1,
  pageSize: 8,
  tableData: [],
  total: 0,
  collectArticleIds: [],
  collectVideoIds: [],
  collectPublicityIds: [],
  collectActivityIds: []
})

const getAvatarUrl = (avatar) => {
  if (!avatar) return '/default-avatar.png'
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  return baseUrl + avatar
}

const getCoverUrl = (cover) => {
  if (!cover) return ''
  if (cover.startsWith('http://') || cover.startsWith('https://')) {
    return cover
  }
  return baseUrl + cover
}

const stripHtml = (html) => {
  const tmp = document.createElement('div')
  tmp.innerHTML = html
  return tmp.textContent || tmp.innerText || ''
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

const formatViewCount = (count) => {
  if (!count) return '0'
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toString()
}

const goToDetail = (item) => {
  if (data.activeTab === 'article') {
    router.push('/front/articleDetail?id=' + item.id)
  } else if (data.activeTab === 'video') {
    router.push('/front/videoDetail?id=' + item.id)
  } else if (data.activeTab === 'publicity') {
    router.push('/front/publicityDetail?id=' + item.id)
  } else if (data.activeTab === 'activity') {
    router.push('/front/activityDetail?id=' + item.id)
  }
}

const handleTabChange = () => {
  data.pageNum = 1
  load()
}

const load = () => {
  if (data.activeTab === 'article') {
    loadArticleCollects()
  } else if (data.activeTab === 'video') {
    loadVideoCollects()
  } else if (data.activeTab === 'publicity') {
    loadPublicityCollects()
  } else if (data.activeTab === 'activity') {
    loadActivityCollects()
  }
}

const loadArticleCollects = () => {
  request.get('/collect/selectAll', {
    params: { userId: data.user.id }
  }).then(res => {
    if (res.code === '200') {
      const collects = res.data || []
      data.collectArticleIds = collects.filter(c => c.articleId).map(c => c.articleId)
      
      if (data.collectArticleIds.length === 0) {
        data.tableData = []
        data.total = 0
        return
      }
      
      loadArticlesByPage()
    }
  })
}

const loadVideoCollects = () => {
  request.get('/collect/selectAll', {
    params: { userId: data.user.id }
  }).then(res => {
    if (res.code === '200') {
      const collects = res.data || []
      data.collectVideoIds = collects.filter(c => c.videoId).map(c => c.videoId)
      
      if (data.collectVideoIds.length === 0) {
        data.tableData = []
        data.total = 0
        return
      }
      
      loadVideosByPage()
    }
  })
}

const loadPublicityCollects = () => {
  request.get('/collect/selectAll', {
    params: { userId: data.user.id }
  }).then(res => {
    if (res.code === '200') {
      const collects = res.data || []
      data.collectPublicityIds = collects.filter(c => c.publicityId).map(c => c.publicityId)
      
      if (data.collectPublicityIds.length === 0) {
        data.tableData = []
        data.total = 0
        return
      }
      
      loadPublicitiesByPage()
    }
  })
}

const loadActivityCollects = () => {
  request.get('/collect/selectAll', {
    params: { userId: data.user.id }
  }).then(res => {
    if (res.code === '200') {
      const collects = res.data || []
      data.collectActivityIds = collects.filter(c => c.activityId).map(c => c.activityId)
      
      if (data.collectActivityIds.length === 0) {
        data.tableData = []
        data.total = 0
        return
      }
      
      loadActivitiesByPage()
    }
  })
}

const loadArticlesByPage = () => {
  const start = (data.pageNum - 1) * data.pageSize
  const end = start + data.pageSize
  const pageArticleIds = data.collectArticleIds.slice(start, end)
  
  if (pageArticleIds.length === 0) {
    data.tableData = []
    return
  }
  
  const promises = pageArticleIds.map(id => request.get('/article/selectById/' + id))
  
  Promise.all(promises).then(results => {
    data.tableData = results.filter(res => res.code === '200' && res.data).map(res => res.data)
    data.total = data.collectArticleIds.length
  })
}

const loadVideosByPage = () => {
  const start = (data.pageNum - 1) * data.pageSize
  const end = start + data.pageSize
  const pageVideoIds = data.collectVideoIds.slice(start, end)
  
  if (pageVideoIds.length === 0) {
    data.tableData = []
    return
  }
  
  const promises = pageVideoIds.map(id => request.get('/video/selectById/' + id))
  
  Promise.all(promises).then(results => {
    const videos = results.filter(res => res.code === '200' && res.data).map(res => res.data)
    
    const countPromises = videos.map(video => {
      return Promise.all([
        request.get('/likes/selectAll', { params: { videoId: video.id } }),
        request.get('/collect/selectAll', { params: { videoId: video.id } }),
        request.get('/comment/selectAll', { params: { videoId: video.id } })
      ]).then(([likeRes, collectRes, commentRes]) => {
        video.likeCount = likeRes.code === '200' ? (likeRes.data?.length || 0) : 0
        video.collectCount = collectRes.code === '200' ? (collectRes.data?.length || 0) : 0
        video.commentCount = commentRes.code === '200' ? (commentRes.data?.length || 0) : 0
        return video
      })
    })
    
    Promise.all(countPromises).then(videosWithCounts => {
      data.tableData = videosWithCounts
      data.total = data.collectVideoIds.length
    })
  })
}

const loadPublicitiesByPage = () => {
  const start = (data.pageNum - 1) * data.pageSize
  const end = start + data.pageSize
  const pagePublicityIds = data.collectPublicityIds.slice(start, end)
  
  if (pagePublicityIds.length === 0) {
    data.tableData = []
    return
  }
  
  const promises = pagePublicityIds.map(id => request.get('/publicity/selectById/' + id))
  
  Promise.all(promises).then(results => {
    data.tableData = results.filter(res => res.code === '200' && res.data).map(res => res.data)
    data.total = data.collectPublicityIds.length
  })
}

const loadActivitiesByPage = () => {
  const start = (data.pageNum - 1) * data.pageSize
  const end = start + data.pageSize
  const pageActivityIds = data.collectActivityIds.slice(start, end)
  
  if (pageActivityIds.length === 0) {
    data.tableData = []
    return
  }
  
  const promises = pageActivityIds.map(id => request.get('/activity/selectById/' + id))
  
  Promise.all(promises).then(results => {
    data.tableData = results.filter(res => res.code === '200' && res.data).map(res => res.data)
    data.total = data.collectActivityIds.length
  })
}

load()
</script>

<style scoped>
.article-content-preview {
  color: #666;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  line-clamp: 4;
  -webkit-box-orient: vertical;
  word-break: break-all;
}

.line1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.publicity-card {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
}

.publicity-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.video-card {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
}

.video-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}
</style>
