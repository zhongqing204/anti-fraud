<template>
  <div>
    <div class="card" style="margin-bottom: 5px">您好！{{ data.user?.name }}，欢迎使用本系统！</div>
    <div style="display: flex">
      <div class="card" style="flex: 50%; height: 350px">
        <div style="font-weight: bold; font-size: 18px; padding: 10px 0 30px 10px"></div>
        <el-timeline style="max-width: 600px">
          <el-timeline-item
              v-for="(item, index) in data.noticeData"
              :key="index"
              :timestamp="item.time"
          >
            {{ item.content }}
          </el-timeline-item>
        </el-timeline>
      </div>
      <div style="flex: 50%"></div>
    </div>
  </div>
</template>

<script setup>

import {reactive} from "vue";


const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  noticeData: []
})

</script>