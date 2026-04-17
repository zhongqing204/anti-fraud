<template>
  <div style="width: 60%; margin: 20px auto">
    <div style="font-size: 18px">我的收藏（{{ data.total }}）</div>
    <div style="margin-top: 20px">
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in data.tableData">
          <div class="front_card" style="margin-bottom: 20px">
            <img :src="item.article.cover" alt="" style="width: 100%; height: 150px; border-radius: 5px; cursor: pointer" @click="router.push('/front/articleDetail?id=' + item.articleId)">
            <div style="padding: 10px">
              <div style="font-size: 18px; font-weight: bold" class="line1">{{ item.article.title }}</div>
              <div style="margin-top: 15px; display: flex; grid-gap: 20px">
                <div style="color: #666666; display: flex; grid-gap: 5px; align-items: center">
                  <el-icon size="18"><Clock /></el-icon>
                  <div>{{ item.article.time }}</div>
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
</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";
import {Delete, Edit} from "@element-plus/icons-vue";


const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  pageNum: 1,
  pageSize: 8,
  tableData: [],
  total: 0,
})

const load = () => {
  request.get('/collect/selectPage', {
    params: {
      userId: data.user.id,
      pageNum: data.pageNum,
      pageSize: data.pageSize
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.records || []
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