<template>
  <div>
    <div style="background-color: #f8f8f8; padding: 20px">
      <div style="width: 60%; margin: 0 auto; display: flex; align-items: center; gap: 15px">
        <el-input prefix-icon="Search" v-model="data.title" @keyup.enter="load" clearable @clear="load" placeholder="请输入活动名称查询" style="width: 350px; height: 40px"></el-input>
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
            <!-- 【新增】线上线下筛选 -->
            <div style="margin-bottom: 15px">
              <div style="font-weight: bold; margin-bottom: 8px">活动类型</div>
              <el-select v-model="data.activityType" placeholder="请选择活动类型" clearable style="width: 100%" :teleported="false">
                <el-option label="全部" :value="undefined" />
                <el-option label="线上" value="线上" />
                <el-option label="线下" value="线下" />
              </el-select>
            </div>
            <div style="margin-bottom: 15px">
              <div style="font-weight: bold; margin-bottom: 8px">状态</div>
              <el-select v-model="data.status" placeholder="请选择状态" clearable style="width: 100%" :teleported="false">
                <el-option label="全部" :value="undefined" />
                <el-option label="进行中" value="进行中" />
                <el-option label="已结束" value="已结束" />
              </el-select>
            </div>
            <div style="margin-bottom: 15px">
              <div style="font-weight: bold; margin-bottom: 8px">时间</div>
              <el-select v-model="data.timeFilter" placeholder="请选择时间范围" clearable style="width: 100%" :teleported="false">
                <el-option label="全部" :value="undefined" />
                <el-option label="最近一周" value="week" />
                <el-option label="最近一月" value="month" />
                <el-option label="最近三月" value="threeMonths" />
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
        <div v-for="item in data.activityData" :key="item.id" style="margin-bottom: 20px; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px; background-color: #fff; cursor: pointer" @click="router.push('/front/activityDetail?id=' + item.id)">
          <div style="display: flex; gap: 20px; align-items: center">
            <div style="flex-shrink: 0; width: 150px; height: 100px">
              <img :src="baseUrl + item.cover" alt="" style="width: 100%; height: 100%; object-fit: cover; border-radius: 5px">
            </div>
            <div style="flex: 1">
              <div style="font-size: 18px; font-weight: bold; margin-bottom: 10px">{{ item.title }}</div>
              <div style="color: #666; font-size: 14px; line-height: 1.6" class="line2">{{ stripHtml(item.content)?.substring(0, 100) || '暂无内容' }}...</div>
              <div style="margin-top: 10px; color: #999; font-size: 12px">
                <span v-if="item.startTime">开始时间：{{ formatTime(item.startTime) }}</span>
                <span v-if="item.endTime" style="margin-left: 20px">结束时间：{{ formatTime(item.endTime) }}</span>
                <!-- 【新增】显示活动类型和地点/参与方式 -->
                <div style="margin-top: 5px">
                  <el-tag v-if="item.activityType" :type="item.activityType === '线上' ? 'success' : 'primary'" size="small">
                    {{ item.activityType }}
                  </el-tag>
                  <span v-if="item.activityType === '线下' && item.location" style="margin-left: 10px; color: #666">📍 {{ item.location }}</span>
                  <span v-if="item.activityType === '线上'" style="margin-left: 10px; color: #666">💻 线上活动</span>
                </div>
              </div>
            </div>
            <div style="flex-shrink: 0">
              <el-tag :type="item.status === '已结束' ? 'info' : 'success'" size="large" style="padding: 10px 20px; font-size: 14px">
                状态：{{ item.status || '进行中' }}
              </el-tag>
            </div>
          </div>
        </div>
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
import {Filter} from '@element-plus/icons-vue'

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

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  title: null,
  categoryId: null,
  // 【新增】线上线下筛选字段
  activityType: null,
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
  data.hasFilter = !!(data.categoryId || data.activityType || data.status || data.timeFilter)
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
      status: data.status
    }
  }).then(res => {
    if (res.code === '200') {
      let activityData = res.data?.records || []
      data.total = res.data?.total || 0
      
      // 时间筛选
      if (data.timeFilter) {
        const now = new Date()
        activityData = activityData.filter(item => {
          const createTime = new Date(item.createTime)
          const diffDays = (now - createTime) / (1000 * 60 * 60 * 24)
          
          switch(data.timeFilter) {
            case 'week':
              return diffDays <= 7
            case 'month':
              return diffDays <= 30
            case 'threeMonths':
              return diffDays <= 90
            default:
              return true
          }
        })
      }
      
      data.activityData = activityData
    } else {
      ElMessage.error(res.msg)
    }
  })
}

load()
</script>

<style scoped>
.line2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
