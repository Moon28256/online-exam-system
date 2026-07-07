<template>
  <div>
    <el-card>
      <template #header>
        <span>试卷管理</span>
        <el-button type="primary" style="float:right" @click="$router.push('/paper/edit')"><el-icon><Plus /></el-icon>组卷</el-button>
      </template>

      <!-- 归属筛选 -->
      <div class="ownership-filter">
        <el-radio-group v-model="filterOwnership" size="default">
          <el-radio-button value="all">全部 ({{ tableData.length }})</el-radio-button>
          <el-radio-button value="mine">我的 ({{ mineCount }})</el-radio-button>
          <el-radio-button value="others">他人 ({{ tableData.length - mineCount }})</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="filteredTableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="title" label="试卷名称" min-width="180" />
        <el-table-column prop="totalScore" label="满分" width="70" />
        <el-table-column prop="duration" label="时长(分)" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status === 'published' ? 'success' : 'info'">
              {{ row.status === 'published' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="归属" width="80">
          <template #default="{row}">
            <el-tag v-if="isMine(row)" type="success" size="small" effect="plain">我的</el-tag>
            <el-tag v-else type="info" size="small" effect="plain">他人</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320">
          <template #default="{row}">
            <template v-if="canManage(row)">
              <el-button size="small" @click="$router.push(`/paper/edit/${row.id}`)">编辑</el-button>
              <el-button v-if="row.status==='draft'" size="small" type="success" @click="publish(row.id)">发布</el-button>
              <el-button v-if="row.status==='published'" size="small" type="warning" @click="unpublish(row.id)">取消发布</el-button>
              <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
            </template>
            <el-button v-else size="small" type="primary" @click="viewPaper(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 试卷详情弹窗（只读） -->
    <el-dialog :title="'试卷详情：' + (viewPaperData.title || '')" v-model="viewVisible" width="720px" top="6vh" append-to-body destroy-on-close>
      <div v-loading="viewLoading">
        <el-descriptions :column="3" border style="margin-bottom:16px">
          <el-descriptions-item label="满分">{{ viewPaperData.totalScore }} 分</el-descriptions-item>
          <el-descriptions-item label="时长">{{ viewPaperData.duration }} 分钟</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="viewPaperData.status === 'published' ? 'success' : 'info'" size="small">
              {{ viewPaperData.status === 'published' ? '已发布' : '草稿' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="viewPaperData.description" style="margin-bottom:12px;color:var(--text-secondary)">
          {{ viewPaperData.description }}
        </div>
        <el-divider>题目列表（共 {{ viewQuestions.length }} 题）</el-divider>
        <div v-for="(q, idx) in viewQuestions" :key="q.id" class="pq-item">
          <div class="pq-header">
            <span class="pq-num">{{ idx + 1 }}.</span>
            <el-tag size="small" effect="plain">{{ typeMap[q.type] }}</el-tag>
            <span class="pq-score">{{ q.score }} 分</span>
          </div>
          <div class="pq-content">{{ q.content }}</div>
          <div v-if="q.optionA || q.optionB" class="pq-options">
            <div v-for="opt in ['A','B','C','D']" :key="opt">
              <span v-if="q['option'+opt]"><strong>{{ opt }}.</strong> {{ q['option'+opt] }}</span>
            </div>
          </div>
        </div>
        <div v-if="!viewQuestions.length && !viewLoading" style="text-align:center;padding:30px;color:var(--text-muted)">暂无题目</div>
      </div>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const loading = ref(false)
const tableData = ref([])

// 试卷详情（只读）
const viewVisible = ref(false)
const viewLoading = ref(false)
const viewPaperData = ref({})
const viewQuestions = ref([])
const typeMap = { single_choice: '单选', multi_choice: '多选', true_false: '判断', fill_blank: '填空', essay: '简答' }

// 当前用户（归属判断）
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

const viewPaper = async (row) => {
  viewPaperData.value = row
  viewVisible.value = true
  viewLoading.value = true
  viewQuestions.value = []
  try {
    const res = await request.get(`/paper/${row.id}/questions`)
    viewQuestions.value = (res && res.data) ? res.data : []
  } finally {
    viewLoading.value = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/paper/list')
    tableData.value = res.data || []
  } finally { loading.value = false }
}

const publish = async (id) => {
  await request.put(`/paper/publish/${id}`)
  ElMessage.success('已发布')
  loadData()
}

const unpublish = async (id) => {
  await request.put(`/paper/unpublish/${id}`)
  ElMessage.success('已退回草稿')
  loadData()
}

const del = async (id) => {
  await ElMessageBox.confirm('确定删除该试卷吗？删除后不可恢复。', '警告', { type: 'warning' })
  await request.delete(`/paper/${id}`)
  ElMessage.success('已删除')
  loadData()
}

loadData()
</script>

<style scoped>
.ownership-filter { margin-bottom: 14px; }
.ownership-filter :deep(.el-radio-button__inner) { font-weight: 600; }

.pq-item {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 10px;
  background: var(--bg-card);
}
.pq-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.pq-num { font-weight: bold; color: var(--accent); }
.pq-score { color: var(--text-muted); font-size: 13px; margin-left: auto; }
.pq-content { font-size: 14px; line-height: 1.7; color: var(--text-primary); margin-bottom: 8px; }
.pq-options { display: flex; flex-direction: column; gap: 4px; color: var(--text-secondary); font-size: 13px; padding-left: 8px; }
</style>
