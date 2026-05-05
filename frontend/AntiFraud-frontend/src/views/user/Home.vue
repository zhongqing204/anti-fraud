<template>
  <div>
    <div style="display: flex; justify-content: center; align-items: center; background-color: #f7f8fa;">
      <img src="/src/assets/images/首页宣传.png" alt="" style="height: 400px; max-width: 1200px; width: 100%; object-fit: cover; cursor: pointer">
    </div>
    <div style="width: 60%; margin: 5px auto">
      <div style="background-color: #f7f8fa; display: flex; align-items: center; justify-content: center; grid-gap: 20px; padding: 50px 0">
        <div style="font-size: 24px">反诈举报中心</div>
        <div>
          <el-button type="primary" style="font-size: 16px; padding: 20px 30px" @click="router.push('/front/report')">点击进入</el-button>
        </div>
      </div>
      <div style="margin-top: 30px; display: flex; grid-gap: 30px">
        <div style="width: 300px">
          <!-- 反诈活动：显示封面和标题 -->
          <div style="display: flex; align-items: center">
            <div style="flex: 1; font-size: 20px">反诈活动</div>
            <div style="width: 80px; text-align: right; cursor: pointer; color: #666666" @click="router.push('/front/activity')">更多 ></div>
          </div>
          <div style="margin-top: 20px">
            <el-row :gutter="20">
              <el-col :span="24" v-for="item in data.activityData" style="margin-bottom: 10px">
                <div class="front_card">
                  <div>
                    <img :src="getActivityCover(item.cover)" alt="" style="height: 180px; width: 100%; border-radius: 5px; cursor: pointer" @click="router.push('/front/activityDetail?id=' + item.id)">
                    <div style="padding: 10px">
                      <div style="font-size: 16px; font-weight: bold" class="line1">{{ item.title }}</div>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
          <!-- 反诈热帖：只显示标题 -->
          <div style="display: flex; align-items: center; margin-top: 10px">
            <div style="flex: 1; font-size: 20px">反诈热帖</div>
            <div style="width: 80px; text-align: right; cursor: pointer; color: #666666" @click="router.push('/front/article')">更多 ></div>
          </div>
          <div style="margin-top: 20px">
            <div v-for="item in data.articleData" style="margin-bottom: 10px; font-size: 15px; cursor: pointer" @click="router.push('/front/articleDetail?id=' + item.id)">{{ item.title }}</div>
          </div>
        </div>
        <div style="flex: 1">
          <div style="display: flex; align-items: center">
            <div style="flex: 1; font-size: 20px">反诈宣传</div>
            <div style="width: 80px; text-align: right; cursor: pointer; color: #666666" @click="router.push('/front/publicity')">更多 ></div>
          </div>
          <div style="margin-top: 20px">
            <el-row :gutter="20">
              <el-col :span="12" v-for="item in data.publicityData" style="margin-bottom: 20px">
                <div class="front_card">
                  <div>
                    <img :src="getCoverUrl(item.cover)" alt="" style="height: 230px; width: 100%; border-radius: 5px; cursor: pointer" @click="router.push('/front/publicityDetail?id=' + item.id)">
                    <div style="padding: 10px">
                      <div style="font-size: 16px; font-weight: bold" class="line1">{{ item.title }}</div>
                      <div style="margin-top: 15px; display: flex; grid-gap: 20px">
                        <div style="color: #666666; display: flex; grid-gap: 5px; align-items: center">
                          <el-icon size="18"><Clock /></el-icon>
                          <div>{{ formatTime(item.createTime) }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
          
          <!-- 反诈视频 -->
          <div style="display: flex; align-items: center; margin-top: 30px">
            <div style="flex: 1; font-size: 20px">反诈视频</div>
            <div style="width: 80px; text-align: right; cursor: pointer; color: #666666" @click="router.push('/front/video')">更多 ></div>
          </div>
          <div style="margin-top: 20px">
            <el-row :gutter="20">
              <el-col :span="12" v-for="item in data.videoData" style="margin-bottom: 20px">
                <div class="front_card">
                  <div>
                    <img :src="getCoverUrl(item.cover)" alt="" style="height: 230px; width: 100%; border-radius: 5px; cursor: pointer" @click="router.push('/front/videoDetail?id=' + item.id)">
                    <div style="padding: 10px">
                      <div style="font-size: 16px; font-weight: bold" class="line1">{{ item.title }}</div>
                      <div style="margin-top: 15px; display: flex; grid-gap: 20px">
                        <div style="color: #666666; display: flex; grid-gap: 5px; align-items: center">
                          <el-icon size="18"><Clock /></el-icon>
                          <div>{{ formatTime(item.createTime) }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import {reactive, ref, onBeforeUnmount, shallowRef} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";


const baseUrl = import.meta.env.VITE_BASE_URL || 'http://localhost:8080'

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  articleData: [],
  activityData: [],
  publicityData: [],
  videoData: [],
})

// 获取宣传封面图片URL
const getCoverUrl = (cover) => {
  if (!cover) return ''
  if (cover.startsWith('http://') || cover.startsWith('https://')) {
    return cover
  }
  return baseUrl + cover
}

// 获取活动封面图片URL
const getActivityCover = (cover) => {
  if (!cover) return ''
  if (cover.startsWith('http://') || cover.startsWith('https://')) {
    return cover
  }
  return baseUrl + cover
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const loadArticle = () => {
  request.get('/article/selectTop2').then(res => {
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
