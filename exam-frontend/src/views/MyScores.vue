<template>
  <div v-loading="loading">
    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="8" v-for="s in statsArr" :key="s.label">
        <el-card shadow="never" class="glass-card" style="text-align:center">
          <div class="ring-wrap">
            <svg viewBox="0 0 120 120" width="120" height="120">
              <circle cx="60" cy="60" r="52" fill="none" stroke="#e6e6e6" stroke-width="8" />
              <circle cx="60" cy="60" r="52" fill="none" :stroke="s.color" stroke-width="8"
                stroke-linecap="round" :stroke-dasharray="circumference"
                :stroke-dashoffset="circumference * (1 - s.percent / 100)"
                transform="rotate(-90 60 60)" style="transition: stroke-dashoffset 1s ease" />
            </svg>
            <div class="ring-text">
              <div class="ring-num">{{ s.value }}</div>
              <div class="ring-label">{{ s.suffix }}</div>
            </div>
          </div>
          <div style="margin-top:8px;font-weight:bold;color:var(--text-primary)">{{ s.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header><el-icon><List /></el-icon> 成绩明细</template>
      <el-table :data="details" border stripe>
        <el-table-column prop="paper_title" label="试卷" />
        <el-table-column prop="avg_score" label="平均分" sortable
          :sort-method="(a, b) => Number(a.avg_score) - Number(b.avg_score)">
          <template #default="{row}">{{ Number(row.avg_score).toFixed(1) }}</template>
        </el-table-column>
        <el-table-column prop="max_score" label="最高分" sortable />
        <el-table-column prop="min_score" label="最低分" sortable />
        <el-table-column prop="student_count" label="考试次数" sortable />
      </el-table>
      <div v-if="!details.length" style="text-align:center;padding:40px;color:var(--text-muted)">暂无记录</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../api/request'

const loading = ref(false)
const details = ref([])
const circumference = 2 * Math.PI * 52  // ~327

const statsArr = reactive([
  { label: '参加考试', value: 0, suffix: '次', color: '#ff8a65', percent: 0 },
  { label: '总分累计', value: 0, suffix: '分', color: '#67c23a', percent: 0 },
  { label: '总平均分', value: 0, suffix: '分', color: '#e6a23c', percent: 0 },
])

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get('/score/my')
    if (res && res.code === 200 && res.data) {
      const d = res.data
      statsArr[0].value = d.totalExams || 0
      statsArr[0].percent = Math.min(100, (d.totalExams || 0) * 10)
      statsArr[1].value = d.totalScore || 0
      statsArr[1].percent = Math.min(100, (d.totalScore || 0) / 2)
      statsArr[2].value = d.overallAvg || 0
      statsArr[2].percent = d.overallAvg || 0
      details.value = d.details || []
    }
  } finally { loading.value = false }
})
</script>

<style scoped>
.ring-wrap { position: relative; display: inline-block; }
.ring-text { position: absolute; top: 50%; left: 50%; transform: translate(-50%,-50%); text-align: center; }
.ring-num { font-size: 24px; font-weight: bold; color: var(--text-primary); }
.ring-label { font-size: 12px; color: var(--text-muted); }
</style>
