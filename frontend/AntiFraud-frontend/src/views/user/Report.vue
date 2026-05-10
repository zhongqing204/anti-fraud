<template>
  <div style="width: 80%; margin: 20px auto">
    <!-- 页面标题 -->
    <div style="font-size: 24px; font-weight: bold; margin-bottom: 20px; color: #303133;">
      <el-icon style="margin-right: 8px; vertical-align: middle;"><Warning /></el-icon>
      反诈举报中心
    </div>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" type="border-card" style="margin-bottom: 20px;">
      <!-- 举报表单 Tab -->
      <el-tab-pane label="提交举报" name="report">
        <!-- 举报指南卡片 -->
        <div class="card" style="padding: 20px; margin-bottom: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white;">
          <div style="font-size: 18px; font-weight: bold; margin-bottom: 10px;">
            <el-icon><InfoFilled /></el-icon> 举报须知
          </div>
          <div style="line-height: 1.8; font-size: 14px;">
            <div>• 请如实填写举报内容，提供详细信息有助于我们快速处理</div>
            <div>• 可上传相关证据图片（截图、聊天记录等），最多9张</div>
            <div>• 我们将在3-7个工作日内完成核查并反馈处理结果</div>
            <div>• 严禁恶意举报，违者将承担相应法律责任</div>
          </div>
        </div>

        <!-- 举报表单卡片 -->
        <div class="card" style="padding: 30px; margin-bottom: 20px;">
          <div style="font-size: 18px; font-weight: bold; margin-bottom: 20px; color: #303133;">
            <el-icon style="margin-right: 5px;"><Edit /></el-icon>
            填写举报信息
          </div>
          
          <el-form label-width="100px" label-position="top">
            <!-- 举报类型 -->
            <el-form-item label="举报类型" required>
              <el-select v-model="data.category" placeholder="请选择诈骗类型" style="width: 100%;">
                <el-option label="网络诈骗" value="网络诈骗">
                  <span style="float: left">网络诈骗</span>
                  <span style="float: right; color: #8492a6; font-size: 12px">钓鱼网站、虚假投资等</span>
                </el-option>
                <el-option label="电话诈骗" value="电话诈骗">
                  <span style="float: left">电话诈骗</span>
                  <span style="float: right; color: #8492a6; font-size: 12px">冒充公检法、客服等</span>
                </el-option>
                <el-option label="短信诈骗" value="短信诈骗">
                  <span style="float: left">短信诈骗</span>
                  <span style="float: right; color: #8492a6; font-size: 12px">中奖信息、虚假链接等</span>
                </el-option>
                <el-option label="邮件诈骗" value="邮件诈骗">
                  <span style="float: left">邮件诈骗</span>
                  <span style="float: right; color: #8492a6; font-size: 12px">钓鱼邮件、病毒附件等</span>
                </el-option>
                <el-option label="其他" value="其他">
                  <span style="float: left">其他类型</span>
                  <span style="float: right; color: #8492a6; font-size: 12px">不属于以上分类</span>
                </el-option>
              </el-select>
            </el-form-item>

            <!-- 举报详情 -->
            <el-form-item label="详细描述" required>
              <el-input 
                type="textarea" 
                :rows="8" 
                v-model="data.content" 
                placeholder="请详细描述您遇到的诈骗情况，包括：&#10;1. 诈骗发生的时间和地点&#10;2. 诈骗分子的联系方式（电话、微信、QQ等）&#10;3. 诈骗手段和过程&#10;4. 涉及的金额（如有）&#10;5. 其他重要信息"
                maxlength="2000"
                show-word-limit
              />
            </el-form-item>

            <!-- 证据上传 -->
            <el-form-item label="上传证据">
              <div style="border: 2px dashed #dcdfe6; border-radius: 8px; padding: 20px; text-align: center; background: #fafafa;">
                <el-upload
                    :action="baseUrl + '/file/upload'"
                    :on-success="handleFileUpload"
                    :on-remove="handleFileRemove"
                    :file-list="data.fileList"
                    multiple
                    :limit="9"
                    accept=".jpg,.jpeg,.png,.gif,.webp,.bmp"
                    list-type="picture-card"
                    :on-exceed="handleExceed"
                >
                  <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%;">
                    <el-icon :size="40" color="#8c939d"><Plus /></el-icon>
                    <div style="margin-top: 10px; color: #8c939d; font-size: 14px;">点击或拖拽上传图片</div>
                    <div style="margin-top: 5px; color: #c0c4cc; font-size: 12px;">支持 JPG、PNG、GIF 格式，最多9张</div>
                  </div>
                </el-upload>
              </div>
              <div style="margin-top: 10px; color: #909399; font-size: 12px;">
                <el-icon><InfoFilled /></el-icon>
                建议上传：聊天记录截图、转账记录、诈骗网站截图、通话记录等证据
              </div>
            </el-form-item>

            <!-- 联系方式（可选） -->
            <el-form-item label="联系方式（选填）">
              <el-input 
                v-model="data.contact" 
                placeholder="请输入您的手机号或邮箱，方便我们向您反馈处理结果"
                maxlength="50"
              />
              <div style="margin-top: 5px; color: #909399; font-size: 12px;">
                我们将严格保护您的个人信息安全
              </div>
            </el-form-item>

            <!-- 提交按钮 -->
            <el-form-item>
              <el-button 
                type="primary" 
                @click="submit" 
                :loading="data.submitting"
                size="large"
                style="width: 100%;"
              >
                <el-icon style="margin-right: 5px;"><Promotion /></el-icon>
                提交举报
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 处理流程说明卡片 -->
        <div class="card" style="padding: 25px; margin-bottom: 20px;">
          <div style="font-size: 18px; font-weight: bold; margin-bottom: 20px; color: #303133;">
            <el-icon style="margin-right: 5px;"><Clock /></el-icon>
            处理流程
          </div>
          <el-steps :active="0" finish-status="success" simple>
            <el-step title="提交举报" description="填写并提交举报信息"></el-step>
            <el-step title="审核受理" description="工作人员将在1-2个工作日内审核"></el-step>
            <el-step title="调查处理" description="展开调查并进行处理"></el-step>
            <el-step title="反馈结果" description="向举报人反馈处理结果"></el-step>
          </el-steps>
          <div style="margin-top: 15px; padding: 15px; background: #f4f4f5; border-radius: 4px; color: #606266; font-size: 13px;">
            <el-icon><InfoFilled /></el-icon>
            您可以在"我的举报"中随时查看举报处理进度
          </div>
        </div>

        <!-- 常见诈骗类型说明卡片 -->
        <div class="card" style="padding: 25px;">
          <div style="font-size: 18px; font-weight: bold; margin-bottom: 20px; color: #303133;">
            <el-icon style="margin-right: 5px;"><QuestionFilled /></el-icon>
            常见诈骗类型说明
          </div>
          <el-collapse accordion>
            <el-collapse-item name="1">
              <template #title>
                <div style="font-weight: bold; color: #409EFF;">
                  <el-icon><Monitor /></el-icon>
                  网络诈骗
                </div>
              </template>
              <div style="line-height: 1.8; color: #606266;">
                <strong>典型场景：</strong><br/>
                • 虚假投资理财平台<br/>
                • 钓鱼网站窃取账号密码<br/>
                • 网络购物诈骗<br/>
                • 刷单返利诈骗<br/>
                • 网络游戏虚拟物品交易诈骗<br/><br/>
                <strong>防范要点：</strong>不轻信高收益投资，不点击不明链接，选择正规交易平台
              </div>
            </el-collapse-item>

            <el-collapse-item name="2">
              <template #title>
                <div style="font-weight: bold; color: #67C23A;">
                  <el-icon><Phone /></el-icon>
                  电话诈骗
                </div>
              </template>
              <div style="line-height: 1.8; color: #606266;">
                <strong>典型场景：</strong><br/>
                • 冒充公检法人员<br/>
                • 冒充银行或客服<br/>
                • "猜猜我是谁"诈骗<br/>
                • 虚假中奖通知<br/>
                • 绑架勒索电话<br/><br/>
                <strong>防范要点：</strong>核实对方身份，不透露验证码，遇到可疑情况及时报警
              </div>
            </el-collapse-item>

            <el-collapse-item name="3">
              <template #title>
                <div style="font-weight: bold; color: #E6A23C;">
                  <el-icon><Message /></el-icon>
                  短信诈骗
                </div>
              </template>
              <div style="line-height: 1.8; color: #606266;">
                <strong>典型场景：</strong><br/>
                • 假冒银行发送钓鱼链接<br/>
                • 虚假ETC认证短信<br/>
                • 快递理赔诈骗<br/>
                • 子女求助短信<br/>
                • 积分兑换诈骗<br/><br/>
                <strong>防范要点：</strong>不点击短信中的链接，通过官方渠道核实信息
              </div>
            </el-collapse-item>

            <el-collapse-item name="4">
              <template #title>
                <div style="font-weight: bold; color: #F56C6C;">
                  <el-icon><ChatDotRound /></el-icon>
                  邮件诈骗
                </div>
              </template>
              <div style="line-height: 1.8; color: #606266;">
                <strong>典型场景：</strong><br/>
                • 钓鱼邮件窃取账号<br/>
                • 携带病毒的附件<br/>
                • 假冒公司高管要求转账<br/>
                • 虚假发票或合同<br/>
                • 海外遗产诈骗<br/><br/>
                <strong>防范要点：</strong>谨慎打开陌生邮件附件，核实发件人身份
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>

        <!-- 紧急提示卡片 -->
        <div class="card" style="padding: 20px; margin-top: 20px; background: #fef0f0; border-left: 4px solid #f56c6c;">
          <div style="display: flex; align-items: start;">
            <el-icon :size="24" color="#f56c6c" style="margin-right: 10px; margin-top: 2px;"><WarningFilled /></el-icon>
            <div>
              <div style="font-weight: bold; color: #f56c6c; margin-bottom: 8px;">紧急情况提示</div>
              <div style="color: #606266; line-height: 1.6; font-size: 14px;">
                如果您正在遭受诈骗或已经造成财产损失，请立即：<br/>
                1️⃣ 拨打 <strong style="color: #f56c6c; font-size: 16px;">110</strong> 报警电话<br/>
                2️⃣ 拨打 <strong style="color: #f56c6c; font-size: 16px;">96110</strong> 反电信网络诈骗专用号码<br/>
                3️⃣ 联系银行冻结相关账户<br/>
                4️⃣ 保存所有证据（聊天记录、转账凭证等）
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- AI诈骗识别 Tab -->
      <el-tab-pane label="AI诈骗识别" name="detection">
        <div class="fraud-detection-container">
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
          <el-card v-if="detectResult" class="result-card" shadow="hover" style="margin-top: 20px;">
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
                <el-button type="warning" @click="switchToReport">
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
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import request from "@/utils/request.js";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Warning,
  InfoFilled,
  Edit,
  Plus,
  Promotion,
  Clock,
  QuestionFilled,
  Monitor,
  Phone,
  Message,
  ChatDotRound,
  WarningFilled,
  Search,
  CircleCheck,
  Flag,
  Key,
  Bell,
  Document
} from "@element-plus/icons-vue";
import { detectFraud, getDetectionHistory } from '@/api/fraudDetection'

const baseUrl = import.meta.env.VITE_BASE_URL

const activeTab = ref('report')

const data = reactive({
  content: null,
  category: null,
  contact: null,
  fileList: [],
  fileUrls: [],
  submitting: false
})

// AI诈骗识别相关
const detectForm = reactive({
  content: '',
  fraudType: ''
})

const detecting = ref(false)
const detectResult = ref(null)

// 检测历史相关
const historyList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const user = JSON.parse(localStorage.getItem('xm-user') || '{}')

onMounted(() => {
  loadHistory()
})

// 提交举报
const submit = () => {
  if (!data.category) {
    ElMessage.warning('请选择举报类型')
    return
  }
  
  if (!data.content || !data.content.trim()) {
    ElMessage.warning('请输入举报内容')
    return
  }
  
  if (data.content.length < 20) {
    ElMessage.warning('举报内容至少需要20个字符，请详细描述情况')
    return
  }

  if (!user.id) {
    ElMessage.warning('请先登录')
    return
  }

  data.submitting = true
  
  request.post('/report/add', {
    userId: user.id,
    userName: user.name,
    category: data.category,
    content: data.content,
    files: data.fileUrls.join(','),
    status: '待处理',
    priority: '一般'
  }).then(res => {
    data.submitting = false
    if (res.code === '200') {
      ElMessage.success('举报提交成功！我们会尽快处理，感谢您的参与')
      data.content = null
      data.category = null
      data.contact = null
      data.fileList = []
      data.fileUrls = []
    } else {
      ElMessage.error(res.msg)
    }
  }).catch(err => {
    data.submitting = false
    ElMessage.error('提交失败，请稍后重试')
    console.error(err)
  })
}

// 文件上传成功回调
const handleFileUpload = (response, file, fileList) => {
  if (response.code === '200') {
    data.fileUrls.push(response.data)
    data.fileList = fileList
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

// 文件移除回调
const handleFileRemove = (file, fileList) => {
  const index = data.fileList.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    data.fileUrls.splice(index, 1)
  }
  data.fileList = fileList
}

// 超出文件数量限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传9张图片')
}

// 切换到举报Tab并预填内容
const switchToReport = () => {
  if (detectResult.value) {
    activeTab.value = 'report'
    data.category = '其他'
    data.content = detectResult.value.description || ''
  }
}

// AI诈骗识别相关函数
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

// 保存检测记录
const saveDetection = () => {
  ElMessage.success('检测记录已保存')
  loadHistory()
}

const getRiskIcon = () => {
  if (!detectResult.value) return 'QuestionFilled'
  switch (detectResult.value.riskLevel) {
    case 'high': return 'CircleCloseFilled'
    case 'medium': return 'WarningFilled'
    case 'low': return 'CircleCheckFilled'
    default: return 'QuestionFilled'
  }
}

const getRiskTitle = () => {
  if (!detectResult.value) return ''
  switch (detectResult.value.riskLevel) {
    case 'high': return '高风险 - 疑似诈骗'
    case 'medium': return '中风险 - 需谨慎'
    case 'low': return '低风险 - 相对安全'
    default: return '未知风险'
  }
}

const getRiskTagType = (level) => {
  switch (level) {
    case 'high': return 'danger'
    case 'medium': return 'warning'
    case 'low': return 'success'
    default: return 'info'
  }
}

const getRiskLevelName = (level) => {
  switch (level) {
    case 'high': return '高风险'
    case 'medium': return '中风险'
    case 'low': return '低风险'
    default: return '未知'
  }
}

const getTypeTagType = () => {
  if (!detectResult.value) return 'info'
  return 'danger'
}

const getFraudTypeName = (type) => {
  switch (type) {
    case 'network': return '网络诈骗'
    case 'phone': return '电话诈骗'
    case 'sms': return '短信诈骗'
    case 'email': return '邮件诈骗'
    default: return '疑似诈骗'
  }
}

const getScoreColor = (score) => {
  if (score >= 70) return '#f56c6c'
  if (score >= 40) return '#e6a23c'
  return '#67c23a'
}

const formatSuggestion = (text) => {
  if (!text) return ''
  return text.replace(/\n/g, '<br/>')
}
</script>

<style scoped>
.fraud-detection-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.detection-card,
.result-card,
.history-card {
  margin-bottom: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.risk-level-section {
  margin-bottom: 30px;
}

.risk-badge {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  border-radius: 8px;
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
