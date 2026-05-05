<template>
  <div style="width: 70%; margin: 20px auto">
    <div style="font-size: 18px; margin-bottom: 15px">我的活动报名</div>
    <div style="margin-top: 20px; padding: 20px" class="card">
      <el-table stripe :data="data.tableData">
        <el-table-column prop="activityName" label="活动名称" width="300" show-overflow-tooltip />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column prop="signupTime" label="报名时间" width="180" />
        <el-table-column label="审核状态" width="120">
          <template v-slot="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="审核说明" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template v-slot="scope">
            <el-button 
              v-if="scope.row.status === '待审核'" 
              type="danger" 
              size="small" 
              @click="cancelSignup(scope.row)"
            >
              取消
            </el-button>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="data.total" style="margin-top: 20px">
        <el-pagination @current-change="load" layout="total, prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
      </div>
    </div>

    <!-- 取消报名对话框 -->
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
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  pageNum: 1,
  pageSize: 10,
  tableData: [],
  total: 0,
  cancelVisible: false,
  cancelSubmitting: false,
  cancelReason: '',
  currentSignupId: null
})

const getStatusType = (status) => {
  if (status === '审核通过') return 'success'
  if (status === '审核拒绝') return 'danger'
  return 'warning'
}

const load = () => {
  request.get('/activitySignUp/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userId: data.user.id
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data.records || []
      data.total = res.data.total
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const cancelSignup = (row) => {
  data.currentSignupId = row.id
  data.cancelReason = ''
  data.cancelVisible = true
}

const submitCancel = () => {
  if (!data.cancelReason || !data.cancelReason.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }

  data.cancelSubmitting = true
  
  request.delete('/activitySignUp/delete/' + data.currentSignupId).then(res => {
    data.cancelSubmitting = false
    if (res.code === '200') {
      ElMessage.success('取消报名成功')
      data.cancelVisible = false
      load()
    } else {
      ElMessage.error(res.msg || '取消报名失败')
    }
  }).catch(err => {
    data.cancelSubmitting = false
    ElMessage.error('取消报名失败，请稍后重试')
    console.error(err)
  })
}

load()
</script>

<style scoped>
</style>
