<template>
  <div style="padding: 20px">
    <div style="font-size: 20px; font-weight: bold; margin-bottom: 20px">活动报名管理</div>
    
    <div style="margin-bottom: 20px; display: flex; gap: 10px">
      <el-input v-model="data.searchUserName" placeholder="搜索用户姓名" style="width: 180px" clearable />
      <el-input v-model="data.searchRealName" placeholder="搜索真实姓名" style="width: 180px" clearable />
      <!-- 【修改】活动名称改为下拉选择框 -->
      <el-select v-model="data.searchActivityId" placeholder="选择活动" style="width: 250px" clearable filterable>
        <el-option 
          v-for="item in data.activityList" 
          :key="item.id" 
          :label="item.title" 
          :value="item.id" 
        />
      </el-select>
      <el-select v-model="data.searchStatus" placeholder="审核状态" style="width: 130px" clearable>
        <el-option label="待审核" value="待审核" />
        <el-option label="审核通过" value="审核通过" />
        <el-option label="审核拒绝" value="审核拒绝" />
      </el-select>
      <el-button type="primary" @click="load">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="card" style="padding: 20px">
      <el-table stripe :data="data.tableData">
        <el-table-column prop="userName" label="用户名" width="100" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" width="60" />
        <el-table-column prop="age" label="年龄" width="60" />
        <el-table-column prop="organization" label="单位/学校" width="150" show-overflow-tooltip />
        <el-table-column prop="activityName" label="活动名称" width="250" show-overflow-tooltip />
        <el-table-column prop="signupTime" label="报名时间" width="180" />
        <el-table-column label="审核状态" width="100">
          <template v-slot="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="审核说明" show-overflow-tooltip />
        <el-table-column label="退报状态" width="100">
          <template v-slot="scope">
            <el-tag v-if="scope.row.cancelStatus" :type="getCancelStatusType(scope.row.cancelStatus)">
              {{ scope.row.cancelStatus }}
            </el-tag>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="cancelReason" label="退报原因" show-overflow-tooltip />
        <!-- 【修改】操作列移到最右边，使用fixed -->
        <el-table-column label="操作" width="350" fixed="right">
          <template v-slot="scope">
            <!-- 【修改】通过/拒绝按钮合并为处理按钮 -->
            <el-button 
              v-if="scope.row.status === '待审核'" 
              type="primary" 
              size="small" 
              @click="showAuditDialog(scope.row)"
              plain
            >
              处理
            </el-button>
            <!-- 【新增】退报审批按钮 -->
            <el-button 
              v-if="scope.row.cancelStatus === '待审批'" 
              type="warning" 
              size="small" 
              @click="showCancelAuditDialog(scope.row)"
              plain
            >
              审批退报
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              @click="showDetailDialog(scope.row)"
              plain
            >
              详情
            </el-button>
            <!-- 【新增】只有审核通过的才能删除 -->
            <el-button 
              v-if="scope.row.status === '审核通过'" 
              type="danger" 
              size="small" 
              @click="handleDelete(scope.row)"
              plain
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="data.total" style="margin-top: 20px">
        <el-pagination @current-change="load" layout="total, prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
      </div>
    </div>

    <!-- 【修改】审核对话框 - 可选择通过或拒绝 -->
    <el-dialog title="处理报名" v-model="data.auditVisible" width="500px" destroy-on-close draggable>
      <div style="padding: 20px">
        <div style="margin-bottom: 20px">
          <div style="font-weight: bold; margin-bottom: 10px">审核结果：</div>
          <el-radio-group v-model="data.auditResult">
            <el-radio value="审核通过">通过</el-radio>
            <el-radio value="审核拒绝">拒绝</el-radio>
          </el-radio-group>
        </div>
        <div style="margin-bottom: 20px">
          <div style="font-weight: bold; margin-bottom: 10px">审核说明：</div>
          <el-input 
            v-model="data.auditReason" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入审核说明"
            maxlength="200"
            show-word-limit
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="data.auditVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit" :loading="data.auditSubmitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="报名信息详情" v-model="data.detailVisible" width="600px" destroy-on-close draggable>
      <div style="padding: 20px">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ data.currentDetail.userName }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ data.currentDetail.realName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ data.currentDetail.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ data.currentDetail.email }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ data.currentDetail.gender }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ data.currentDetail.age }}</el-descriptions-item>
          <el-descriptions-item label="单位/学校" :span="2">{{ data.currentDetail.organization }}</el-descriptions-item>
          <el-descriptions-item label="活动名称" :span="2">{{ data.currentDetail.activityName }}</el-descriptions-item>
          <el-descriptions-item label="报名时间">{{ data.currentDetail.signupTime }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">{{ data.currentDetail.status }}</el-descriptions-item>
          <el-descriptions-item label="备注说明" :span="2">{{ data.currentDetail.remark || '无' }}</el-descriptions-item>
          <el-descriptions-item label="审核说明" :span="2">{{ data.currentDetail.reason || '无' }}</el-descriptions-item>
          <el-descriptions-item v-if="data.currentDetail.cancelStatus" label="退报状态" :span="2">
            <el-tag :type="getCancelStatusType(data.currentDetail.cancelStatus)">{{ data.currentDetail.cancelStatus }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="data.currentDetail.cancelReason" label="退报原因" :span="2">{{ data.currentDetail.cancelReason }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 【新增】退报审批对话框 -->
    <el-dialog title="审批退报申请" v-model="data.cancelAuditVisible" width="500px" destroy-on-close draggable>
      <div style="padding: 20px">
        <div style="margin-bottom: 15px; padding: 10px; background: #f5f7fa; border-radius: 4px;">
          <div style="font-weight: bold; margin-bottom: 5px;">用户退报原因：</div>
          <div style="color: #666;">{{ data.currentCancelAudit.cancelReason || '未填写' }}</div>
        </div>
        <div style="margin-bottom: 20px">
          <div style="font-weight: bold; margin-bottom: 10px">审批结果：</div>
          <el-radio-group v-model="data.cancelAuditResult">
            <el-radio value="已同意">同意退报</el-radio>
            <el-radio value="已拒绝">拒绝退报</el-radio>
          </el-radio-group>
        </div>
        <div style="margin-bottom: 20px">
          <div style="font-weight: bold; margin-bottom: 10px">审批说明：</div>
          <el-input 
            v-model="data.cancelAuditReason" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入审批说明（选填）"
            maxlength="200"
            show-word-limit
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="data.cancelAuditVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCancelAudit" :loading="data.cancelAuditSubmitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  tableData: [],
  total: 0,
  searchUserName: '',
  searchRealName: '',
  // 【修改】使用活动ID而不是名称
  searchActivityId: null,
  activityList: [], // 活动列表
  searchStatus: '',
  auditVisible: false,
  auditSubmitting: false,
  auditReason: '',
  auditResult: '审核通过',
  currentAuditId: null,
  detailVisible: false,
  currentDetail: {},
  // 【新增】退报审批相关
  cancelAuditVisible: false,
  cancelAuditSubmitting: false,
  cancelAuditReason: '',
  cancelAuditResult: '已同意',
  currentCancelAudit: {}
})

const getStatusType = (status) => {
  if (status === '审核通过') return 'success'
  if (status === '审核拒绝') return 'danger'
  return 'warning'
}

// 【新增】获取退报状态类型
const getCancelStatusType = (status) => {
  if (status === '已同意') return 'success'
  if (status === '已拒绝') return 'danger'
  return 'warning'
}

// 【新增】加载活动列表
const loadActivityList = () => {
  request.get('/activity/selectAll').then(res => {
    if (res.code === '200') {
      data.activityList = res.data || []
    }
  })
}
loadActivityList()

const load = () => {
  request.get('/activitySignUp/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userName: data.searchUserName,
      realName: data.searchRealName,
      activityId: data.searchActivityId, // 【修改】使用活动ID
      status: data.searchStatus
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

const resetSearch = () => {
  data.searchUserName = ''
  data.searchRealName = ''
  data.searchActivityId = null // 【修改】重置活动ID
  data.searchStatus = ''
  data.pageNum = 1
  load()
}

// 【修改】显示处理对话框
const showAuditDialog = (row) => {
  data.currentAuditId = row.id
  data.auditResult = '审核通过'
  data.auditReason = ''
  data.auditVisible = true
}

// 【新增】显示退报审批对话框
const showCancelAuditDialog = (row) => {
  data.currentCancelAudit = row
  data.cancelAuditResult = '已同意'
  data.cancelAuditReason = ''
  data.cancelAuditVisible = true
}

// 【新增】提交退报审批
const submitCancelAudit = () => {
  data.cancelAuditSubmitting = true
  
  request.put('/activitySignUp/approveCancel', {
    id: data.currentCancelAudit.id,
    cancelStatus: data.cancelAuditResult,
    reason: data.cancelAuditReason
  }).then(res => {
    data.cancelAuditSubmitting = false
    if (res.code === '200') {
      ElMessage.success('审批成功')
      data.cancelAuditVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  }).catch(err => {
    data.cancelAuditSubmitting = false
    ElMessage.error('审批失败，请稍后重试')
    console.error(err)
  })
}

const submitAudit = () => {
  if (!data.auditReason || !data.auditReason.trim()) {
    ElMessage.warning('请输入审核说明')
    return
  }

  data.auditSubmitting = true
  
  request.put('/activitySignUp/update', {
    id: data.currentAuditId,
    status: data.auditResult,
    reason: data.auditReason
  }).then(res => {
    data.auditSubmitting = false
    if (res.code === '200') {
      ElMessage.success('处理成功')
      data.auditVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  }).catch(err => {
    data.auditSubmitting = false
    ElMessage.error('处理失败，请稍后重试')
    console.error(err)
  })
}

const showDetailDialog = (row) => {
  data.currentDetail = row
  data.detailVisible = true
}

// 【新增】删除报名记录
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条报名记录吗？', '删除确认', { 
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    request.delete('/activitySignUp/delete/' + row.id).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {
    // 用户取消删除
  })
}

load()
</script>

<style scoped>
</style>
