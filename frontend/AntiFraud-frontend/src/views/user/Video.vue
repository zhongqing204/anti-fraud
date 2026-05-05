<template>
  <div>
    <div style="background-color: #f8f8f8; padding: 20px">
      <div style="width: 60%; margin: 0 auto; display: flex; align-items: center; gap: 15px">
        <el-input prefix-icon="Search" v-model="data.title" @keyup.enter="load" clearable @clear="load" placeholder="请输入视频标题查询" style="width: 350px; height: 40px"></el-input>
        <el-popover trigger="manual" :width="300" v-model:visible="data.filterVisible">
          <template #reference>
            <el-button :type="data.hasFilter ? 'primary' : ''" @click="data.filterVisible = !data.filterVisible">
              <el-icon><Filter /></el-icon>
              筛选
            </el-button>
          </template>
          <div style="padding: 10px" @click.stop>
            <div style="margin-bottom: 15px">
              <div style="font-weight: bold; margin-bottom: 8px">分类</div>
              <el-select v-model="data.categoryId" placeholder="请选择分类" clearable style="width: 100%" :teleported="false">
                <el-option label="全部" :value="undefined" />
                <el-option v-for="item in data.categoryData" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </div>
            <div style="margin-bottom: 15px">
              <div style="font-weight: bold; margin-bottom: 8px">时间</div>
              <el-select v-model="data.timeFilter" placeholder="请选择时间范围" clearable style="width: 100%" :teleported="false">
                <el-option label="全部" :value="undefined" />
                <el-option label="最近一周" value="week" />
                <el-option label="最近一月" value="month" />
                <el-option label="最近三月" value="threeMonths" />
                <el-option label="最近一年" value="year" />
              </el-select>
            </div>
            <div style="display: flex; gap: 10px; justify-content: flex-end">
              <el-button size="small" @click="resetFilter">重置</el-button>
              <el-button type="primary" size="small" @click="applyFilter">确定</el-button>
            </div>
          </div>
        </el-popover>
      </div>
    </div>
    <div style="width: 60%; margin: 30px auto">
      <div>
         <el-row :gutter="20">
          <el-col :span="6" v-for="item in data.videoData" :key="item.id" style="margin-bottom: 20px">
            <div class="front_card">
              <div>
                <div style="position: relative">
                  <img :src="getCoverUrl(item.cover)" alt="" style="height: 180px; width: 100%; border-radius: 5px; cursor: pointer" @click="router.push('/front/videoDetail?id=' + item.id)">
                  <div style="position: absolute; bottom: 8px; right: 8px; background: rgba(0,0,0,0.7); color: white; padding: 2px 8px; border-radius: 3px; font-size: 12px">
                    {{ item.duration || '00:00' }}
                  </div>
                  <div style="position: absolute; bottom: 8px; left: 8px; background: rgba(0,0,0,0.7); color: white; padding: 2px 8px; border-radius: 3px; font-size: 12px; display: flex; align-items: center; gap: 5px">
                    <el-icon><VideoPlay /></el-icon>
                    {{ formatViewCount(item.viewCount) }}
                  </div>
                </div>
                <div style="padding: 10px">
                  <div style="font-size: 16px; font-weight: bold" class="line1">{{ item.title }}</div>
                  <div style="margin-top: 10px; color: #666; font-size: 13px">
                    <span>{{ item.createTime }}</span>
                  </div>
                  <div style="margin-top: 8px; display: flex; grid-gap: 15px; color: #999; font-size: 12px">
                    <div style="display: flex; align-items: center; gap: 3px; cursor: pointer" @click.stop="toggleLike(item)">
                      <img :src="likeIcon" alt="" style="width: 16px; height: 16px" :style="{ filter: item.liked ? 'none' : 'grayscale(100%)', opacity: item.liked ? 1 : 0.5 }">
                      <span :style="{ color: item.liked ? '#409EFF' : '' }">{{ item.likeCount || 0 }}</span>
                    </div>
                    <div style="display: flex; align-items: center; gap: 3px; cursor: pointer" @click.stop="toggleCollect(item)">
                      <img :src="collectIcon" alt="" style="width: 16px; height: 16px" :style="{ filter: item.collected ? 'none' : 'grayscale(100%)', opacity: item.collected ? 1 : 0.5 }">
                      <span :style="{ color: item.collected ? '#F56C6C' : '' }">{{ item.collectCount || 0 }}</span>
                    </div>
                    <div style="display: flex; align-items: center; gap: 3px; cursor: pointer" @click.stop="router.push('/front/videoDetail?id=' + item.id)">
                      <img :src="commentIcon" alt="" style="width: 16px; height: 16px">
                      <span>{{ item.commentCount || 0 }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      <div v-if="data.total" style="margin-top: 20px">
        <el-pagination @current-change="load" layout="total, prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
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
  pageSize: 8,
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
    if (res.code === '200') {
      let videos = res.data?.records || []
      data.total = res.data?.total || 0
      
      // 时间筛选（先筛选，再查询统计数据）
      if (data.timeFilter) {
        const now = new Date()
        videos = videos.filter(item => {
          const createTime = new Date(item.createTime)
          const diffDays = (now - createTime) / (1000 * 60 * 60 * 24)
          
          switch(data.timeFilter) {
            case 'week':
              return diffDays <= 7
            case 'month':
              return diffDays <= 30
            case 'threeMonths':
              return diffDays <= 90
            case 'year':
              return diffDays <= 365
            default:
              return true
          }
        })
      }
      
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
.line1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.front_card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.3s;
}

.front_card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}
</style>
