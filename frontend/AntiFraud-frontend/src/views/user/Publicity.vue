<template>
  <div>
    <div style="background-color: #f8f8f8; padding: 20px">
      <div style="width: 60%; margin: 0 auto">
        <el-input prefix-icon="Search" v-model="data.title" @keyup.enter="load" clearable @clear="load" placeholder="请输入宣传标题称查询" style="width: 400px; height: 40px"></el-input>
      </div>
    </div>
    <div style="width: 60%; margin: 30px auto">
      <div>
        <el-row :gutter="20">
          <el-col :span="6" v-for="item in data.newsData" style="margin-bottom: 20px">
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
import {reactive, ref, onBeforeUnmount, shallowRef} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";

const baseUrl = import.meta.env.VITE_BASE_URL

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  title: null,
  pageNum: 1,
  pageSize: 6,
  total: 0,
  newsData: [],
})

const getCoverUrl = (cover) => {
  if (!cover) return ''
  if (cover.startsWith('http://') || cover.startsWith('https://')) {
    return cover
  }
  return baseUrl + cover
}

const load = () => {
  request.get('/publicity/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
    }
  }).then(res => {
    if (res.code === '200') {
      data.newsData = res.data?.records || []
      data.total = res.data?.total || 0
    } else {
      ElMessage.error(res.msg)
    }
  })
}

load()

</script>

<style scoped>

</style>