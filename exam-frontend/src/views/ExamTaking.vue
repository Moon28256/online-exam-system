<template>
  <div>
    <el-card v-if="loading" shadow="never"><div style="text-align:center;padding:60px">加载考试数据...</div></el-card>
    <el-card v-else shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span><el-icon><EditPen /></el-icon> {{ paperTitle }}</span>
          <div style="display:flex;align-items:center;gap:12px">
            <div :class="['countdown', { 'breathe-alert': timeLeft <= 300 }]">
              <el-icon><Timer /></el-icon>
              <span style="font-size:20px;font-weight:bold;margin-left:6px">{{ formatTime(timeLeft) }}</span>
            </div>
            <el-button @click="drawerVisible = true"><el-icon><Grid /></el-icon> 答题卡</el-button>
            <el-button type="danger" @click="openPreview" :icon="Finished">交卷</el-button>
          </div>
        </div>
      </template>

      <!-- 作答进度条 -->
      <div class="progress-bar">
        <div class="progress-info">
          <span>作答进度</span>
          <span class="progress-count">{{ answeredCount }} / {{ questions.length }} 已答</span>
        </div>
        <el-progress :percentage="progressPercent" :color="progressColor" :stroke-width="10" />
      </div>

      <div v-for="(q, idx) in questions" :key="q.id" :id="'q' + idx" class="question-block">
        <div class="q-header">
          <span class="q-num">{{ idx + 1 }}.</span>
          <el-tag size="small" effect="dark">{{ typeMap[q.type] }}</el-tag>
          <span style="margin-left:8px;color:var(--text-muted)">{{ q.score }}分</span>
          <el-tag v-if="isAnswered(q)" type="success" size="small" effect="plain" style="margin-left:auto">已答</el-tag>
          <el-tag v-else type="warning" size="small" effect="plain" style="margin-left:auto">未答</el-tag>
        </div>
        <div class="q-content">{{ q.content }}</div>

        <el-radio-group v-if="q.type === 'single_choice'" v-model="answers[q.id]" class="q-options">
          <template v-for="opt in ['A','B','C','D']" :key="opt">
            <el-radio :value="opt" v-if="q['option'+opt]" class="q-option-item">
              <span class="q-option-label">{{ opt }}</span>
              <span class="q-option-text">{{ q['option'+opt] }}</span>
            </el-radio>
          </template>
        </el-radio-group>

        <el-checkbox-group v-else-if="q.type === 'multi_choice'" v-model="multiAnswers[q.id]" class="q-options">
          <template v-for="opt in ['A','B','C','D']" :key="opt">
            <el-checkbox :label="opt" v-if="q['option'+opt]" class="q-option-item">
              <span class="q-option-label">{{ opt }}</span>
              <span class="q-option-text">{{ q['option'+opt] }}</span>
            </el-checkbox>
          </template>
        </el-checkbox-group>

        <el-radio-group v-else-if="q.type === 'true_false'" v-model="answers[q.id]" class="q-options tf-options">
          <el-radio value="A" class="q-option-item tf-right"><span class="q-option-label">✓</span> 正确</el-radio>
          <el-radio value="B" class="q-option-item tf-wrong"><span class="q-option-label">✗</span> 错误</el-radio>
        </el-radio-group>

        <el-input v-else v-model="answers[q.id]" :type="q.type==='essay'?'textarea':'text'"
          :rows="q.type==='essay'?4:1" :placeholder="q.type==='fill_blank'?'请输入答案':'请输入你的回答'" />
      </div>

      <div style="text-align:center;margin-top:20px">
        <el-button type="danger" size="large" @click="openPreview" :icon="Finished">确认交卷</el-button>
      </div>
    </el-card>

    <!-- 答题卡抽屉 -->
    <el-drawer v-model="drawerVisible" title="答题卡" direction="rtl" size="320px">
      <div class="card-summary">
        <div class="card-stat">
          <div class="card-stat-num answered">{{ answeredCount }}</div>
          <div class="card-stat-label">已答</div>
        </div>
        <div class="card-stat">
          <div class="card-stat-num unanswered">{{ questions.length - answeredCount }}</div>
          <div class="card-stat-label">未答</div>
        </div>
        <div class="card-stat">
          <div class="card-stat-num total">{{ questions.length }}</div>
          <div class="card-stat-label">总题数</div>
        </div>
      </div>
      <div class="card-legend">
        <span><i class="dot answered"></i>已答</span>
        <span><i class="dot unanswered"></i>未答</span>
      </div>
      <div class="card-grid">
        <div v-for="(q, idx) in questions" :key="q.id"
          :class="['card-cell', isAnswered(q) ? 'cell-answered' : 'cell-unanswered']"
          @click="scrollToQuestion(idx)">
          {{ idx + 1 }}
        </div>
      </div>
      <div style="margin-top:20px;text-align:center">
        <el-button type="danger" @click="drawerVisible = false; openPreview()">交卷</el-button>
      </div>
    </el-drawer>

    <!-- 交卷预览确认 -->
    <el-dialog v-model="previewVisible" title="交卷确认" width="480px" append-to-body destroy-on-close>
      <div class="preview-box">
        <div class="preview-emoji">{{ hasUnanswered ? '⚠️' : '✅' }}</div>
        <div class="preview-title">{{ hasUnanswered ? '还有未答的题目' : '全部题目已完成' }}</div>
        <div class="preview-summary">
          <span>已答 <b class="ok">{{ answeredCount }}</b> 题</span>
          <span v-if="hasUnanswered">未答 <b class="warn">{{ questions.length - answeredCount }}</b> 题</span>
          <span>共 {{ questions.length }} 题</span>
        </div>
        <div v-if="hasUnanswered" class="preview-unanswered">
          <div class="preview-subtitle">未答题号：</div>
          <div class="preview-nums">
            <span v-for="idx in unansweredIndices" :key="idx" class="preview-num" @click="jumpFromPreview(idx)">{{ idx + 1 }}</span>
          </div>
        </div>
        <el-alert v-if="hasUnanswered" type="warning" :closable="false" show-icon style="margin-top:12px">
          <template #title>未答的题目将被记为 0 分，确定要交卷吗？</template>
        </el-alert>
        <el-alert v-else type="success" :closable="false" show-icon style="margin-top:12px">
          <template #title>所有题目已作答，可以放心交卷</template>
        </el-alert>
      </div>
      <template #footer>
        <el-button v-if="hasUnanswered" type="primary" @click="previewVisible = false">继续答题</el-button>
        <el-button v-else @click="previewVisible = false">再检查一下</el-button>
        <el-button type="danger" @click="confirmSubmit" :loading="submitting">确认交卷</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Finished, Grid } from '@element-plus/icons-vue'
import request from '../api/request'

const route = useRoute()
const router = useRouter()
const examRecordId = route.params.examRecordId

const typeMap = { single_choice:'单选', multi_choice:'多选', true_false:'判断', fill_blank:'填空', essay:'简答' }
const loading = ref(true)
const paperTitle = ref('')
const questions = ref([])
const answers = reactive({})
const multiAnswers = reactive({})
const timeLeft = ref(0)
const drawerVisible = ref(false)
const previewVisible = ref(false)
const submitting = ref(false)
let timer = null
let endTime = null
let initialized = false   // 防止初始化时触发保存

// 判断某题是否已作答
const isAnswered = (q) => {
  if (q.type === 'multi_choice') {
    return (multiAnswers[q.id] || []).length > 0
  }
  return !!(answers[q.id] && String(answers[q.id]).trim())
}
const answeredCount = computed(() => questions.value.filter(isAnswered).length)
const hasUnanswered = computed(() => answeredCount.value < questions.value.length)
const unansweredIndices = computed(() =>
  questions.value.map((q, idx) => isAnswered(q) ? -1 : idx).filter(i => i >= 0)
)
const progressPercent = computed(() => questions.value.length ? Math.round(answeredCount.value * 100 / questions.value.length) : 0)
const progressColor = computed(() => {
  if (progressPercent.value === 100) return '#67c23a'
  if (progressPercent.value >= 60) return '#ff8a65'
  return '#e6a23c'
})

const loadFromStorage = () => {
  const data = JSON.parse(sessionStorage.getItem('examData') || '{}')
  if (!data.examRecordId) { ElMessage.error('考试数据丢失'); router.push('/exam-list'); return }
  paperTitle.value = data.paperTitle || ''
  questions.value = data.questions || []
  initialized = false
  questions.value.forEach(q => {
    if (q.savedAnswer) {
      if (q.type === 'multi_choice') multiAnswers[q.id] = q.savedAnswer.split(',')
      else answers[q.id] = q.savedAnswer
    } else {
      if (q.type === 'multi_choice') multiAnswers[q.id] = []
      else answers[q.id] = ''
    }
  })
  setTimeout(() => { initialized = true }, 200)
  endTime = new Date(data.endTime).getTime()
  timeLeft.value = Math.max(0, Math.floor((endTime - Date.now()) / 1000))
  loading.value = false
  startTimer()
}

const startTimer = () => {
  timer = setInterval(() => {
    timeLeft.value = Math.max(0, Math.floor((endTime - Date.now()) / 1000))
    if (timeLeft.value <= 0) { clearInterval(timer); ElMessage.warning('时间到，自动交卷'); doSubmit() }
  }, 1000)
}

const saveAnswer = async (qid, ans) => {
  try { await request.post('/exam/answer', { examRecordId, questionId: qid, userAnswer: ans }) } catch(e){}
}

watch(answers, (v) => {
  if (!initialized) return
  for (const [k, val] of Object.entries(v)) saveAnswer(k, val)
}, { deep: true })
watch(multiAnswers, (v) => {
  if (!initialized) return
  for (const [k, val] of Object.entries(v)) saveAnswer(k, (val||[]).join(','))
}, { deep: true })

const formatTime = (s) => `${Math.floor(s/60).toString().padStart(2,'0')}:${(s%60).toString().padStart(2,'0')}`

// 跳转到某题
const scrollToQuestion = (idx) => {
  drawerVisible.value = false
  previewVisible.value = false
  const el = document.getElementById('q' + idx)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    el.classList.add('flash')
    setTimeout(() => el.classList.remove('flash'), 1200)
  }
}
const jumpFromPreview = (idx) => scrollToQuestion(idx)

// 打开交卷预览
const openPreview = () => { previewVisible.value = true }

// 确认交卷（实际提交）
const confirmSubmit = async () => {
  await doSubmit()
}

// 执行提交
const doSubmit = async () => {
  if (timer) clearInterval(timer)
  submitting.value = true
  try {
    const res = await request.post('/exam/submit/' + examRecordId)
    if (res && res.code === 200) {
      sessionStorage.setItem('submitResult', JSON.stringify(res.data))
      sessionStorage.removeItem('examData')
      router.push('/exam/result/' + examRecordId)
    }
  } catch (e) {
    ElMessage.error('交卷失败')
  } finally {
    submitting.value = false
  }
}

onBeforeUnmount(() => { if (timer) clearInterval(timer) })
loadFromStorage()
</script>

<style scoped>
.question-block { border: 1px solid var(--border-color); border-radius: 10px; padding: 20px; margin-bottom: 16px; background: var(--bg-card); transition: all 0.2s; }
.question-block:hover { box-shadow: var(--shadow); }
.question-block.flash { animation: flash 1.2s ease; }
@keyframes flash {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255,138,101,0); }
  30% { box-shadow: 0 0 0 4px rgba(255,138,101,0.4); }
}
.q-header { margin-bottom: 10px; display: flex; align-items: center; }
.q-num { font-size: 16px; font-weight: bold; margin-right: 10px; color: var(--accent); }
.q-content { font-size: 15px; margin-bottom: 14px; line-height: 1.7; }
.q-options { display: flex; flex-direction: column; gap: 6px; }
.q-option-item {
  display: flex !important;
  align-items: center;
  margin-right: 0 !important;
  padding: 10px 14px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  transition: all 0.2s;
  cursor: pointer;
}
.q-option-item:hover { background: var(--accent-light); border-color: var(--accent); }
.q-option-item.is-checked { background: rgba(255,138,101,0.1); border-color: var(--accent); }
.q-option-label {
  display: inline-flex; align-items: center; justify-content: center;
  width: 24px; height: 24px; border-radius: 50%;
  background: var(--accent-light); color: var(--accent);
  font-weight: bold; font-size: 13px; margin-right: 10px; flex-shrink: 0;
}
.q-option-text { font-size: 14px; line-height: 1.5; }

.tf-options { flex-direction: row !important; gap: 16px; }
.tf-options .q-option-item { min-width: 120px; justify-content: center; }
.tf-right .q-option-label { background: rgba(103,194,58,0.12); color: #67c23a; }
.tf-wrong .q-option-label { background: rgba(245,108,108,0.12); color: #f56c6c; }
.tf-right.is-checked { background: rgba(103,194,58,0.08); border-color: #67c23a; }
.tf-wrong.is-checked { background: rgba(245,108,108,0.08); border-color: #f56c6c; }

.countdown { display: inline-flex; align-items: center; padding: 6px 16px; border-radius: 8px; background: var(--accent-light); }

/* 进度条 */
.progress-bar { margin-bottom: 18px; padding: 14px 18px; background: var(--accent-light); border-radius: 10px; }
.progress-info { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px; color: var(--text-secondary); }
.progress-count { font-weight: 600; color: var(--accent); }

/* 答题卡抽屉 */
.card-summary { display: flex; gap: 12px; margin-bottom: 16px; }
.card-stat { flex: 1; text-align: center; padding: 12px; background: var(--accent-light); border-radius: 10px; }
.card-stat-num { font-size: 26px; font-weight: 800; line-height: 1.2; }
.card-stat-num.answered { color: #67c23a; }
.card-stat-num.unanswered { color: #e6a23c; }
.card-stat-num.total { color: var(--accent); }
.card-stat-label { font-size: 12px; color: var(--text-muted); margin-top: 2px; }
.card-legend { display: flex; gap: 16px; margin-bottom: 14px; font-size: 13px; color: var(--text-secondary); }
.dot { display: inline-block; width: 12px; height: 12px; border-radius: 3px; margin-right: 4px; vertical-align: middle; }
.dot.answered { background: #67c23a; }
.dot.unanswered { background: #e6a23c; }
.card-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 8px; }
.card-cell {
  aspect-ratio: 1; display: flex; align-items: center; justify-content: center;
  border-radius: 8px; font-weight: 600; cursor: pointer; transition: all 0.2s;
  border: 2px solid transparent;
}
.cell-answered { background: rgba(103,194,58,0.15); color: #67c23a; border-color: rgba(103,194,58,0.3); }
.cell-unanswered { background: rgba(230,162,60,0.15); color: #e6a23c; border-color: rgba(230,162,60,0.3); }
.card-cell:hover { transform: scale(1.1); }

/* 交卷预览 */
.preview-box { text-align: center; padding: 8px 0; }
.preview-emoji { font-size: 48px; margin-bottom: 8px; }
.preview-title { font-size: 18px; font-weight: 700; color: var(--text-primary); margin-bottom: 12px; }
.preview-summary { display: flex; justify-content: center; gap: 20px; font-size: 14px; color: var(--text-secondary); margin-bottom: 16px; }
.preview-summary b.ok { color: #67c23a; }
.preview-summary b.warn { color: #e6a23c; }
.preview-unanswered { background: var(--accent-light); border-radius: 8px; padding: 12px; }
.preview-subtitle { font-size: 13px; color: var(--text-muted); margin-bottom: 8px; }
.preview-nums { display: flex; flex-wrap: wrap; gap: 6px; justify-content: center; }
.preview-num {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 28px; height: 28px; padding: 0 6px;
  background: #fff; border: 1px solid #e6a23c; color: #e6a23c;
  border-radius: 6px; font-weight: 600; cursor: pointer; transition: all 0.2s;
}
.preview-num:hover { background: #e6a23c; color: #fff; }
</style>
