<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <el-icon><Trophy /></el-icon> 成绩排名
        <el-select v-model="paperId" placeholder="选择试卷" style="width:260px;float:right" @change="loadData" clearable>
          <el-option v-for="p in papers" :key="p.id" :label="p.title" :value="p.id" />
        </el-select>
      </template>

      <el-table :data="displayList" border stripe v-loading="loading" v-if="paperId">
        <el-table-column label="排名" width="70" align="center">
          <template #default="{ $index }">
            <span v-if="$index >= 3" class="rank-badge rank-normal">{{ $index + 1 }}</span>
            <span v-else class="rank-badge" :class="'rank-' + ($index + 1)">
              {{ ['🥇','🥈','🥉'][$index] }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="real_name" label="姓名" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column label="成绩" sortable prop="score">
          <template #default="{row}">
            <el-tag :type="row.score >= 36 ? 'success' : row.score >= 24 ? 'warning' : 'danger'" effect="dark">
              {{ row.score }} 分
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submit_time" label="提交时间" />
      </el-table>

      <div v-else style="text-align:center;padding:80px;color:var(--text-muted)">
        <el-icon :size="48"><Trophy /></el-icon>
        <p>请选择一份试卷查看排名</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import request from '../api/request'

const loading = ref(false)
const paperId = ref('')
const papers = ref([])
const ranking = ref([])
const displayList = computed(() => ranking.value)

const loadPapers = async () => {
  const res = await request.get('/paper/list')
  papers.value = (res && res.data) ? res.data : []
}

const loadData = async () => {
  if (!paperId.value) return
  loading.value = true
  try {
    const res = await request.get('/score/ranking/' + paperId.value)
    ranking.value = (res && res.data) ? res.data : []
  } finally { loading.value = false }
}

loadPapers()
</script>

<style scoped>
.rank-normal {
  background: var(--bg-primary);
  color: var(--text-muted);
  border: 1px solid var(--border-color);
}
</style>
