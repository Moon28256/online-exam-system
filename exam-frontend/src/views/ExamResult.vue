<template>
  <div>
    <el-card shadow="never" v-loading="loading">
      <template #header><el-icon><Finished /></el-icon> 考试结果</template>

      <el-result v-if="result" :icon="result.totalScore >= 24 ? 'success' : 'warning'"
        :title="'得分：' + result.totalScore + ' 分'"
        :sub-title="'正确 ' + result.correctCount + ' / ' + result.totalCount + ' 题'">
        <template #extra>
          <el-button type="primary" @click="$router.push('/exam-list')"><el-icon><EditPen /></el-icon> 继续考试</el-button>
          <el-button @click="$router.push('/wrong-questions')"><el-icon><Collection /></el-icon> 查看错题本</el-button>
        </template>
      </el-result>

      <el-divider>答题详情</el-divider>

      <div v-for="(a, idx) in answers" :key="a.questionId" class="answer-item"
        :class="{ 'is-correct': a.isCorrect, 'is-wrong': !a.isCorrect }">
        <div class="a-header">
          <span class="a-num">{{ idx + 1 }}.</span>
          <el-tag :type="a.isCorrect ? 'success' : 'danger'" size="small" effect="dark">
            {{ a.isCorrect ? '✅ 正确' : '❌ 错误' }}
          </el-tag>
          <span style="margin-left:8px;color:var(--text-muted)">{{ typeMap[a.type] }} · {{ a.score }}/{{ a.maxScore }}分</span>
        </div>

        <div class="a-content">{{ a.content }}</div>

        <!-- 选择题/多选：显示选项 -->
        <div v-if="a.type === 'single_choice' || a.type === 'multi_choice'" class="a-options">
          <template v-for="opt in ['A','B','C','D']" :key="opt">
            <div v-if="a['option'+opt]" class="a-opt" :class="optionClass(a, opt)">
              <span class="a-opt-label">{{ opt }}</span>
              <span class="a-opt-text">{{ a['option'+opt] }}</span>
              <el-icon v-if="isCorrectOpt(a, opt)" class="a-opt-icon correct-icon"><CircleCheck /></el-icon>
              <el-icon v-else-if="isWrongOpt(a, opt)" class="a-opt-icon wrong-icon"><CircleClose /></el-icon>
            </div>
          </template>
        </div>

        <!-- 判断题：特殊显示 -->
        <div v-if="a.type === 'true_false'" class="a-options tf-result">
          <div class="a-opt" :class="tfClass(a, 'A')">
            <span class="a-opt-label">✓</span> 正确
            <el-icon v-if="isCorrectOpt(a, 'A')" class="a-opt-icon correct-icon"><CircleCheck /></el-icon>
            <el-icon v-else-if="isWrongOpt(a, 'A')" class="a-opt-icon wrong-icon"><CircleClose /></el-icon>
          </div>
          <div class="a-opt" :class="tfClass(a, 'B')">
            <span class="a-opt-label">✗</span> 错误
            <el-icon v-if="isCorrectOpt(a, 'B')" class="a-opt-icon correct-icon"><CircleCheck /></el-icon>
            <el-icon v-else-if="isWrongOpt(a, 'B')" class="a-opt-icon wrong-icon"><CircleClose /></el-icon>
          </div>
        </div>

        <div class="a-answer">
          <span>你的答案：<el-tag :type="a.isCorrect ? 'success' : 'danger'" size="small">{{ a.userAnswer || '未作答' }}</el-tag></span>
          <span v-if="!a.isCorrect" style="margin-left:16px">正确答案：<el-tag type="success" size="small">{{ a.correctAnswer }}</el-tag></span>
          <span v-else style="margin-left:16px;color:var(--text-muted)">正确答案：{{ a.correctAnswer }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const loading = ref(true)
const result = ref(null)
const typeMap = { single_choice:'单选', multi_choice:'多选', true_false:'判断', fill_blank:'填空', essay:'简答' }
const answers = computed(() => result.value?.answers || [])

// 获取正确选项列表（多选用）
const correctOpts = (a) => {
  if (!a.correctAnswer) return []
  return a.correctAnswer.split(',').map(s => s.trim()).filter(Boolean)
}
// 获取用户选择列表
const userOpts = (a) => {
  if (!a.userAnswer) return []
  return a.userAnswer.split(',').map(s => s.trim()).filter(Boolean)
}

const isCorrectOpt = (a, opt) => correctOpts(a).includes(opt)
const isUserPick = (a, opt) => userOpts(a).includes(opt)
// 用户选错的选项：用户选了 且 不是正确答案
const isWrongOpt = (a, opt) => isUserPick(a, opt) && !isCorrectOpt(a, opt)

const optionClass = (a, opt) => ({
  'is-correct-opt': isCorrectOpt(a, opt),
  'is-wrong-pick': isUserPick(a, opt) && !isCorrectOpt(a, opt),
  'is-user-pick': isUserPick(a, opt) && isCorrectOpt(a, opt),
})

const tfClass = (a, opt) => ({
  'is-correct-opt': isCorrectOpt(a, opt),
  'is-wrong-pick': isUserPick(a, opt) && !isCorrectOpt(a, opt),
})

const loadResult = async () => {
  try {
    const submitResult = JSON.parse(sessionStorage.getItem('submitResult') || '{}')
    if (submitResult.examRecordId) { result.value = submitResult }
    loading.value = false
  } catch (e) { loading.value = false }
}
loadResult()
</script>

<style scoped>
.answer-item {
  border: 1px solid var(--border-color);
  border-left: 4px solid #ccc;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 16px;
  background: var(--bg-card);
}
.answer-item.is-correct { border-left-color: #67c23a; }
.answer-item.is-wrong { border-left-color: #f56c6c; }

.a-header { margin-bottom: 8px; display: flex; align-items: center; gap: 8px; }
.a-num { font-weight: bold; color: var(--accent); font-size: 16px; }
.a-content { font-size: 15px; margin-bottom: 12px; line-height: 1.7; color: var(--text-primary); }

/* 选项展示 */
.a-options { display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; }
.a-opt {
  display: flex; align-items: center;
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  transition: all 0.15s;
  position: relative;
}
.a-opt-label {
  display: inline-flex; align-items: center; justify-content: center;
  width: 24px; height: 24px; border-radius: 50%;
  background: var(--accent-light); color: var(--accent);
  font-weight: bold; font-size: 13px; margin-right: 10px; flex-shrink: 0;
}
.a-opt-text { font-size: 14px; line-height: 1.5; }
.a-opt-icon { margin-left: auto; font-size: 18px; flex-shrink: 0; }
.a-opt-icon.correct-icon { color: #67c23a; }
.a-opt-icon.wrong-icon { color: #f56c6c; }

/* 正确选项高亮 */
.a-opt.is-correct-opt {
  background: rgba(103,194,58,0.08);
  border-color: #67c23a;
}
.a-opt.is-correct-opt .a-opt-label {
  background: rgba(103,194,58,0.15);
  color: #67c23a;
}
/* 用户选错的高亮 */
.a-opt.is-wrong-pick {
  background: rgba(245,108,108,0.08);
  border-color: #f56c6c;
}
.a-opt.is-wrong-pick .a-opt-label {
  background: rgba(245,108,108,0.15);
  color: #f56c6c;
}
/* 用户选对的高亮 */
.a-opt.is-user-pick {
  background: rgba(255,138,101,0.08);
  border-color: var(--accent);
}

/* 判断题并排 */
.tf-result { flex-direction: row !important; gap: 12px; }
.tf-result .a-opt { flex: 1; justify-content: center; }

.a-answer { padding-top: 8px; border-top: 1px solid var(--border-color); }
</style>
