<template>
  <div style="margin: 20px auto; width: 60%">
    <div class="card" style="padding: 50px 80px">
      <div style="text-align: center; font-size: 22px; font-weight: 400">{{ data.activityData.title }}</div>
      <div style="text-align: center; color: #666666; margin-top: 20px">
        <span>发布时间：{{ data.activityData.createTime }}</span>
        <!-- 【修改】根据报名状态显示不同按钮 -->
        <el-button 
          v-if="!data.hasSignedUp" 
          type="primary" 
          size="small" 
          :disabled="data.activityData.status === '已结束'"  
          @click="showSignupDialog" 
          style="margin-left: 30px;"
        >
          报名
        </el-button>
        <el-button 
          v-else-if="data.signupStatus === '待审核'" 
          type="warning" 
          size="small" 
          @click="showCancelDialog"
          style="margin-left: 30px;"
        >
          取消报名
        </el-button>
        <el-tag v-else-if="data.signupStatus === '审核通过'" type="success" size="small" style="margin-left: 30px;">
          已报名（审核通过）
        </el-tag>
        <el-tag v-else-if="data.signupStatus === '审核拒绝'" type="danger" size="small" style="margin-left: 30px;">
          已报名（审核拒绝）
        </el-tag>
      </div>
      
      <!-- ===== 修改：活动时间信息（明显展示） ===== -->
      <div style="margin-top: 30px; padding: 25px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 12px; box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);">
        <!-- 【新增】活动类型标签（最顶部，非常醒目） -->
        <div style="text-align: center; margin-bottom: 20px;">
          <el-tag 
            :type="data.activityData.activityType === '线上' ? 'success' : 'primary'" 
            size="large"
            style="font-size: 16px; padding: 10px 30px; font-weight: bold;"
          >
            {{ data.activityData.activityType === '线上' ? '🌐 线上活动' : '📍 线下活动' }}
          </el-tag>
        </div>
        
        <div style="display: flex; justify-content: space-around; align-items: center; flex-wrap: wrap; gap: 20px;">
          <div style="text-align: center;">
            <div style="font-size: 13px; color: rgba(255, 255, 255, 0.9); margin-bottom: 8px; font-weight: 500;">⏰ 活动开始</div>
            <div style="font-size: 18px; font-weight: bold; color: #fff; background: rgba(255, 255, 255, 0.2); padding: 8px 15px; border-radius: 8px;">{{ formatTime(data.activityData.startTime) }}</div>
          </div>
          <div style="text-align: center;">
            <div style="font-size: 13px; color: rgba(255, 255, 255, 0.9); margin-bottom: 8px; font-weight: 500;">⏰ 活动结束</div>
            <div style="font-size: 18px; font-weight: bold; color: #fff; background: rgba(255, 255, 255, 0.2); padding: 8px 15px; border-radius: 8px;">{{ formatTime(data.activityData.endTime) }}</div>
          </div>
          <div style="text-align: center;">
            <div style="font-size: 13px; color: rgba(255, 255, 255, 0.9); margin-bottom: 8px; font-weight: 500;">📅 持续天数</div>
            <div style="font-size: 20px; font-weight: bold; color: #ffd700; background: rgba(255, 255, 255, 0.2); padding: 8px 15px; border-radius: 8px;">{{ calculateDuration() }}</div>
          </div>
        </div>
        
        <!-- 【新增】线上线下详细信息区域 -->
        <div style="margin-top: 25px; padding-top: 20px; border-top: 2px solid rgba(255, 255, 255, 0.3);">
          <!-- 线下活动显示地点 -->
          <div v-if="data.activityData.activityType === '线下'" style="text-align: center;">
            <div style="font-size: 14px; color: rgba(255, 255, 255, 0.9); margin-bottom: 10px; font-weight: 500;">📍 活动地点</div>
            <div style="font-size: 18px; font-weight: bold; color: #fff; background: rgba(255, 255, 255, 0.25); padding: 12px 20px; border-radius: 8px; display: inline-block; max-width: 80%;">
              {{ data.activityData.location || '未设置' }}
            </div>
          </div>
          
          <!-- 线上活动显示参与方式 -->
          <div v-if="data.activityData.activityType === '线上'" style="text-align: center;">
            <div style="font-size: 14px; color: rgba(255, 255, 255, 0.9); margin-bottom: 10px; font-weight: 500;">💻 参与方式</div>
            <div style="background: rgba(255, 255, 255, 0.95); padding: 20px; border-radius: 10px; text-align: left; max-width: 90%; margin: 0 auto;">
              <!-- 显示富文本内容（包含二维码图片等） -->
              <div v-if="data.activityData.participationMethod" v-html="data.activityData.participationMethod" style="color: #333; line-height: 1.8;"></div>
              <div v-else style="color: #999; text-align: center;">未设置参与方式</div>
            </div>
          </div>
        </div>
      </div>
      
      <div 
        class="activity-content" style="margin-top: 50px;"
        v-html="data.activityData.content"
      ></div>
      
      <!-- 点赞、收藏、评论功能区域 -->
      <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; display: flex; gap: 40px">
        <div style="display: flex; align-items: center; gap: 5px; cursor: pointer; color: #666" @click="toggleLike">
          <img src="@/assets/images/点赞.png" alt="点赞" style="width: 20px; height: 20px" :style="{ filter: data.likeFlag ? 'none' : 'grayscale(100%)', opacity: data.likeFlag ? 1 : 0.5 }">
          <span :style="{ color: data.likeFlag ? '#409EFF' : '' }">点赞 {{ data.likeCount }}</span>
        </div>
        <div style="display: flex; align-items: center; gap: 5px; cursor: pointer; color: #666" @click="toggleCollect">
          <img src="@/assets/images/收藏.png" alt="收藏" style="width: 20px; height: 20px" :style="{ filter: data.collectFlag ? 'none' : 'grayscale(100%)', opacity: data.collectFlag ? 1 : 0.5 }">
          <span :style="{ color: data.collectFlag ? '#F56C6C' : '' }">收藏 {{ data.collectCount }}</span>
        </div>
        <div style="display: flex; align-items: center; gap: 5px; cursor: pointer; color: #666" @click="showCommentInput">
          <img src="@/assets/images/评论.png" alt="评论" style="width: 20px; height: 20px">
          <span>评论 {{ data.commentData.length }}</span>
        </div>
      </div>
    </div>

    <!-- 评论区 -->
    <div class="card" style="margin-top: 10px; padding: 30px">
      <div v-show="data.showCommentSection" style="display: flex; align-items: center; gap: 10px">
        <el-input 
          v-model="data.content" 
          ref="commentInputRef"
          placeholder="请输入评论内容"
          style="flex: 1"
        ></el-input>
        <el-button type="primary" @click="submitComment" :disabled="!data.content || !data.content.trim()">发布</el-button>
      </div>
      
      <div style="margin-top: 20px">
        <div v-for="item in data.commentData" :key="item.id" style="padding: 15px 0; border-bottom: 1px solid #eee">
          <div style="display: flex; align-items: center; margin-bottom: 10px">
            <img :src="getAvatarUrl(item.userAvatar)" alt="" style="height: 25px; width: 25px; border-radius: 50%; object-fit: cover">
            <div style="margin-left: 10px">
              <div style="color: #666; font-size: 14px">{{ item.userName }}</div>
              <div style="color: #999; font-size: 12px; margin-top: 2px">{{ item.time }}</div>
            </div>
          </div>
          <div style="margin-bottom: 10px; color: #333; line-height: 1.6">{{ item.content }}</div>
        </div>
      </div>
    </div>

    <!-- 【新增】报名表单对话框 -->
    <el-dialog title="活动报名" v-model="data.signupVisible" width="600px" destroy-on-close>
      <el-form :model="data.signupForm" label-width="120px" style="padding: 20px">
        <el-form-item label="真实姓名" required>
          <el-input v-model="data.signupForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="data.signupForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" required>
          <el-input v-model="data.signupForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="data.signupForm.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="data.signupForm.age" :min="1" :max="120" />
        </el-form-item>
        <el-form-item label="所在单位/学校">
          <el-input v-model="data.signupForm.organization" placeholder="请输入所在单位或学校" />
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input 
            v-model="data.signupForm.remark" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入备注说明（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.signupVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSignup" :loading="data.signupSubmitting">确定报名</el-button>
      </template>
    </el-dialog>

    <!-- 【新增】取消报名对话框 -->
    <el-dialog title="取消报名" v-model="data.cancelVisible" width="500px" destroy-on-close>
      <div style="padding: 20px">
        <div style="margin-bottom: 20px; color: #666">
          请说明取消报名的原因：
        </div>
        <el-input 
          v-model="data.cancelReason" 
          type="textarea" 
          :rows="4" 
          placeholder="请输入取消原因"
          maxlength="200"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="data.cancelVisible = false">取消</el-button>
        <el-button type="danger" @click="submitCancel" :loading="data.cancelSubmitting">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref, onMounted} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import {signUpAdd, getActivityDetail} from "@/api/activity.js";

const baseUrl = import.meta.env.VITE_BASE_URL
const commentInputRef = ref(null)

// 【新增】格式化时间显示（精确到分钟）
const formatTime = (time) => {
  if (!time) return '未设置'
  return time.replace('T', ' ').substring(0, 16)
}

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  activityId: router.currentRoute.value.query.id,
  activityData: {},
  content: null,
  likeCount: 0,
  likeFlag: false,
  collectCount: 0,
  collectFlag: false,
  commentData: [],
  showCommentSection: false,
  // 【新增】报名相关数据
  hasSignedUp: false,
  signupStatus: '',
  signupVisible: false,
  signupSubmitting: false,
  signupForm: {
    realName: '',
    phone: '',
    email: '',
    gender: '男',
    age: null,
    organization: '',
    remark: ''
  },
  // 【新增】取消报名相关数据
  cancelVisible: false,
  cancelSubmitting: false,
  cancelReason: ''
})

const getAvatarUrl = (avatar) => {
  if (!avatar) return '/default-avatar.png'
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  return baseUrl + avatar
}

// ===== 新增方法：计算活动持续天数 =====
const calculateDuration = () => {
  if (!data.activityData.startTime || !data.activityData.endTime) {
    return '未设置'
  }
  const start = new Date(data.activityData.startTime)
  const end = new Date(data.activityData.endTime)
  const diffTime = end - start
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays > 0 ? `${diffDays} 天` : '当天'
}

const loadActivityDetail = () => {
  if (!data.activityId || data.activityId === 'undefined') {
    ElMessage.error('参数错误，无法加载内容')
    return
  }
  getActivityDetail(data.activityId).then(res => {
    if (res.code === '200') {
      data.activityData = res.data
      // 【新增】检查是否已报名
      checkSignupStatus()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 【新增】检查报名状态
const checkSignupStatus = () => {
  request.get('/activitySignUp/selectAll', {
    params: {
      userId: data.user.id,
      activityId: data.activityId
    }
  }).then(res => {
    if (res.code === '200' && res.data && res.data.length > 0) {
      data.hasSignedUp = true
      data.signupStatus = res.data[0].status
    } else {
      data.hasSignedUp = false
      data.signupStatus = ''
    }
  })
}

// 【新增】显示报名对话框
const showSignupDialog = () => {
  if (!data.user.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  data.signupForm = {
    realName: '',
    phone: '',
    email: '',
    gender: '男',
    age: null,
    organization: '',
    remark: ''
  }
  data.signupVisible = true
}

// 【新增】提交报名
const submitSignup = () => {
  // 验证必填项
  if (!data.signupForm.realName || !data.signupForm.realName.trim()) {
    ElMessage.warning('请输入真实姓名')
    return
  }
  if (!data.signupForm.phone || !data.signupForm.phone.trim()) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (!data.signupForm.email || !data.signupForm.email.trim()) {
    ElMessage.warning('请输入邮箱')
    return
  }

  data.signupSubmitting = true
  
  signUpAdd({
    userId: data.user.id,
    userName: data.user.name,
    realName: data.signupForm.realName,
    phone: data.signupForm.phone,
    email: data.signupForm.email,
    gender: data.signupForm.gender,
    age: data.signupForm.age,
    organization: data.signupForm.organization,
    remark: data.signupForm.remark,
    activityId: data.activityId,
    activityName: data.activityData.title,
    status: '待审核'
  }).then(res => {
    data.signupSubmitting = false
    if (res.code === '200') {
      ElMessage.success('报名成功，等待管理员审核')
      data.signupVisible = false
      checkSignupStatus()
    } else {
      ElMessage.error(res.msg || '你已经报名，请等待审核')
    }
  }).catch(err => {
    data.signupSubmitting = false
    console.error('报名失败:', err)
    ElMessage.error(err.response?.data?.msg || '报名失败，请稍后重试')
  })
}

// 【新增】显示取消报名对话框
const showCancelDialog = () => {
  if (data.signupStatus !== '待审核') {
    ElMessage.warning('只有待审核状态的报名才能取消')
    return
  }
  data.cancelReason = ''
  data.cancelVisible = true
}

// 【新增】提交取消报名
const submitCancel = () => {
  if (!data.cancelReason || !data.cancelReason.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }

  data.cancelSubmitting = true
  
  // 先获取报名记录ID
  request.get('/activitySignUp/selectAll', {
    params: {
      userId: data.user.id,
      activityId: data.activityId
    }
  }).then(res => {
    if (res.code === '200' && res.data && res.data.length > 0) {
      const signupId = res.data[0].id
      
      // 删除报名记录
      request.delete('/activitySignUp/delete/' + signupId).then(delRes => {
        data.cancelSubmitting = false
        if (delRes.code === '200') {
          ElMessage.success('取消报名成功')
          data.cancelVisible = false
          checkSignupStatus()
        } else {
          ElMessage.error(delRes.msg || '取消报名失败')
        }
      }).catch(err => {
        data.cancelSubmitting = false
        ElMessage.error('取消报名失败，请稍后重试')
        console.error(err)
      })
    } else {
      data.cancelSubmitting = false
      ElMessage.error('未找到报名记录')
    }
  })
}

const toggleLike = () => {
  request.post('/likes/add', {
    userId: data.user.id,
    activityId: data.activityId,
    userName: data.user.name,
    activityTitle: data.activityData.title
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      checkLike()
      loadLikeCount()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const checkLike = () => {
  if (!data.activityId) return
  request.get('/likes/selectAll', {
    params: {
      userId: data.user.id,
      activityId: data.activityId
    }
  }).then(res => {
    if (res.code === '200') {
      data.likeFlag = !!res.data.length;
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const loadLikeCount = () => {
  if (!data.activityId) return
  request.get('/likes/selectAll', {
    params: {
      activityId: data.activityId
    }
  }).then(res => {
    if (res.code === '200') {
      data.likeCount = res.data.length || 0
    }
  })
}

const toggleCollect = () => {
  request.post('/collect/add', {
    userId: data.user.id,
    activityId: data.activityId,
    userName: data.user.name,
    activityTitle: data.activityData.title
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      checkCollect()
      loadCollectCount()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const checkCollect = () => {
  if (!data.activityId) return
  request.get('/collect/selectAll', {
    params: {
      userId: data.user.id,
      activityId: data.activityId
    }
  }).then(res => {
    if (res.code === '200') {
      data.collectFlag = !!res.data.length;
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const loadCollectCount = () => {
  if (!data.activityId) return
  request.get('/collect/selectAll', {
    params: {
      activityId: data.activityId
    }
  }).then(res => {
    if (res.code === '200') {
      data.collectCount = res.data.length || 0
    }
  })
}

const showCommentInput = () => {
  data.showCommentSection = !data.showCommentSection
  if (data.showCommentSection) {
    setTimeout(() => {
      commentInputRef.value?.focus()
    }, 100)
  }
}

const submitComment = () => {
  if (!data.content || !data.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  request.post('/comment/add', {
    userId: data.user.id,
    activityId: data.activityId,
    userName: data.user.name,
    content: data.content,
    time: new Date().toLocaleString()
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('评论成功')
      data.content = ''
      loadComment()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const loadComment = () => {
  if (!data.activityId) return
  request.get('/comment/selectAll', {
    params: {
      activityId: data.activityId
    }
  }).then(res => {
    if (res.code === '200') {
      data.commentData = res.data || []
    } else {
      ElMessage.error(res.msg)
    }
  })
}

onMounted(() => {
  loadActivityDetail()
  checkLike()
  loadLikeCount()
  checkCollect()
  loadCollectCount()
  loadComment()
})
</script>

<style scoped>
/* 【新增】活动详情内容样式 */
.activity-content {
  line-height: 1.8;
}

.activity-content :deep(p) {
  margin: 1em 0;
  line-height: 1.8;
}

.activity-content :deep(br) {
  display: block;
  content: "";
  margin: 0.5em 0;
}

.activity-content :deep(div) {
  margin: 0.5em 0;
}

.activity-content :deep(ul), .activity-content :deep(ol) {
  padding-left: 20px;
  margin: 1em 0;
}

.activity-content :deep(li) {
  margin: 0.5em 0;
}

.activity-content :deep(blockquote) {
  margin: 1em 0;
  padding: 10px 20px;
  border-left: 4px solid #ccc;
  background-color: #f8f8f8;
}

.activity-content :deep(pre) {
  margin: 1em 0;
  padding: 10px;
  background-color: #f6f8fa;
  border-radius: 3px;
  overflow-x: auto;
}

.activity-content :deep(table) {
  border-collapse: collapse;
  margin: 1em 0;
  width: 100%;
}

.activity-content :deep(th), .activity-content :deep(td) {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

.activity-content :deep(th) {
  background-color: #f6f8fa;
  font-weight: bold;
}
</style>
