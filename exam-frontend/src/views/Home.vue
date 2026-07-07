<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <div class="stat-card glass-card" @click="$router.push(card.link)">
          <div class="stat-icon">
            <el-icon :size="32"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-num">{{ card.count }}</div>
            <div class="stat-label">{{ card.title }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><el-icon><PieChart /></el-icon> 成绩分布概览</template>
          <div ref="pieChart" style="height:280px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><el-icon><TrendCharts /></el-icon> 近期考试趋势</template>
          <div ref="lineChart" style="height:280px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../api/request'

const username = ref(sessionStorage.getItem('username') || '')
const role = ref(sessionStorage.getItem('role') || '')
const cards = ref([])
const pieChart = ref(null)
const lineChart = ref(null)

onMounted(async () => {
  try {
    const res = await request.get('/analysis/overview')
    const d = (res && res.data) ? res.data : {}

    if (role.value === 'admin') {
      cards.value = [
        { icon: 'UserFilled', title: '用户管理', count: d.users||0, link: '/users' },
        { icon: 'Reading', title: '题库管理', count: d.questions||0, link: '/questions' },
        { icon: 'Document', title: '试卷管理', count: d.papers||0, link: '/papers' },
        { icon: 'TrendCharts', title: '考试记录', count: d.exams||0, link: '/score-ranking' },
      ]
    } else if (role.value === 'teacher') {
      cards.value = [
        { icon: 'Reading', title: '题库总数', count: d.questions||0, link: '/questions' },
        { icon: 'Document', title: '试卷总数', count: d.papers||0, link: '/papers' },
        { icon: 'EditPen', title: '考试记录', count: d.exams||0, link: '/score-ranking' },
        { icon: 'DataLine', title: '成绩记录', count: d.scores||0, link: '/analysis' },
      ]
    } else {
      cards.value = [
        { icon: 'EditPen', title: '可考试卷', count: d.papers||0, link: '/exam-list' },
        { icon: 'Finished', title: '已考试', count: d.exams||0, link: '/my-scores' },
        { icon: 'Collection', title: '错题数量', count: d.questions||0, link: '/wrong-questions' },
        { icon: 'TrendCharts', title: '成绩趋势', count: d.scores||0, link: '/my-trend' },
      ]
    }
  } catch (e) { /* ignore */ }

  await nextTick()
  initCharts()
})

const initCharts = async () => {
  if (!pieChart.value) return

  // 成绩分布饼图
  try {
    const perfRes = await request.get('/analysis/performance')
    const byType = (perfRes && perfRes.data && perfRes.data.byType) || []

    const pie = echarts.init(pieChart.value)
    pie.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['50%', '75%'],
        center: ['50%', '55%'],
        itemStyle: { borderRadius: 6, borderColor: 'var(--bg-card)', borderWidth: 3 },
        label: { formatter: '{b}\n{d}%' },
        data: byType.map(t => ({
          name: { single_choice:'单选', multi_choice:'多选', true_false:'判断', fill_blank:'填空', essay:'简答' }[t.type] || t.type,
          value: Number(t.correct_rate)
        })),
        color: ['#ff8a65', '#ff6b6b', '#feca57', '#ff9ff3', '#a29bfe']
      }]
    })
    window.addEventListener('resize', () => pie.resize())
  } catch (e) { /* ignore */ }

  // 近期活动折线图
  try {
    const actRes = await request.get('/analysis/recent-activity')
    const actData = (actRes && actRes.data) || []

    const line = echarts.init(lineChart.value)
    line.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: actData.map(a => a.exam_date).reverse() },
      yAxis: { type: 'value', name: '场次' },
      series: [{
        type: 'bar',
        data: actData.map(a => a.exam_count).reverse(),
        itemStyle: { borderRadius: [6,6,0,0], color: '#ff8a65' },
        barWidth: '50%'
      }]
    })
    window.addEventListener('resize', () => line.resize())
  } catch (e) { /* ignore */ }
}
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  gap: 16px;
  cursor: pointer;
}
.stat-icon { color: var(--accent); }
.stat-num { font-size: 28px; font-weight: bold; color: var(--text-primary); }
.stat-label { font-size: 14px; color: var(--text-muted); margin-top: 4px; }
</style>
