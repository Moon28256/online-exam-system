<template>
  <div>
    <el-card>
      <template #header><span>在线考试</span></template>
      <el-table :data="tableData" border stripe v-loading="loading" :default-sort="{prop:'id',order:'descending'}">
        <el-table-column prop="title" label="试卷名称" min-width="200" />
        <el-table-column prop="totalScore" label="满分" width="80" />
        <el-table-column prop="duration" label="时长(分)" width="80" />
        <el-table-column label="状态" width="120">
          <template #default>
            <el-tag type="success">可考试</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{row}">
            <el-button type="primary" size="small" @click="startExam(row.id)"><el-icon><EditPen /></el-icon>开始考试</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="tableData.length === 0 && !loading" style="text-align:center;padding:50px;color:#999">暂无可用试卷</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/paper/list', { params: { status: 'published' } })
    tableData.value = res.data || []
  } finally { loading.value = false }
}

const startExam = async (paperId) => {
  try {
    const res = await request.post('/exam/start', { paperId })
    if (res.code === 200 && res.data) {
      sessionStorage.setItem('examData', JSON.stringify(res.data))
      router.push(`/exam/${res.data.examRecordId}`)
    }
  } catch (e) {
    ElMessage.error('开始考试失败')
  }
}

loadData()
</script>
