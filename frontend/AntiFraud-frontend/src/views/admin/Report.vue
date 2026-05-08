<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.content" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入举报内容查询"></el-input>
      <el-select v-model="data.category" placeholder="举报类型" style="width: 150px; margin-right: 10px" clearable>
        <el-option label="网络诈骗" value="网络诈骗"></el-option>
        <el-option label="电话诈骗" value="电话诈骗"></el-option>
        <el-option label="短信诈骗" value="短信诈骗"></el-option>
        <el-option label="邮件诈骗" value="邮件诈骗"></el-option>
        <el-option label="其他" value="其他"></el-option>
      </el-select>
      <el-select v-model="data.status" placeholder="处理状态" style="width: 120px; margin-right: 10px" clearable>
        <el-option label="待处理" value="待处理"></el-option>
        <el-option label="处理中" value="处理中"></el-option>
        <el-option label="已处理" value="已处理"></el-option>
      </el-select>
      <el-select v-model="data.priority" placeholder="优先级" style="width: 120px; margin-right: 10px" clearable>
        <el-option label="紧急" value="紧急"></el-option>
        <el-option label="一般" value="一般"></el-option>
        <el-option label="低" value="低"></el-option>
      </el-select>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
      <el-button type="primary" plain @click="batchProcess">批量处理</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="content" label="举报内容" width="350" show-overflow-tooltip />
        <el-table-column prop="category" label="举报类型" width="100">
          <template v-slot="scope">
            <el-tag v-if="scope.row.category" type="info">{{ scope.row.category }}</el-tag>
            <span v-else style="color: #999">未分类</span>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80">
          <template v-slot="scope">
            <el-tag v-if="scope.row.priority === '紧急'" type="danger">{{ scope.row.priority }}</el-tag>
            <el-tag v-else-if="scope.row.priority === '一般'" type="warning">{{ scope.row.priority }}</el-tag>
            <el-tag v-else type="success">{{ scope.row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="files" label="附件" width="100">
          <template v-slot="scope">
            <div v-if="scope.row.files">
              <el-button 
                link 
                type="primary" 
                @click="previewImages(scope.row.files)"
              >
                <el-icon><Link /></el-icon>
                查看
              </el-button>
            </div>
            <span v-else style="color: #999">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="举报人" width="100" />
        <el-table-column prop="time" label="举报时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template v-slot="scope">
            <el-tag v-if="scope.row.status === '已处理'" type="success">{{ scope.row.status }}</el-tag>
            <el-tag v-else-if="scope.row.status === '处理中'" type="warning">{{ scope.row.status }}</el-tag>
            <el-tag v-else type="danger">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="处理说明" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template v-slot="scope">
            <el-button type="primary" plain @click="handleEdit(scope.row)">处理</el-button>
            <el-button type="danger" plain @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <el-dialog title="处理举报" v-model="data.formVisible" width="50%" destroy-on-close draggable>
      <el-form :model="data.form" label-width="100px" style="padding: 20px">
        <el-form-item label="举报内容">
          <div style="color: #606266; white-space: pre-wrap;">{{ data.form.content }}</div>
        </el-form-item>
        <el-form-item label="举报类型">
          <el-select v-model="data.form.category" placeholder="请选择举报类型" style="width: 100%">
            <el-option label="网络诈骗" value="网络诈骗"></el-option>
            <el-option label="电话诈骗" value="电话诈骗"></el-option>
            <el-option label="短信诈骗" value="短信诈骗"></el-option>
            <el-option label="邮件诈骗" value="邮件诈骗"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="data.form.priority" placeholder="请选择优先级" style="width: 100%">
            <el-option label="紧急" value="紧急"></el-option>
            <el-option label="一般" value="一般"></el-option>
            <el-option label="低" value="低"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="data.form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待处理" value="待处理"></el-option>
            <el-option label="处理中" value="处理中"></el-option>
            <el-option label="已处理" value="已处理"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="data.form.reason" type="textarea" :rows="3" placeholder="请输入处理说明（将反馈给用户）"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="update">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="批量处理" v-model="data.batchProcessVisible" width="40%" destroy-on-close draggable>
      <el-form label-width="100px" style="padding: 20px">
        <el-form-item label="处理状态">
          <el-select v-model="data.batchForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待处理" value="待处理"></el-option>
            <el-option label="处理中" value="处理中"></el-option>
            <el-option label="已处理" value="已处理"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="data.batchForm.reason" type="textarea" :rows="3" placeholder="请输入处理说明"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.batchProcessVisible = false">取 消</el-button>
          <el-button type="primary" @click="confirmBatchProcess">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="data.previewVisible" width="80%" destroy-on-close class="image-preview-dialog" draggable>
      <div class="image-preview-container">
        <div class="preview-nav prev" @click="prevImage">
          <el-icon :size="40"><ArrowLeft /></el-icon>
        </div>
        <div class="preview-image">
          <img :src="data.previewImages[data.currentImageIndex]" alt="预览图片" />
        </div>
        <div class="preview-nav next" @click="nextImage">
          <el-icon :size="40"><ArrowRight /></el-icon>
        </div>
      </div>
      <div class="preview-indicator">
        {{ data.currentImageIndex + 1 }} / {{ data.previewImages.length }}
      </div>
    </el-dialog>

  </div>
</template>

<script setup>

import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {Link, ArrowLeft, ArrowRight} from "@element-plus/icons-vue";

const baseUrl = import.meta.env.VITE_BASE_URL


const data = reactive({
  formVisible: false,
  batchProcessVisible: false,
  form: {},
  batchForm: {
    status: '',
    reason: ''
  },
  tableData: [],
  pageNum: 1,
  pageSize: 5,
  total: 0,
  content: null,
  category: null,
  status: null,
  priority: null,
  ids: [],
  previewVisible: false,
  previewImages: [],
  currentImageIndex: 0,
})


const load = () => {
  request.get('/report/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      content: data.content,
      category: data.category,
      status: data.status,
      priority: data.priority
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.records || []
      data.total = res.data?.total
    }
  })
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const update = () => {
  request.put('/report/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/report/delete/' + id).then(res => {
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

const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning("请选择数据")
    return
  }
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete("/report/delete/batch", {data: data.ids}).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}

const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.id)
}

const reset = () => {
  data.content = null
  data.category = null
  data.status = null
  data.priority = null
  load()
}

const parseFiles = (filesStr) => {
  if (!filesStr) return []
  try {
    return JSON.parse(filesStr)
  } catch (e) {
    return filesStr.split(',').filter(url => url.trim())
  }
}

const previewImages = (filesStr) => {
  const images = parseFiles(filesStr)
  if (images.length === 0) {
    ElMessage.warning('暂无图片')
    return
  }
  data.previewImages = images.map(url => baseUrl + url)
  data.currentImageIndex = 0
  data.previewVisible = true
}

const prevImage = () => {
  if (data.currentImageIndex > 0) {
    data.currentImageIndex--
  } else {
    data.currentImageIndex = data.previewImages.length - 1
  }
}

const nextImage = () => {
  if (data.currentImageIndex < data.previewImages.length - 1) {
    data.currentImageIndex++
  } else {
    data.currentImageIndex = 0
  }
}

const batchProcess = () => {
  if (!data.ids.length) {
    ElMessage.warning("请选择要处理的举报")
    return
  }
  data.batchForm = {
    status: '',
    reason: ''
  }
  data.batchProcessVisible = true
}

const confirmBatchProcess = () => {
  if (!data.batchForm.status) {
    ElMessage.warning("请选择处理状态")
    return
  }
  request.post('/report/batchUpdate', {
    ids: data.ids,
    status: data.batchForm.status,
    reason: data.batchForm.reason
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('批量处理成功')
      data.batchProcessVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

load()
</script>

<style scoped>
.image-preview-dialog :deep(.el-dialog__body) {
  padding: 0;
  background-color: #000;
}

.image-preview-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 70vh;
  position: relative;
}

.preview-image {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.preview-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.preview-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  color: #fff;
  cursor: pointer;
  padding: 20px;
  transition: all 0.3s;
  z-index: 10;
}

.preview-nav:hover {
  color: #409eff;
}

.preview-nav.prev {
  left: 0;
}

.preview-nav.next {
  right: 0;
}

.preview-indicator {
  text-align: center;
  color: #000;
  padding: 15px;
  font-size: 14px;
}
</style>
