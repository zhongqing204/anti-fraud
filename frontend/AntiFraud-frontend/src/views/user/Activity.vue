<template>
  <div>
    <div style="background-color: #f8f8f8; padding: 20px">
      <div style="width: 60%; margin: 0 auto">
        <el-input prefix-icon="Search" v-model="data.title" @keyup.enter="load" clearable @clear="load" placeholder="请输入活动名称查询" style="width: 400px; height: 40px"></el-input>
      </div>
    </div>
    <div style="width: 60%; margin: 30px auto">
      <div>
        <el-button style="margin-bottom: 20px" @click="changCategory(null)" :type="checkType(null)">全部</el-button>
        <el-button style="margin-bottom: 20px" @click="changCategory(item.id)" :type="checkType(item.id)" v-for="item in data.categoryData" :key="item.id">{{ item.name }}</el-button>
      </div>
      <div>
         <el-row :gutter="20">
          <el-col :span="6" v-for="item in data.activityData" :key="item.id" style="margin-bottom: 20px">
            <div class="front_card">
              <div>
                <img :src="baseUrl + item.cover" alt="" style="height: 180px; width: 100%; border-radius: 5px; cursor: pointer" @click="router.push('/front/activityDetail?id=' + item.id)">
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
import {Clock} from '@element-plus/icons-vue'
const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  title: null,
  categoryId: null,
  pageNum: 1,
  pageSize: 6,
  total: 0,
  activityData: [],
  categoryData: [],
})

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

const changCategory = (categoryId) => {
  data.categoryId = categoryId
  load()
}

const checkType = (categoryId) => {
  return data.categoryId === categoryId ? 'primary' : ''
}

const load = () => {
  request.get('/activity/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      categoryId: data.categoryId,
    }
  }).then(res => {
    if (res.code === '200') {
      data.activityData = res.data?.records
      data.total = res.data?.total
    } else {
      ElMessage.error(res.msg)
    }
  })
}

load()

</script>