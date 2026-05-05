<template>
  <div style="width: 70%; margin: 20px auto">
    <div style="font-size: 18px; margin-bottom: 15px">我的评论</div>
    <el-radio-group v-model="data.activeTab" @change="handleTabChange" style="margin-bottom: 20px">
      <el-radio-button label="article">反诈论坛</el-radio-button>
      <el-radio-button label="video">反诈视频</el-radio-button>
      <el-radio-button label="publicity">反诈宣传</el-radio-button>
      <el-radio-button label="activity">反诈活动</el-radio-button>
    </el-radio-group>
    <div style="margin-top: 20px; padding: 20px" class="card">
      <el-table stripe :data="data.tableData">
        <el-table-column label="标题" width="400" show-overflow-tooltip>
          <template v-slot="scope">
            <a :href="getDetailUrl(scope.row)" target="_blank" style="text-decoration: none; color: #333; cursor: pointer">{{ getTitle(scope.row) }}</a>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" />
        <el-table-column prop="time" label="评论时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template v-slot="scope">
            <el-button type="danger" size="small" @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="data.total" style="margin-top: 20px">
        <el-pagination @current-change="load" layout="total, prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  activeTab: 'article',
  pageNum: 1,
  pageSize: 5,
  tableData: [],
  total: 0,
})

// 根据类型获取标题
const getTitle = (row) => {
  if (data.activeTab === 'article') {
    return row.articleTitle
  } else if (data.activeTab === 'video') {
    return row.videoTitle
  } else if (data.activeTab === 'publicity') {
    return row.publicityTitle
  } else if (data.activeTab === 'activity') {
    return row.activityTitle
  }
  return ''
}

// 根据类型跳转到不同详情页
const getDetailUrl = (row) => {
  if (data.activeTab === 'article') {
    return `/front/articleDetail?id=${row.articleId}`
  } else if (data.activeTab === 'video') {
    return `/front/videoDetail?id=${row.videoId}`
  } else if (data.activeTab === 'publicity') {
    return `/front/publicityDetail?id=${row.publicityId}`
  } else if (data.activeTab === 'activity') {
    return `/front/activityDetail?id=${row.activityId}`
  }
  return ''
}

const handleTabChange = () => {
  data.pageNum = 1
  load()
}

const load = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    userId: data.user.id
  }
  
  request.get('/comment/selectPage', {
    params: params
  }).then(res => {
    if (res.code === '200') {
      let allData = res.data?.records || []
      
      // 根据类型过滤数据
      if (data.activeTab === 'article') {
        allData = allData.filter(item => item.articleId != null)
      } else if (data.activeTab === 'video') {
        allData = allData.filter(item => item.videoId != null)
      } else if (data.activeTab === 'publicity') {
        allData = allData.filter(item => item.publicityId != null)
      } else if (data.activeTab === 'activity') {
        allData = allData.filter(item => item.activityId != null)
      }
      
      data.tableData = allData
      data.total = allData.length
    } else {
      ElMessage.error(res.msg)
    }
  })
}

load()

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/comment/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success("删除成功")
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}
</script>

<style scoped>
</style>
