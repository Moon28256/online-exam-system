<template>
  <div>
    <el-card shadow="never">
      <template #header><el-icon><TrendCharts /></el-icon> 成绩趋势</template>
      <div ref="chart" style="height:380px"></div>
      <div v-if="!hasData" style="text-align:center;padding:60px;color:var(--text-muted)">
        <el-icon :size="48"><DataLine /></el-icon>
        <p>暂无考试记录</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../api/request'

const chart = ref(null)
const hasData = ref(false)

onMounted(async () => {
  try {
    const res = await request.get('/analysis/my-trend')
    const data = (res && res.data) || []
    if (!data.length) return
    hasData.value = true
    await nextTick()
    if (!chart.value) return

    const c = echarts.init(chart.value)
    c.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['平均分', '最高分'], bottom: 0 },
      grid: { left: 40, right: 20, top: 20, bottom: 40 },
      xAxis: { type: 'category', data: data.map(d => d.paper_title) },
      yAxis: { type: 'value', name: '分数' },
      series: [
        {
          name: '平均分', type: 'line',
          data: data.map(d => Number(d.avg_score).toFixed(1)),
          smooth: true, symbol: 'circle', symbolSize: 10,
          lineStyle: { width: 3, color: '#ff8a65' },
          itemStyle: { color: '#ff8a65' },
          areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[
            {offset:0,color:'rgba(255,138,101,0.3)'},{offset:1,color:'rgba(255,138,101,0.02)'}
          ])}
        },
        {
          name: '最高分', type: 'line',
          data: data.map(d => d.max_score),
          smooth: true, symbol: 'diamond', symbolSize: 10,
          lineStyle: { width: 3, type: 'dashed', color: '#67c23a' },
          itemStyle: { color: '#67c23a' }
        }
      ]
    })
    window.addEventListener('resize', () => c.resize())
  } catch (e) { /* ignore */ }
})
</script>
