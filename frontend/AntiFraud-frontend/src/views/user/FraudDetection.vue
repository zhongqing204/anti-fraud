<template>
  <div class="fraud-detection-container">
    <div class="page-header">
      <h2><el-icon><Warning /></el-icon> AI智能诈骗识别</h2>
      <p class="subtitle">基于机器学习算法，智能识别各类诈骗信息</p>
    </div>

    <!-- 检测输入区域 -->
    <el-card class="detection-card">
      <template #header>
        <div class="card-header">
          <span><el-icon><Edit /></el-icon> 输入待检测内容</span>
        </div>
      </template>
      
      <el-form label-width="100px">
        <el-form-item label="诈骗类型">
          <el-select v-model="detectForm.fraudType" placeholder="选择诈骗类型（可选）" style="width: 100%">
            <el-option label="自动识别" value="" />
            <el-option label="网络诈骗" value="network" />
            <el-option label="电话诈骗" value="phone" />
            <el-option label="短信诈骗" value="sms" />
            <el-option label="邮件诈骗" value="email" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="检测内容" required>
          <el-input
            v-model="detectForm.content"
            type="textarea"
            :rows="8"
            placeholder="请粘贴可疑的短信、聊天记录、邮件等内容...&#10;&#10;例如：&#10;恭喜您中奖100万元，请点击链接http://xxx.com领取，需要提供银行卡号和验证码"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleDetect" 
            :loading="detecting"
            size="large"
          >
            <el-icon><Search /></el-icon> 开始检测
          </el-button>
          <el-button @click="resetForm" size="large">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 检测结果展示 -->
    <el-card v-if="detectResult" class="result-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><CircleCheck /></el-icon> 检测结果</span>
        </div>
      </template>
      
      <div class="result-content">
        <!-- 风险等级 -->
        <div class="risk-level-section">
          <div class="risk-badge" :class="detectResult.riskLevel">
            <el-icon :size="40"><component :is="getRiskIcon()" /></el-icon>
            <div class="risk-text">
              <div class="risk-title">{{ getRiskTitle() }}</div>
              <div class="risk-score">风险分数：{{ detectResult.riskScore }}/100</div>
            </div>
          </div>
        </div>
        
        <!-- 诈骗类型 -->
        <div class="info-section">
          <h4><el-icon><Flag /></el-icon> 诈骗类型</h4>
          <el-tag :type="getTypeTagType()" size="large">
            {{ getFraudTypeName(detectResult.fraudType) }}
          </el-tag>
        </div>
        
        <!-- 检测到的关键词 -->
        <div v-if="detectResult.keywords && detectResult.keywords.length > 0" class="info-section">
          <h4><el-icon><Key /></el-icon> 可疑关键词</h4>
          <div class="keywords-cloud">
            <el-tag 
              v-for="(keyword, index) in detectResult.keywords" 
              :key="index"
              type="danger"
              effect="plain"
              style="margin: 5px"
            >
              {{ keyword }}
            </el-tag>
          </div>
        </div>
        
        <!-- 检测结果描述 -->
        <div class="info-section">
          <h4><el-icon><InfoFilled /></el-icon> 结果分析</h4>
          <div class="description-box">
            {{ detectResult.description }}
          </div>
        </div>
        
        <!-- 建议措施 -->
        <div class="info-section">
          <h4><el-icon><Bell /></el-icon> 建议措施</h4>
          <div class="suggestion-box" v-html="formatSuggestion(detectResult.suggestion)"></div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button type="warning" @click="goToReport">
            <el-icon><Warning /></el-icon> 提交举报
          </el-button>
          <el-button type="primary" @click="saveDetection">
            <el-icon><Document /></el-icon> 保存记录
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 检测历史 -->
    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span><el-icon><Clock /></el-icon> 检测历史</span>
          <el-button size="small" @click="loadHistory">刷新</el-button>
        </div>
      </template>
      
      <el-table :data="historyList" stripe style="width: 100%">
        <el-table-column prop="detectTime" label="检测时间" width="180" />
        <el-table-column label="风险等级" width="120">
          <template #default="{ row }">
            <el-tag :type="getRiskTagType(row.riskLevel)" size="small">
              {{ getRiskLevelName(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="诈骗类型" width="120">
          <template #default="{ row }">
            {{ getFraudTypeName(row.fraudType) }}
          </template>
        </el-table-column>
        <el-table-column prop="riskScore" label="风险分数" width="100">
          <template #default="{ row }">
            <span :style="{ color: getScoreColor(row.riskScore), fontWeight: 'bold' }">
              {{ row.riskScore }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="检测内容" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadHistory"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning, Edit, Search, CircleCheck, Flag, Key, InfoFilled, Bell, Document, Clock } from '@element-plus/icons-vue'
import { detectFraud, getDetectionHistory } from '@/api/fraudDetection'
import router from '@/router'

const user = JSON.parse(localStorage.getItem('xm-user') || '{}')

const detectForm = reactive({
  content: '',
  fraudType: ''
})

const detecting = ref(false)
const detectResult = ref(null)

const historyList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 执行检测
const handleDetect = async () => {
  if (!detectForm.content.trim()) {
    ElMessage.warning('请输入待检测内容')
    return
  }
  
  detecting.value = true
  
  try {
    const res = await detectFraud({
      content: detectForm.content,
      userId: user.id,
      userName: user.name
    })
    
    if (res.code === '200') {
      detectResult.value = res.data
      ElMessage.success('检测完成')
      
      // 如果是高风险，弹出警告
      if (res.data.riskLevel === 'high') {
        ElMessageBox.alert(
          '检测到高风险诈骗内容！请务必谨慎对待，不要轻信对方，不要转账或提供个人信息。',
          '⚠️ 高风险警告',
          {
            confirmButtonText: '我知道了',
            type: 'warning'
          }
        )
      }
    } else {
      ElMessage.error(res.msg || '检测失败')
    }
  } catch (error) {
    console.error('检测失败', error)
    ElMessage.error('检测失败，请稍后重试')
  } finally {
    detecting.value = false
  }
}

// 重置表单
const resetForm = () => {
  detectForm.content = ''
  detectForm.fraudType = ''
  detectResult.value = null
}

// 加载检测历史
const loadHistory = async () => {
  try {
    const res = await getDetectionHistory({
      userId: user.id,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    
    if (res.code === '200') {
      historyList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载历史失败', error)
  }
}

// 查看详情
const viewDetail = (row) => {
  detectResult.value = {
    riskLevel: row.riskLevel,
    riskScore: row.riskScore,
    fraudType: row.fraudType,
    description: row.resultDescription,
    suggestion: row.suggestion,
    keywords: row.keywords ? JSON.parse(row.keywords) : []
  }
  
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 跳转到举报页面
const goToReport = () => {
  router.push('/front/report')
}

// 保存检测记录
const saveDetection = () => {
  ElMessage.success('检测记录已保存')
  loadHistory()
}

// 获取风险图标
const getRiskIcon = () => {
  if (!detectResult.value) return 'QuestionFilled'
  switch (detectResult.value.riskLevel) {
    case 'high': return 'CircleCloseFilled'
    case 'medium': return 'WarningFilled'
    case 'low': return 'CircleCheckFilled'
    default: return 'QuestionFilled'
  }
}

// 获取风险标题
const getRiskTitle = () => {
  if (!detectResult.value) return ''
  switch (detectResult.value.riskLevel) {
    case 'high': return '高风险 - 疑似诈骗'
    case 'medium': return '中风险 - 需谨慎'
    case 'low': return '低风险 - 相对安全'
    default: return '未知风险'
  }
}

// 获取风险标签类型
const getRiskTagType = (level) => {
  switch (level) {
    case 'high': return 'danger'
    case 'medium': return 'warning'
    case 'low': return 'success'
    default: return 'info'
  }
}

// 获取风险等级名称
const getRiskLevelName = (level) => {
  switch (level) {
    case 'high': return '高风险'
    case 'medium': return '中风险'
    case 'low': return '低风险'
    default: return '未知'
  }
}

// 获取类型标签类型
const getTypeTagType = () => {
  if (!detectResult.value) return 'info'
  return 'danger'
}

// 获取诈骗类型名称
const getFraudTypeName = (type) => {
  switch (type) {
    case 'network': return '网络诈骗'
    case 'phone': return '电话诈骗'
    case 'sms': return '短信诈骗'
    case 'email': return '邮件诈骗'
    default: return '疑似诈骗'
  }
}

// 获取分数颜色
const getScoreColor = (score) => {
  if (score >= 70) return '#f56c6c'
  if (score >= 40) return '#e6a23c'
  return '#67c23a'
}

// 格式化建议文本
const formatSuggestion = (text) => {
  if (!text) return ''
  return text.replace(/\n/g, '<br/>')
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.fraud-detection-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  font-size: 24px;
  margin: 0 0 10px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.subtitle {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

.detection-card,
.result-card,
.history-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.result-content {
  padding: 10px 0;
}

.risk-level-section {
  margin-bottom: 30px;
}

.risk-badge {
  padding: 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.risk-badge.high {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  border: 2px solid #f56c6c;
  color: #f56c6c;
}

.risk-badge.medium {
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
  border: 2px solid #e6a23c;
  color: #e6a23c;
}

.risk-badge.low {
  background: linear-gradient(135deg, #f0f9ff 0%, #e1f3ff 100%);
  border: 2px solid #67c23a;
  color: #67c23a;
}

.risk-text {
  flex: 1;
}

.risk-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 5px;
}

.risk-score {
  font-size: 16px;
  opacity: 0.9;
}

.info-section {
  margin-bottom: 25px;
}

.info-section h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.keywords-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.description-box,
.suggestion-box {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 6px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.suggestion-box {
  background: #fff3e0;
  border-left: 4px solid #ff9800;
}

.action-buttons {
  margin-top: 30px;
  display: flex;
  gap: 15px;
  justify-content: center;
}
</style>
