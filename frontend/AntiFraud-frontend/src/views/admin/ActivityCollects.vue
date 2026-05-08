NEW_FILE_CODE
D:\anti-fraud\frontend\AntiFraud-frontend\src\views\admin\ActivityCollects.vue
<template>
  <div>
    <!-- ===== 搜索区域 ===== -->
    <div class="card" style="margin-bottom: 10px">
      <el-input v-model="data.userName" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入用户名查询"></el-input>
      <el-input v-model="data.activityTitle" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入活动标题查询"></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <!-- ===== 表格区域 ===== -->
    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="userName" label="用户名" width="150" align="center" />
        <el-table-column prop="activityTitle" label="活动标题" show-overflow-tooltip align="center" />
        <el-table-column prop="time" label="收藏时间" width="180" align="center" />
        <el-table-column label="操作" width="150" align="center">
          <template v-slot="scope">
            <el-button type="info" plain @click="del(scope.row.id)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- ===== 分页区域 ===== -->
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";

const data = reactive({
  userName: null,
  activityTitle: null,
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  ids: [],
})

// 加载活动收藏数据
const load = () => {
  request.get('/collect/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userName: data.userName,
      activityTitle: data.activityTitle
    }
  }).then(res => {
    if (res.code === '200') {
      // 过滤出活动收藏记录（有 activityId 的记录）
      const records = res.data?.records || []
      data.tableData = records.filter(item => item.activityId != null)
      data.total = data.tableData.length
    } else {
      ElMessage.error(res.msg)
    }
  })
}
load()

// 删除单个收藏记录
const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/collect/delete/' + id).then(res => {
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

// 批量删除收藏记录
const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning("请选择数据")
    return
  }
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete("/collect/delete/batch", {data: data.ids}).then(res => {
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

// 选择框变化
const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.id)
}

// 重置搜索
const reset = () => {
  data.userName = null
  data.activityTitle = null
  data.pageNum = 1
  load()
}
</script>
