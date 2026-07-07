<template>
  <div>
    <el-card v-loading="loading">
      <template #header>
        <span>{{ isEdit ? '编辑试卷' : '创建试卷' }}</span>
      </template>

      <el-form :model="paper" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="试卷名称">
              <el-input v-model="paper.title" placeholder="输入试卷名称" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="考试时长">
              <el-input-number v-model="paper.duration" :min="1" :max="180" /> 分钟
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="创建者">
              <el-input :model-value="creatorName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="paper.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <el-divider>选题（已选 {{ selected.length }} 题，总分 {{ total }} 分）</el-divider>

      <el-table :data="questions" border stripe @selection-change="onSelect" max-height="400" ref="tableRef">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="content" label="题目内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="题型" width="80">
          <template #default="{row}">{{ typeMap[row.type] || row.type }}</template>
        </el-table-column>
        <el-table-column label="此题分值" width="100">
          <template #default="{row}">
            <el-input-number :model-value="scores[row.id] ?? 5"
              @update:model-value="val => scores[row.id] = val"
              :min="1" :max="50" size="small" controls-position="right" style="width:90px" />
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:20px;text-align:center">
        <strong>总分：{{ total }} 分</strong>
        <br/>
        <el-button @click="$router.push('/papers')">取消</el-button>
        <el-button type="primary" @click="doSubmit" :loading="submitting">保存试卷</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)

const typeMap = { single_choice: '单选', multi_choice: '多选', true_false: '判断', fill_blank: '填空', essay: '简答' }

const loading = ref(true)
const submitting = ref(false)
const questions = ref([])
const selected = ref([])
const scores = ref({})
const tableRef = ref(null)

const creatorName = sessionStorage.getItem('username') || '当前用户'

const paper = reactive({
  title: '',
  description: '',
  duration: 60
})

const total = computed(() => {
  if (!selected.value.length) return 0
  return selected.value.reduce((s, r) => s + (scores.value[r.id] || 5), 0)
})

const onSelect = (rows) => {
  selected.value = rows || []
  for (const r of (rows || [])) {
    if (!scores.value[r.id]) {
      scores.value[r.id] = r.score || 5
    }
  }
}

const doSubmit = async () => {
  if (!selected.value.length) {
    ElMessage.warning('请至少选择一道题目')
    return
  }
  if (!paper.title.trim()) {
    ElMessage.warning('请输入试卷名称')
    return
  }
  submitting.value = true
  try {
    const data = {
      title: paper.title,
      description: paper.description,
      duration: paper.duration,
      startTime: '2026-01-01 00:00:00',
      endTime: '2099-12-31 23:59:59',
      questions: selected.value.map(r => ({
        questionId: String(r.id),
        score: scores.value[r.id] || 5
      }))
    }
    if (isEdit.value) {
      await request.put('/paper/' + route.params.id, data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/paper/create', data)
      ElMessage.success('创建成功')
    }
    router.push('/papers')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get('/question/list')
    if (res && res.data) {
      questions.value = res.data
    }
  } catch (e) {
    questions.value = []
  }
  loading.value = false
})
</script>
