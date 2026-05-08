<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.userName" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入用户姓名查询"></el-input>
      <el-input v-model="data.articleTitle" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入帖子标题查询"></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="userName" label="用户姓名" />
        <el-table-column prop="articleTitle" label="帖子名称" />
        <el-table-column prop="time" label="收藏时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template v-slot="scope">
            <el-button type="info" plain @click="del(scope.row.id)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete} from "@element-plus/icons-vue";

const data = reactive({
  userName: null,
  articleTitle: null,
  pageNum: 1,
  pageSize: 10,
  tableData: [],
  total: 0,
  ids: [],
})

// 加载收藏列表
const load = () => {
  request.get('/collect/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userName: data.userName,
      articleTitle: data.articleTitle
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.records || []
      data.total = res.data?.total || 0
    }
  })
}

// 删除单个收藏
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

// 批量删除收藏
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

// 表格选择变化
const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.id)
}

// 重置查询条件
const reset = () => {
  data.userName = null
  data.articleTitle = null
  load()
}

load()
</script>
