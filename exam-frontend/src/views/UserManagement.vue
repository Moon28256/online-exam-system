<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span><el-icon><UserFilled /></el-icon> 用户管理</span>
          <el-tag effect="plain">共 {{ users.length }} 人</el-tag>
        </div>
      </template>

      <!-- 角色统计 -->
      <el-row :gutter="12" style="margin-bottom:16px">
        <el-col :span="8" v-for="s in roleStats" :key="s.role">
          <div class="stat-mini" :class="s.cls" @click="filterRole = filterRole === s.role ? '' : s.role">
            <div class="stat-mini-num">{{ s.count }}</div>
            <div class="stat-mini-label">{{ s.label }}</div>
          </div>
        </el-col>
      </el-row>

      <!-- 筛选搜索栏 -->
      <div class="filter-bar">
        <el-select v-model="filterRole" placeholder="全部角色" clearable size="default" style="width:140px">
          <el-option label="学生" value="student" />
          <el-option label="教师" value="teacher" />
          <el-option label="管理员" value="admin" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索用户名 / 姓名" clearable size="default" style="width:260px" :prefix-icon="Search" />
        <el-button size="default" @click="filterRole = ''; keyword = ''">重置</el-button>
        <span class="filter-result">显示 {{ filteredUsers.length }} / {{ users.length }} 人</span>
      </div>

      <el-table :data="filteredUsers" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="120" show-overflow-tooltip />
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="realName" label="姓名" width="110" />
        <el-table-column label="角色" width="90">
          <template #default="{row}">
            <el-tag :type="roleTagType(row.role)" size="small">
              {{ roleMap[row.role] || row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createdTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{row}">
            <el-button size="small" type="danger" plain @click="delUser(row)"
              :disabled="row.role === 'admin'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!filteredUsers.length && !loading" class="empty-tip">未找到匹配的用户</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, Search } from '@element-plus/icons-vue'
import request from '../api/request'

const loading = ref(false)
const users = ref([])
const filterRole = ref('')
const keyword = ref('')
const roleMap = { student: '学生', teacher: '教师', admin: '管理员' }

const roleTagType = (r) => ({ admin: 'danger', teacher: 'warning', student: 'success' }[r] || 'info')

// 角色统计
const roleStats = computed(() => {
  const count = (r) => users.value.filter(u => u.role === r).length
  return [
    { role: 'student', label: '学生', count: count('student'), cls: 'stat-student' },
    { role: 'teacher', label: '教师', count: count('teacher'), cls: 'stat-teacher' },
    { role: 'admin', label: '管理员', count: count('admin'), cls: 'stat-admin' },
  ]
})

// 过滤 + 搜索
const filteredUsers = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return users.value.filter(u => {
    if (filterRole.value && u.role !== filterRole.value) return false
    if (kw) {
      const name = (u.username || '').toLowerCase()
      const real = (u.realName || '').toLowerCase()
      if (!name.includes(kw) && !real.includes(kw)) return false
    }
    return true
  })
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/list')
    users.value = (res && res.data) ? res.data : []
  } finally {
    loading.value = false
  }
}

const delUser = async (row) => {
  if (row.role === 'admin') {
    ElMessage.warning('不能删除管理员')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除用户"${row.username}"吗？`, '警告', { type: 'warning' })
    await request.delete(`/user/${row.id}`)
    ElMessage.success('已删除')
    loadData()
  } catch (e) {
    // 用户取消
  }
}

loadData()
</script>

<style scoped>
.stat-mini {
  padding: 14px;
  border-radius: 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.25s;
  border: 2px solid transparent;
}
.stat-mini:hover { transform: translateY(-2px); box-shadow: var(--shadow-hover); }
.stat-mini-num { font-size: 26px; font-weight: 800; line-height: 1.2; }
.stat-mini-label { font-size: 13px; color: var(--text-muted); margin-top: 2px; }
.stat-student { background: rgba(103,194,58,0.1); }
.stat-student .stat-mini-num { color: #67c23a; }
.stat-teacher { background: rgba(230,162,60,0.1); }
.stat-teacher .stat-mini-num { color: #e6a23c; }
.stat-admin { background: rgba(245,108,108,0.1); }
.stat-admin .stat-mini-num { color: #f56c6c; }

.filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.filter-result { color: var(--text-muted); font-size: 13px; margin-left: auto; }

.empty-tip { text-align: center; padding: 40px; color: var(--text-muted); }
</style>
