<template>
  <div>
    <div style="background-color: #f8f8f8; padding: 20px">
      <div style="width: 70%; margin: 0 auto">
        <el-input prefix-icon="Search" v-model="data.title" @keyup.enter="load" clearable @clear="load" placeholder="请输入帖子名称查询" style="width: 400px; height: 40px"></el-input>
      </div>
    </div>
    <div style="width: 70%; margin: 30px auto">
      <div>
        <el-button style="margin-bottom: 20px" @click="changCategory(null)" :type="checkType(null)">全部</el-button>
        <el-button style="margin-bottom: 20px" @click="changCategory(item.id)" :type="checkType(item.id)" v-for="item in data.categoryData">{{ item.name }}</el-button>
      </div>
      <div class="card" style="margin-bottom: 10px" v-for="item in data.articleData">
        <div style="display: flex; grid-gap: 20px">
          <img :src="item.img" alt="" style="height: 120px; width: 150px; border-radius: 5px; cursor: pointer" @click="router.push('/front/articleDetail?id=' + item.id)">
          <div>
            <div style="font-size: 16px; font-weight: bold">{{ item.title }}</div>
            <div style="margin-top: 10px; color: #666666; line-height: 25px; height: 50px" class="line2">{{ item.content?.substring(0, 100) || '' }}</div>
            <div style="margin-top: 15px; display: flex; grid-gap: 20px">
              <div style="display: flex; grid-gap: 5px; align-items: center">
                <img :src="item.userAvatar" alt="" style="height: 20px; width: 20px; border-radius: 50%">
                <div style="color: #666666">{{ item.userName }}</div>
              </div>
              <div style="color: #666666; display: flex; grid-gap: 5px; align-items: center">
                <el-icon size="18"><View /></el-icon>
                <div>{{ item.views }}</div>
              </div>
              <div style="color: #666666; display: flex; grid-gap: 5px; align-items: center">
                <el-icon size="18"><Clock /></el-icon>
                <div>{{ item.time }}</div>
              </div>
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
import {reactive, ref, onBeforeUnmount, shallowRef} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";


const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  title: null,
  categoryId: null,
  pageNum: 1,
  pageSize: 5,
  total: 0,
  articleData: [],
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
  request.get('/article/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      categoryId: data.categoryId,
      status: '审核通过'
    }
  }).then(res => {
    if (res.code === '200') {
      data.articleData = res.data?.list
      data.total = res.data?.total
    } else {
      ElMessage.error(res.msg)
    }
  })
}

load()

</script>

<style scoped>

</style>