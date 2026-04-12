<template>
  <div>
    <div style="background-color: #f8f8f8; padding: 20px">
      <div style="width: 60%; margin: 0 auto">
        <el-input prefix-icon="Search" v-model="data.title" @keyup.enter="load" clearable @clear="load" placeholder="请输入公告标题查询" style="width: 400px; height: 40px"></el-input>
      </div>
    </div>
    <div style="width: 60%; margin: 30px auto">
      <el-table :data="data.noticeList" stripe @row-click="viewNotice" :header-cell-style="{ cursor: 'default' }">
        <el-table-column prop="title" label="公告标题">
        <template #default="scope">
            <span class="notice-title">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="发布时间" width="200" align="center">
          <template #default="scope">
            <div style="display: flex; align-items: center; gap: 5px; justify-content: center;">
              <el-icon size="18"><Clock /></el-icon>
              <span>{{ scope.row.time }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <el-dialog title="公告详情" v-model="data.detailVisible" width="60%" top="5vh">
      <div style="text-align: center;">
        <h3 style="margin-bottom: 15px;">{{ data.currentNotice.title }}</h3>
        <p style="color: #999; margin-bottom: 20px;">发布时间：{{ data.currentNotice.time }}</p>
      </div>
      <div v-html="data.currentNotice.content" style="line-height: 1.8; height: 60vh; overflow-y: auto;"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import request from "@/utils/request.js";

const data = reactive({
  noticeList: [],
  title: null,
  detailVisible: false,
  currentNotice: {}
});

const loadNotice = () => {
  request.get('/notice/selectAll', {
    params: {
      title: data.title
    }
  }).then(res => {
    if (res.code === '200') {
      data.noticeList = res.data || [];
    }
  });
};

const viewNotice = (row) => {
  data.currentNotice = row;
  data.detailVisible = true;
};

const load = () => {
  loadNotice();
};

onMounted(() => {
  loadNotice();
});
</script>

<style scoped>
.notice-title {
  display: inline-block;
  cursor: pointer;
}
</style>