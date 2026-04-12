<template>
  <div style="width: 70%; margin: 20px auto">
    <div style="font-size: 25px">我的报名</div>
    <div style="margin-top: 20px; padding: 20px" class="card">
      <el-table stripe :data="data.tableData">
        <el-table-column prop="activityName" label="活动名称" width="400" show-overflow-tooltip>
          <template v-slot="scope">
            <span style="cursor: pointer;" @click="router.push('/front/activityDetail?id=' + scope.row.activityId)">{{ scope.row.activityName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="signupTime" label="报名时间">
          <template v-slot="scope">
              {{ scope.row.signupTime }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="审核状态">
          <template v-slot="scope">
            <el-tag v-if="scope.row.status === '待审核'" type="warning">{{ scope.row.status }}</el-tag>
            <el-tag v-if="scope.row.status === '审核通过'" type="success">{{ scope.row.status }}</el-tag>
            <el-tag v-if="scope.row.status === '审核拒绝'" type="danger">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="审核说明" />
        <el-table-column label="操作" width="100" fixed="right">
          <template v-slot="scope">
            <el-button type="danger" plain @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="data.total" style="margin-top: 20px">
        <el-pagination @current-change="load" layout="total, prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
      </div>
    </div>

  </div>
</template>

<script setup>
import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";
import {Delete, Edit} from "@element-plus/icons-vue";
import {signUpSelectPage, signUpDelete} from "@/api/activity.js";

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  title: null,
  pageNum: 1,
  pageSize: 5,
  tableData: [],
  total: 0,
})

const load = () => {
  signUpSelectPage({
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    userId: data.user.id
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.records || []
      data.total = res.data?.total || 0
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    signUpDelete(id).then(res => {
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

load()
</script>