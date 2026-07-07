<template>
  <div v-loading="loading">
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6" v-for="(v,k) in overview" :key="k">
        <el-card shadow="never" class="glass-card">
          <el-statistic :title="labelMap[k]||k" :value="Number(v)" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>各类别正确率</template>
          <div ref="catChart" style="height:260px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>各题型正确率</template>
          <div ref="typeChart" style="height:260px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top:16px">
      <template #header>最近考试活动</template>
      <el-table :data="recentActivity" border stripe>
        <el-table-column prop="exam_date" label="日期" />
        <el-table-column prop="exam_count" label="考试场次" />
        <el-table-column prop="avg_score" label="平均分" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../api/request'

const loading = ref(false)
const overview = ref({})
const recentActivity = ref([])
const catChart = ref(null)
const typeChart = ref(null)
const labelMap = { users:'用户数', questions:'题目数', papers:'试卷数', exams:'考试次数', scores:'成绩记录' }
const typeMap = { single_choice:'单选', multi_choice:'多选', true_false:'判断', fill_blank:'填空', essay:'简答' }

onMounted(async () => {
  loading.value = true
  try {
    const [ov, perf, recent] = await Promise.all([
      request.get('/analysis/overview'),
      request.get('/analysis/performance'),
      request.get('/analysis/recent-activity'),
    ])
    if (ov && ov.code === 200) overview.value = ov.data
    if (perf && perf.code === 200) {
      await nextTick()
      initCategoryChart(perf.data.byCategory || [])
      initTypeChart(perf.data.byType || [])
    }
    if (recent && recent.code === 200) recentActivity.value = recent.data || []
  } finally { loading.value = false }
})

const initCategoryChart = (data) => {
  if (!catChart.value || !data.length) return
  const chart = echarts.init(catChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 80, right: 40, top: 10, bottom: 20 },
    xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    yAxis: { type: 'category', data: data.map(d => d.category || '未知') },
    series: [{
      type: 'bar',
      data: data.map(d => Number(d.correct_rate)),
      itemStyle: {
        borderRadius: [0, 6, 6, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#ff8a65' }, { offset: 1, color: '#ff6b6b' }
        ])
      },
      barWidth: '50%',
      label: { show: true, position: 'right', formatter: '{c}%' }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

const initTypeChart = (data) => {
  if (!typeChart.value || !data.length) return
  const chart = echarts.init(typeChart.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['45%', '72%'],
      center: ['50%', '55%'],
      itemStyle: { borderRadius: 6, borderColor: 'var(--bg-card)', borderWidth: 3 },
      label: { formatter: '{b}\n{d}%' },
      data: data.map(t => ({
        name: typeMap[t.type] || t.type,
        value: Number(t.correct_rate)
      })),
      color: ['#ff8a65', '#ff6b6b', '#feca57', '#ff9ff3', '#a29bfe']
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}
</script>
