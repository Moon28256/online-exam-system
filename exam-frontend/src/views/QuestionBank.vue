<template>
  <div>
    <el-card>
      <template #header>
        <span>题库管理</span>
        <el-button type="primary" style="float:right" @click="openAdd"><el-icon><Plus /></el-icon>添加题目</el-button>
      </template>

      <!-- 归属筛选 -->
      <div class="ownership-filter">
        <el-radio-group v-model="filterOwnership" size="default">
          <el-radio-button value="all">全部 ({{ tableData.length }})</el-radio-button>
          <el-radio-button value="mine">我的 ({{ mineCount }})</el-radio-button>
          <el-radio-button value="others">他人 ({{ tableData.length - mineCount }})</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 筛选 -->
      <el-row :gutter="10" style="margin-bottom:15px">
        <el-col :span="4">
          <el-select v-model="filters.type" placeholder="题型" clearable @change="loadData">
            <el-option label="单选" value="single_choice" />
            <el-option label="多选" value="multi_choice" />
            <el-option label="判断" value="true_false" />
            <el-option label="填空" value="fill_blank" />
            <el-option label="简答" value="essay" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filters.difficulty" placeholder="难度" clearable @change="loadData">
            <el-option label="简单" value="easy" />
            <el-option label="中等" value="medium" />
            <el-option label="困难" value="hard" />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-input v-model="filters.category" placeholder="类别" clearable @clear="loadData" />
        </el-col>
        <el-col :span="5">
          <el-input v-model="filters.keyword" placeholder="搜索题目内容" clearable @clear="loadData" />
        </el-col>
        <el-col :span="3">
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-col>
      </el-row>

      <!-- 表格 -->
      <el-table :data="filteredTableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="题目内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="题型" width="90">
          <template #default="{row}">{{ typeMap[row.type] }}</template>
        </el-table-column>
        <el-table-column prop="category" label="类别" width="90" />
        <el-table-column label="难度" width="70">
          <template #default="{row}">
            <el-tag :type="diffColor[row.difficulty]" size="small">{{ diffMap[row.difficulty] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="60" />
        <el-table-column label="归属" width="80">
          <template #default="{row}">
            <el-tag v-if="isMine(row)" type="success" size="small" effect="plain">我的</el-tag>
            <el-tag v-else type="info" size="small" effect="plain">他人</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{row}">
            <template v-if="canManage(row)">
              <el-button size="small" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
            </template>
            <el-button v-else size="small" type="primary" @click="openView(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑/查看弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px"
      append-to-body destroy-on-close @closed="resetForm">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="题型" prop="type">
          <el-select v-model="form.type" style="width:100%" @change="onTypeChange" :disabled="readonly">
            <el-option label="单选题" value="single_choice" />
            <el-option label="多选题" value="multi_choice" />
            <el-option label="判断题" value="true_false" />
            <el-option label="填空题" value="fill_blank" />
            <el-option label="简答题" value="essay" />
          </el-select>
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-input v-model="form.category" placeholder="如：Java基础" :disabled="readonly" />
        </el-form-item>
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="2" :disabled="readonly" />
        </el-form-item>
        <!-- 选择题才显示选项 -->
        <template v-if="showOptions">
          <el-form-item label="选项A" prop="optionA"><el-input v-model="form.optionA" :disabled="readonly" /></el-form-item>
          <el-form-item label="选项B" prop="optionB"><el-input v-model="form.optionB" :disabled="readonly" /></el-form-item>
          <el-form-item label="选项C"><el-input v-model="form.optionC" :disabled="readonly" /></el-form-item>
          <el-form-item label="选项D"><el-input v-model="form.optionD" :disabled="readonly" /></el-form-item>
        </template>
        <el-form-item label="正确答案" prop="answer">
          <el-input v-model="form.answer" :placeholder="answerPlaceholder" :disabled="readonly" />
        </el-form-item>
        <el-form-item label="难度">
          <el-radio-group v-model="form.difficulty" :disabled="readonly">
            <el-radio value="easy">简单</el-radio>
            <el-radio value="medium">中等</el-radio>
            <el-radio value="hard">困难</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分值">
          <el-input-number v-model="form.score" :min="1" :max="100" :disabled="readonly" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ readonly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!readonly" type="primary" @click="submit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const readonly = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const tableData = ref([])

const dialogTitle = computed(() => readonly.value ? '题目详情' : (isEdit.value ? '编辑题目' : '添加题目'))

// 当前用户（用于归属判断）
const currentUserId = sessionStorage.getItem('userId') || ''
const isAdmin = sessionStorage.getItem('role') === 'admin'
const isMine = (row) => String(row.creatorId) === String(currentUserId)
const canManage = (row) => isAdmin || isMine(row)

// 归属筛选
const filterOwnership = ref('all')
const mineCount = computed(() => tableData.value.filter(isMine).length)
const filteredTableData = computed(() => {
  if (filterOwnership.value === 'mine') return tableData.value.filter(isMine)
  if (filterOwnership.value === 'others') return tableData.value.filter(r => !isMine(r))
  return tableData.value
})

const typeMap = { single_choice: '单选', multi_choice: '多选', true_false: '判断', fill_blank: '填空', essay: '简答' }
const diffMap = { easy: '简单', medium: '中等', hard: '困难' }
const diffColor = { easy: 'success', medium: 'warning', hard: 'danger' }

const filters = reactive({ type: '', difficulty: '', category: '', keyword: '' })

const form = reactive({
  id: null, type: 'single_choice', category: '', content: '',
  optionA: '', optionB: '', optionC: '', optionD: '', answer: '',
  difficulty: 'medium', score: 5
})

const formRules = {
  type: [{ required: true }],
  content: [{ required: true, message: '请输入题目内容' }],
  answer: [{ required: true, message: '请输入正确答案' }],
  category: [{ required: true, message: '请输入类别' }]
}

const showOptions = computed(() => ['single_choice', 'multi_choice', 'true_false'].includes(form.type))
const answerPlaceholder = computed(() => {
  if (form.type === 'single_choice') return '如：A'
  if (form.type === 'multi_choice') return '如：A,C,D'
  if (form.type === 'true_false') return '如：A（正确）或 B（错误）'
  if (form.type === 'fill_blank') return '如：Object'
  return '简答题答案'
})

const onTypeChange = () => {
  if (form.type === 'true_false') {
    form.optionA = '正确'; form.optionB = '错误'
    form.optionC = ''; form.optionD = ''
  } else if (!['single_choice', 'multi_choice'].includes(form.type)) {
    form.optionA = ''; form.optionB = ''; form.optionC = ''; form.optionD = ''
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.type) params.type = filters.type
    if (filters.difficulty) params.difficulty = filters.difficulty
    if (filters.category) params.category = filters.category
    if (filters.keyword) params.keyword = filters.keyword
    const res = await request.get('/question/list', { params })
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  isEdit.value = false
  readonly.value = false
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  readonly.value = false
  Object.assign(form, row)
  dialogVisible.value = true
}

const openView = (row) => {
  isEdit.value = true
  readonly.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null, type: 'single_choice', category: '', content: '',
    optionA: '', optionB: '', optionC: '', optionD: '', answer: '',
    difficulty: 'medium', score: 5
  })
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/question/${form.id}`, form)
      ElMessage.success('更新成功')
    } else {
      await request.post('/question/add', form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const del = async (id) => {
  await ElMessageBox.confirm('确定删除该题目吗？', '提示', { type: 'warning' })
  await request.delete(`/question/${id}`)
  ElMessage.success('删除成功')
  loadData()
}

loadData()
</script>

<style scoped>
.ownership-filter { margin-bottom: 14px; }
.ownership-filter :deep(.el-radio-button__inner) { font-weight: 600; }
</style>
