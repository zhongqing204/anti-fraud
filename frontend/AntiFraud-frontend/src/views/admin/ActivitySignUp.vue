<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.userName" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入用户名称查询"></el-input>
      <el-input v-model="data.activityName" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入活动名称查询"></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="userName" label="用户名称" align="center" />
        <el-table-column prop="activityName" label="活动名称" width="200" show-overflow-tooltip align="center" />
        <el-table-column prop="signupTime" label="报名时间" align="center" />
        <el-table-column prop="status" label="审核状态" align="center">
          <template v-slot="scope">
            <el-tag type="warning" v-if="scope.row.status === '待审核'">{{ scope.row.status }}</el-tag>
            <el-tag type="success" v-if="scope.row.status === '审核通过'">{{ scope.row.status }}</el-tag>
            <el-tag type="danger" v-if="scope.row.status === '审核拒绝'">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="审核说明" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template v-slot="scope">
            <el-button type="primary" plain @click="handleEdit(scope.row)">审核</el-button>
            <el-button type="danger" plain @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <el-dialog title="审核信息" v-model="data.formVisible" width="40%" destroy-on-close>
      <el-form :model="data.form" label-width="80px" style="padding: 20px">
        <el-form-item prop="status" label="审核状态">
          <el-select v-model="data.form.status" placeholder="请选择状态">
            <el-option value="待审核" label="待审核"></el-option>
            <el-option value="审核通过" label="审核通过"></el-option>
            <el-option value="审核拒绝" label="审核拒绝"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="reason" label="审核说明">
          <el-input v-model="data.form.reason" placeholder="请输入审核说明"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="update">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="帖子内容" v-model="data.viewVisible" width="50%" destroy-on-close>
      <div style="padding: 10px 20px" v-html="data.viewContent"></div>
    </el-dialog>
  </div>
</template>

<script setup>

import {reactive, ref} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit} from "@element-plus/icons-vue";
import {signUpSelectPage, signUpDelete, signUpDeleteBatch} from "@/api/activity.js";


const data = reactive({
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 5,
  total: 0,
  userName: null,
  activityName: null,
  ids: [],
  rules: {
    status: [
      { required: true, message: '请选择审核状态', trigger: 'blur' }
    ],
  }
})

const load = () => {
  signUpSelectPage({
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    userName: data.userName,
    activityName: data.activityName
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
  request.put('/activitySignUp/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
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

const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning("请选择数据")
    return
  }
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    signUpDeleteBatch(data.ids).then(res => {
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
  data.userName = null
  data.activityName = null
  load()
}

load()
</script>