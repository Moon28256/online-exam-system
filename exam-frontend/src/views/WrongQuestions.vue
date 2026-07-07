<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span><el-icon><Collection /></el-icon> 错题本</span>
          <el-tag type="danger" effect="dark" round>共 {{ list.length }} 题</el-tag>
        </div>
      </template>

      <!-- 筛选条 -->
      <div class="filter-bar" v-if="list.length">
        <el-select v-model="filterType" placeholder="题型" clearable size="small" style="width:120px">
          <el-option label="单选" value="single_choice" />
          <el-option label="多选" value="multi_choice" />
          <el-option label="判断" value="true_false" />
          <el-option label="填空" value="fill_blank" />
        </el-select>
        <el-select v-model="filterCategory" placeholder="分类" clearable size="small" style="width:160px">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
      </div>

      <!-- 空状态 -->
      <div v-if="list.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">🎉</div>
        <h2>暂无错题</h2>
        <p>继续保持！</p>
      </div>

      <!-- 错题列表（精简卡片） -->
      <div v-for="(item, idx) in filteredList" :key="item.id" class="wrong-item" @click="openDetail(item)">
        <div class="w-left">
          <div class="w-rank">{{ idx + 1 }}</div>
        </div>
        <div class="w-body">
          <div class="w-header">
            <el-tag size="small" :type="typeColor(item.type)">{{ typeMap[item.type] }}</el-tag>
            <el-tag size="small" type="warning" effect="plain">{{ diffMap[item.difficulty] }}</el-tag>
            <el-tag size="small" effect="plain" v-if="item.category">{{ item.category }}</el-tag>
            <span class="wrong-count">错误 {{ item.wrong_count }} 次</span>
          </div>
          <div class="w-content">{{ item.content }}</div>
        </div>
        <div class="w-right">
          <el-button size="small" type="primary" @click.stop="openDetail(item)">
            <el-icon><View /></el-icon> 查看详情
          </el-button>
          <el-button size="small" type="danger" plain @click.stop="remove(item.id)">
            <el-icon><Delete /></el-icon> 移除
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="错题详情" width="640px" top="6vh" destroy-on-close append-to-body>
      <div v-if="current" class="detail-box">
        <div class="d-header">
          <el-tag size="small" :type="typeColor(current.type)">{{ typeMap[current.type] }}</el-tag>
          <el-tag size="small" type="warning" effect="plain">{{ diffMap[current.difficulty] }}</el-tag>
          <el-tag size="small" effect="plain" v-if="current.category">{{ current.category }}</el-tag>
          <span class="wrong-count">累计错误 {{ current.wrong_count }} 次</span>
        </div>

        <div class="d-content">{{ current.content }}</div>

        <!-- 选择题/多选：显示选项 -->
        <div v-if="current.type === 'single_choice' || current.type === 'multi_choice'" class="d-options">
          <template v-for="opt in ['A','B','C','D']" :key="opt">
            <div v-if="current['option'+opt] && isCorrectOpt(current, opt)" class="d-opt is-correct-opt">
              <span class="d-opt-label">{{ opt }}</span>
              <span class="d-opt-text">{{ current['option'+opt] }}</span>
              <el-icon class="d-opt-icon"><CircleCheck /></el-icon>
            </div>
            <div v-else-if="current['option'+opt]" class="d-opt">
              <span class="d-opt-label">{{ opt }}</span>
              <span class="d-opt-text">{{ current['option'+opt] }}</span>
            </div>
          </template>
        </div>

        <!-- 判断题 -->
        <div v-else-if="current.type === 'true_false'" class="d-options tf-detail">
          <div class="d-opt" :class="isCorrectOpt(current,'A') ? 'is-correct-opt' : ''">
            <span class="d-opt-label">✓</span> 正确
            <el-icon v-if="isCorrectOpt(current,'A')" class="d-opt-icon"><CircleCheck /></el-icon>
          </div>
          <div class="d-opt" :class="isCorrectOpt(current,'B') ? 'is-correct-opt' : ''">
            <span class="d-opt-label">✗</span> 错误
            <el-icon v-if="isCorrectOpt(current,'B')" class="d-opt-icon"><CircleCheck /></el-icon>
          </div>
        </div>

        <!-- 答案区 -->
        <div class="d-answer">
          <div class="answer-row">
            <span class="answer-label">正确答案</span>
            <el-tag type="success" size="large" effect="dark">{{ current.answer }}</el-tag>
          </div>
          <div class="answer-tip" v-if="current.type === 'multi_choice'">
            💡 多选题：需选全部正确选项，少选/多选均不得分
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="danger" @click="removeFromDetail">
          <el-icon><Delete /></el-icon> 从错题本移除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const loading = ref(false)
const list = ref([])
const filterType = ref('')
const filterCategory = ref('')
const detailVisible = ref(false)
const current = ref(null)

const typeMap = { single_choice: '单选', multi_choice: '多选', true_false: '判断', fill_blank: '填空', essay: '简答' }
const diffMap = { easy: '简单', medium: '中等', hard: '困难' }

const typeColor = (t) => ({ single_choice: '', multi_choice: 'success', true_false: 'warning', fill_blank: 'info', essay: 'danger' }[t] || '')

// 所有分类（去重）
const categories = computed(() => [...new Set(list.value.map(i => i.category).filter(Boolean))])

// 过滤后的列表
const filteredList = computed(() => {
  return list.value.filter(i => {
    if (filterType.value && i.type !== filterType.value) return false
    if (filterCategory.value && i.category !== filterCategory.value) return false
    return true
  })
})

// 正确选项集合
const correctOpts = (a) => {
  if (!a?.answer) return []
  return a.answer.split(',').map(s => s.trim()).filter(Boolean)
}
const isCorrectOpt = (a, opt) => correctOpts(a).includes(opt)

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/wrong/my')
    list.value = res.data || []
  } finally { loading.value = false }
}

const openDetail = (item) => { current.value = item; detailVisible.value = true }

const remove = async (id) => {
  await request.delete(`/wrong/${id}`)
  ElMessage.success('已移除')
  loadData()
}

const removeFromDetail = async () => {
  if (!current.value) return
  await request.delete(`/wrong/${current.value.id}`)
  ElMessage.success('已移除')
  detailVisible.value = false
  loadData()
}

loadData()
</script>

<style scoped>
.filter-bar { display: flex; gap: 10px; margin-bottom: 16px; }

.empty-state { text-align: center; padding: 60px 20px; color: var(--text-muted); }
.empty-icon { font-size: 64px; margin-bottom: 12px; }

.wrong-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  border: 1px solid var(--border-color);
  border-left: 4px solid #f56c6c;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 12px;
  background: var(--bg-card);
  transition: all 0.25s;
  cursor: pointer;
}
.wrong-item:hover { box-shadow: var(--shadow); transform: translateX(2px); border-left-color: #f56c6c; }

.w-left { flex-shrink: 0; }
.w-rank {
  width: 28px; height: 28px; border-radius: 50%;
  background: rgba(245,108,108,0.12); color: #f56c6c;
  display: flex; align-items: center; justify-content: center;
  font-weight: bold; font-size: 14px;
}
.w-body { flex: 1; min-width: 0; }
.w-header { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; margin-bottom: 8px; }
.wrong-count { color: var(--text-muted); font-size: 12px; margin-left: 4px; }
.w-content {
  color: var(--text-primary); font-size: 14px; line-height: 1.6;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden;
}
.w-right { display: flex; flex-direction: column; gap: 6px; flex-shrink: 0; }

/* 详情弹窗 */
.detail-box { padding: 4px; }
.d-header { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; margin-bottom: 14px; }
.d-content {
  font-size: 16px; line-height: 1.8; color: var(--text-primary);
  padding: 14px 16px; background: var(--accent-light); border-radius: 8px;
  margin-bottom: 16px;
}
.d-options { display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px; }
.d-opt {
  display: flex; align-items: center;
  padding: 10px 14px; border: 1px solid var(--border-color); border-radius: 8px;
  transition: all 0.15s;
}
.d-opt-label {
  display: inline-flex; align-items: center; justify-content: center;
  width: 26px; height: 26px; border-radius: 50%;
  background: var(--accent-light); color: var(--accent);
  font-weight: bold; margin-right: 12px; flex-shrink: 0;
}
.d-opt-text { font-size: 14px; line-height: 1.5; color: var(--text-primary); }
.d-opt-icon { margin-left: auto; font-size: 18px; color: #67c23a; }
.d-opt.is-correct-opt {
  background: rgba(103,194,58,0.08); border-color: #67c23a;
}
.d-opt.is-correct-opt .d-opt-label { background: rgba(103,194,58,0.15); color: #67c23a; }

.tf-detail { flex-direction: row !important; gap: 12px; }
.tf-detail .d-opt { flex: 1; justify-content: center; }

.d-answer {
  padding: 14px 16px; background: var(--bg-card);
  border: 1px solid var(--border-color); border-radius: 8px;
  border-left: 4px solid #67c23a;
}
.answer-row { display: flex; align-items: center; gap: 12px; }
.answer-label { color: var(--text-secondary); font-size: 14px; }
.answer-tip { margin-top: 8px; color: var(--text-muted); font-size: 13px; }
</style>
