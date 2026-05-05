NEW_FILE_CODE
D:\anti-fraud\frontend\AntiFraud-frontend\src\views\admin\PublicityComments.vue
<template>
  <div>
    <!-- ===== 搜索区域 ===== -->
    <div class="card" style="margin-bottom: 10px">
      <el-input v-model="data.userName" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入用户名查询"></el-input>
      <el-input v-model="data.publicityTitle" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入宣传标题查询"></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <!-- ===== 表格区域 ===== -->
    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="userName" label="用户名" width="120" align="center" />
        <el-table-column prop="publicityTitle" label="宣传标题" width="200" show-overflow-tooltip align="center" />
        <el-table-column prop="content" label="评论内容" show-overflow-tooltip align="center" />
        <el-table-column prop="time" label="评论时间" width="180" align="center" />
        <el-table-column label="操作" width="150" align="center">
          <template v-slot="scope">
            <el-button type="danger" plain @click="del(scope.row.id)">删除</el-button>
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
  publicityTitle: null,
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  ids: [],
})

// 加载宣传评论数据
const load = () => {
  request.get('/comment/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userName: data.userName,
      publicityTitle: data.publicityTitle
    }
  }).then(res => {
    if (res.code === '200') {
      // 过滤出宣传评论记录（有 publicityId 的记录）
      const records = res.data?.records || []
      data.tableData = records.filter(item => item.publicityId != null)
      data.total = data.tableData.length
    } else {
      ElMessage.error(res.msg)
    }
  })
}
load()

// 删除单个评论记录
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

// 批量删除评论记录
const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning("请选择数据")
    return
  }
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete("/comment/delete/batch", {data: data.ids}).then(res => {
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
  data.publicityTitle = null
  data.pageNum = 1
  load()
}
</script>
