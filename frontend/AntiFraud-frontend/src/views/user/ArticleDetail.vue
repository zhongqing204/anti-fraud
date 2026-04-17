<template>
  <div style="margin: 20px auto; width: 60%">
    <div class="card" style="padding: 50px 80px">
      <div style="text-align: center; font-size: 22px; font-weight: 400">{{ data.articleData.title }}</div>
      <div style="text-align: center; color: #666666; margin-top: 20px">
        <span>发布时间：{{ data.articleData.time }}</span>
        <span style="margin: 0 20px">浏览量：{{ data.articleData.views }}</span>
        <el-button v-if="data.collectFlag" type="danger" size="small" @click="collect">取消收藏</el-button>
        <el-button v-else type="primary" size="small" @click="collect">收藏</el-button>
      </div>
      <div style="margin-top: 50px" v-html="data.articleData.content"></div>
    </div>

    <!-- 评论区 -->
    <div class="card" style="margin-top: 10px; padding: 30px">
      <div style="font-weight: 400; font-size: 20px">发表您的评论</div>
      <div style="margin-top: 20px">
        <el-input type="textarea" :rows="4" v-model="data.content" placeholder="发表一下此刻的感受吧！"></el-input>
      </div>
      <div style="margin-top: 10px; text-align: right">
        <el-button type="info" @click="submit">发布</el-button>
      </div>
      <div style="font-weight: 400; font-size: 20px; margin-top: 30px">看看大家都说了什么（{{ data.commentData.length }}）</div>
      <div style="margin-top: 20px; display: flex; grid-gap: 20px; flex-direction: column">
        <div v-for="item in data.commentData" :key="item.id" style="display: flex; grid-gap: 20px; padding: 15px; border-bottom: 1px solid #eee">
          <img :src="item.userAvatar || '/default-avatar.png'" alt="" style="width: 50px; height: 50px; border-radius: 50%; object-fit: cover">
          <div style="flex: 1">
            <div style="font-weight: bold">{{ item.userName }}</div>
            <div style="margin-top: 5px; color: #666666">{{ item.content }}</div>
            <div style="margin-top: 5px; color: #999999; font-size: 12px">{{ item.time }}</div>
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


const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  articleId: router.currentRoute.value.query.id,
  articleData: {},
  collectFlag: false,
  content: null,
  commentData: [],
})

const loadArticle = () => {
  request.get('/article/selectById/' + data.articleId).then(res => {
    if (res.code === '200') {
      data.articleData = res.data
      data.articleData.views = data.articleData.views + 1
      request.put('/article/update', data.articleData)
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadArticle()

const collect = () => {
  request.post('/collect/add', {
    userId: data.user.id,
    articleId: data.articleId
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      checkCollect()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const checkCollect = () => {
  request.get('/collect/selectAll', {
    params: {
      userId: data.user.id,
      articleId: data.articleId
    }
  }).then(res => {
    if (res.code === '200') {
      data.collectFlag = !!res.data.length;
    } else {
      ElMessage.error(res.msg)
    }
  })
}
checkCollect()

// 提交评论
const submit = () => {
  if (!data.content) {
    ElMessage.warning('请输入评论内容')
    return
  }
  request.post('/comment/add', {
    userId: data.user.id,
    articleId: data.articleId,
    content: data.content
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('评论成功')
      data.content = null
      loadComment()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 加载评论列表
const loadComment = () => {
  request.get('/comment/selectAll', {
    params: {
      articleId: data.articleId
    }
  }).then(res => {
    if (res.code === '200') {
      data.commentData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadComment()
</script>

<style scoped>

</style>