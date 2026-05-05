<template>
  <div>
    <div style="display: flex; grid-gap: 10px">
      <div style="flex: 1; display: flex; padding: 20px 0; align-items: center" class="card">
        <div style="flex: 1; text-align: center">
          <img src="/src/assets/images/帖子.png" alt="" style="width: 80px; height: 80px">
        </div>
        <div style="flex: 1">
          <div style="font-size: 20px; margin-bottom: 10px">用户帖子总数</div>
          <div style="font-size: 20px; font-weight: bold">{{ data.baseData.article }}</div>
        </div>
      </div>
      <div style="flex: 1; display: flex; padding: 20px 0; align-items: center" class="card">
        <div style="flex: 1; text-align: center">
          <img src="/src/assets/images/文章.png" alt="" style="width: 80px; height: 80px">
        </div>
        <div style="flex: 1">
          <div style="font-size: 20px; margin-bottom: 10px">反诈宣传数量</div>
          <div style="font-size: 20px; font-weight: bold">{{ data.baseData.publicity }}</div>
        </div>
      </div>
      <div style="flex: 1; display: flex; padding: 20px 0; align-items: center" class="card">
        <div style="flex: 1; text-align: center">
          <img src="/src/assets/images/活动.png" alt="" style="width: 80px; height: 80px">
        </div>
        <div style="flex: 1">
          <div style="font-size: 20px; margin-bottom: 10px">反诈活动总数</div>
          <div style="font-size: 20px; font-weight: bold">{{ data.baseData.activity }}</div>
        </div>
      </div>
      <div style="flex: 1; display: flex; padding: 20px 0; align-items: center" class="card">
        <div style="flex: 1; text-align: center">
          <img src="/src/assets/images/男头像.png" alt="" style="width: 80px; height: 80px">
        </div>
        <div style="flex: 1">
          <div style="font-size: 20px; margin-bottom: 10px">平台用户总数</div>
          <div style="font-size: 20px; font-weight: bold">{{ data.baseData.user }}</div>
        </div>
      </div>
      <div style="flex: 1; display: flex; padding: 20px 0; align-items: center" class="card">
        <div style="flex: 1; text-align: center">
          <img src="/src/assets/images/视频.png" alt="" style="width: 80px; height: 80px">
        </div>
        <div style="flex: 1">
          <div style="font-size: 20px; margin-bottom: 10px">宣传视频总数</div>
          <div style="font-size: 20px; font-weight: bold">{{ data.baseData.video }}</div>
        </div>
      </div>
    </div>
    <div class="card" style="margin-top: 10px; height: 500px" id="line"></div>
    <div style="margin-top: 10px; display: flex; grid-gap: 10px">
      <div style="flex: 1; height: 400px" class="card" id="pie1"></div>
      <div style="flex: 1; height: 400px" class="card" id="pie2"></div>
      <div style="flex: 1; height: 400px" class="card" id="pie3"></div>
    </div>
  </div>
</template>

<script setup>

import {reactive, onMounted} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import * as echarts from "echarts";


const data = reactive({
  baseData: {},
})

const loadBase = () => {
  request.get('/dashboard/base').then(res => {
    if (res.code === '200') {
      data.baseData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 平滑折线图配置
let lineOptions = {
  title: {
    text: '最近一周平台用户反诈举报趋势图',
    subtext: '统计维度：最近一周',
    left: 'center'
  },
  legend: {
    data: [],
    template:""
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '20%',
    containLabel: true
  },
  tooltip: {
    trigger: 'item'
  },
  xAxis: {
    name: '日期',
    type: 'category',
    data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
  },
  yAxis: {
    name: '举报数量',
    type: 'value'
  },
  series: [
    {
      name: '举报数量',
      data: [820, 932, 901, 934, 1290, 1330, 1320],
      type: 'line',
      smooth: true,
      markLine: {
        data: [{ type: 'average', name: '系统中近7天反诈举报数量平均值' }]
      },
      markPoint: {
        data: [
          { type: 'max', name: '最大值' },
          { type: 'min', name: '最小值' }
        ]
      },
    },
  ]
}

// 饼图配置1 - 宣传分类占比
let pieOptions1 = {
  title: {
    text: '不同分类下反诈宣传数量占比',
    subtext: '统计维度：反诈分类',
    left: 'center'
  },
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b} : {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: '数量占比',
      type: 'pie',
      radius: '50%',
      center: ['50%', '60%'],
      data: [
        {value: 1, name: '轻微'},
        {value: 735, name: '雀巢咖啡'},
        {value: 580, name: '星巴克咖啡'},
        {value: 484, name: '栖巢咖啡'},
        {value: 300, name: '小武哥咖啡'}
      ]
    }
  ]
}

// 饼图配置2 - 活动分类占比
let pieOptions2 = {
  title: {
    text: '不同分类下反诈活动数量占比',
    subtext: '统计维度：反诈分类',
    left: 'center'
  },
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b} : {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: '数量占比',
      type: 'pie',
      radius: '50%',
      center: ['50%', '60%'],
      data: [
        {value: 1, name: '轻微'},
        {value: 735, name: '雀巢咖啡'},
        {value: 580, name: '星巴克咖啡'},
        {value: 484, name: '栖巢咖啡'},
        {value: 300, name: '小武哥咖啡'}
      ]
    }
  ]
}

// 饼图配置3 - 视频分类占比
let pieOptions3 = {
  title: {
    text: '不同分类下反诈视频数量占比',
    subtext: '统计维度：反诈分类',
    left: 'center'
  },
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b} : {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: '数量占比',
      type: 'pie',
      radius: '50%',
      center: ['50%', '60%'],
      data: [
        {value: 1, name: '轻微'},
        {value: 735, name: '雀巢咖啡'},
        {value: 580, name: '星巴克咖啡'},
        {value: 484, name: '栖巢咖啡'},
        {value: 300, name: '小武哥咖啡'}
      ]
    }
  ]
}

const loadLineData = () => {
  request.get('/dashboard/line').then(res => {
    if (res.code === '200') {
      let chartDom = document.getElementById('line')
      let myChart = echarts.init(chartDom)
      lineOptions.xAxis.data = res.data.x
      lineOptions.series[0].data = res.data.y
      myChart.setOption(lineOptions)
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const loadPie1Data = () => {
  request.get('/dashboard/pie1').then(res => {
    if (res.code === '200') {
      let chartDom = document.getElementById('pie1')
      let myChart = echarts.init(chartDom)
      pieOptions1.series[0].data = res.data
      myChart.setOption(pieOptions1)
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const loadPie2Data = () => {
  request.get('/dashboard/pie2').then(res => {
    if (res.code === '200') {
      let chartDom = document.getElementById('pie2')
      let myChart = echarts.init(chartDom)
      pieOptions2.series[0].data = res.data
      myChart.setOption(pieOptions2)
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const loadPie3Data = () => {
  request.get('/dashboard/pie3').then(res => {
    if (res.code === '200') {
      let chartDom = document.getElementById('pie3')
      let myChart = echarts.init(chartDom)
      pieOptions3.series[0].data = res.data
      myChart.setOption(pieOptions3)
    } else {
      ElMessage.error(res.msg)
    }
  })
}

onMounted(() => {
  loadBase()
  loadLineData()
  loadPie1Data()
  loadPie2Data()
  loadPie3Data()
})

</script>
