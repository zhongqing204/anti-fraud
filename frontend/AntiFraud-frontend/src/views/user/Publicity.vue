<template>
  <div>
    <div style="background-color: #f8f8f8; padding: 20px">
      <div style="width: 60%; margin: 0 auto; display: flex; align-items: center; gap: 15px">
        <el-input prefix-icon="Search" v-model="data.title" @keyup.enter="load" clearable @clear="load" placeholder="请输入宣传标题查询" style="width: 350px; height: 40px"></el-input>
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
                <el-option label="全部" :value="null" />
                <el-option v-for="item in data.categoryData" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </div>
            <div style="margin-bottom: 15px">
              <div style="font-weight: bold; margin-bottom: 8px">时间</div>
              <el-select v-model="data.timeFilter" placeholder="请选择时间范围" clearable style="width: 100%" :teleported="false">
                <el-option label="全部" :value="null" />
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
          <el-col :span="6" v-for="item in data.newsData" :key="item.id" style="margin-bottom: 20px">
            <div class="front_card">
              <div>
                <img :src="getCoverUrl(item.cover)" alt="" style="height: 180px; width: 100%; border-radius: 5px; cursor: pointer" @click="router.push('/front/publicityDetail?id=' + item.id)">
                <div style="padding: 10px">
                  <div style="font-size: 16px; font-weight: bold" class="line1">{{ item.title }}</div>
                  <div style="margin-top: 15px; display: flex; grid-gap: 20px">
                    <div style="color: #666666; display: flex; grid-gap: 5px; align-items: center">
                      <el-icon size="18"><Clock /></el-icon>
                      <div>{{ item.createTime }}</div>
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
import {Clock, Filter} from '@element-plus/icons-vue'

const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  title: null,
  categoryId: null,
  timeFilter: null,
  categoryData: [],
  pageNum: 1,
  pageSize: 6,
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
    if (res.code === '200') {
      let newsData = res.data?.records || []
      data.total = res.data?.total || 0
      
      // 时间筛选
      if (data.timeFilter) {
        const now = new Date()
        newsData = newsData.filter(item => {
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
      
      data.newsData = newsData
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
