<template>
  <el-container style="height:100vh">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo">
        <span v-if="!isCollapse" style="font-size:17px;color:var(--sidebar-text);font-weight:700;letter-spacing:1px">📝 在线考试系统</span>
        <span v-else style="font-size:20px">📝</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        background-color="transparent"
        :text-color="menuTextColor"
        :active-text-color="menuActiveColor"
      >
        <template v-for="item in menuItems" :key="item.path">
          <el-menu-item :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>

      <!-- 折叠按钮 -->
      <div class="collapse-btn" @click="isCollapse = !isCollapse">
        <el-icon><component :is="isCollapse ? 'Expand' : 'Fold'" /></el-icon>
      </div>
    </el-aside>

    <!-- 右侧 -->
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator=">">
            <el-breadcrumb-item :to="{ path: '/home' }">
              <el-icon><HomeFilled /></el-icon> 首页
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <!-- 主题切换：轨道拨动开关 -->
          <ThemeSwitch />

          <el-tag :type="roleTagType" effect="dark" size="large">{{ roleName }}</el-tag>
          <span class="username clickable" @click="router.push('/profile')">{{ username }}</span>
          <el-button type="danger" plain size="small" @click="logout">
            <el-icon><SwitchButton /></el-icon> 退出
          </el-button>
        </div>
      </el-header>

      <!-- 内容 -->
      <el-main class="main-content">
        <div v-if="appError" class="error-page">
          <el-result icon="error" title="页面出错了" :sub-title="errorMsg">
            <template #extra>
              <el-button type="primary" @click="reloadPage">刷新页面</el-button>
              <el-button @click="goHome">返回首页</el-button>
            </template>
          </el-result>
        </div>
        <router-view v-else v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onErrorCaptured, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import ThemeSwitch from '../components/ThemeSwitch.vue'
import { useTheme } from '../composables/useTheme'

const router = useRouter()
const route = useRoute()
const { isDark } = useTheme()
const isCollapse = ref(false)
const username = ref('')
const role = ref('')
const appError = ref(false)
const errorMsg = ref('')

// 侧边栏菜单颜色随主题切换（浅色用深字，深色用浅字）
const menuTextColor = computed(() => isDark.value ? '#f0e0d0' : '#5d3a3a')
const menuActiveColor = computed(() => isDark.value ? '#ffb89a' : '#ff7043')

onErrorCaptured((err) => {
  console.error(err)
  errorMsg.value = err.message || '未知错误'
  appError.value = true
  return false
})

watch(() => route.fullPath, () => { if (appError.value) appError.value = false })

const activeMenu = computed(() => route.path)

const pageTitles = {
  '/home': '首页', '/questions': '题库管理', '/papers': '试卷管理',
  '/exam-list': '在线考试', '/my-scores': '我的成绩', '/score-ranking': '成绩排名',
  '/wrong-questions': '错题本', '/my-trend': '成绩趋势', '/analysis': '数据分析',
  '/users': '用户管理', '/profile': '个人中心',
}
const currentTitle = computed(() => {
  const path = route.path
  if (path.startsWith('/paper/edit')) return route.params.id ? '编辑试卷' : '组卷'
  if (path.startsWith('/exam/result')) return '考试结果'
  if (path.startsWith('/exam/')) return '正在考试'
  return pageTitles[path] || ''
})

const roleName = computed(() => ({ student:'学生', teacher:'教师', admin:'管理员' }[role.value] || role.value))
const roleTagType = computed(() => ({ admin:'danger', teacher:'warning', student:'success' }[role.value] || 'info'))

const menuItems = computed(() => {
  const profileItem = { title: '个人中心', path: '/profile', icon: 'UserFilled' }
  if (role.value === 'student') return [
    { title: '在线考试', path: '/exam-list', icon: 'EditPen' },
    { title: '我的成绩', path: '/my-scores', icon: 'DataAnalysis' },
    { title: '错题本', path: '/wrong-questions', icon: 'Collection' },
    { title: '成绩趋势', path: '/my-trend', icon: 'TrendCharts' },
    profileItem,
  ]
  if (role.value === 'teacher') return [
    { title: '题库管理', path: '/questions', icon: 'Reading' },
    { title: '试卷管理', path: '/papers', icon: 'Document' },
    { title: '成绩排名', path: '/score-ranking', icon: 'Trophy' },
    { title: '数据分析', path: '/analysis', icon: 'DataLine' },
    profileItem,
  ]
  return [
    { title: '用户管理', path: '/users', icon: 'UserFilled' },
    { title: '题库管理', path: '/questions', icon: 'Reading' },
    { title: '试卷管理', path: '/papers', icon: 'Document' },
    { title: '成绩排名', path: '/score-ranking', icon: 'Trophy' },
    { title: '数据分析', path: '/analysis', icon: 'DataLine' },
    profileItem,
  ]
})

onMounted(() => {
  const realName = sessionStorage.getItem('realName')
  username.value = realName || sessionStorage.getItem('username') || ''
  role.value = sessionStorage.getItem('role') || ''
})

const reloadPage = () => window.location.reload()
const goHome = () => { appError.value = false; router.push('/home') }
const logout = () => { sessionStorage.clear(); window.location.href = '/#/login' }
</script>

<style scoped>
.aside {
  background: var(--bg-sidebar);
  overflow-y: auto;
  position: relative;
  transition: width 0.3s;
}
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--sidebar-text);
  font-weight: bold;
  border-bottom: 1px solid var(--border-color);
  letter-spacing: 1px;
}
.el-menu {
  border-right: none !important;
}
.el-menu-item {
  transition: all 0.25s;
  margin: 4px 10px;
  border-radius: 10px;
  font-weight: 500;
}
.el-menu-item:hover {
  background: var(--menu-hover-bg) !important;
}
.el-menu-item.is-active {
  background: var(--menu-active-bg) !important;
  font-weight: 700;
  box-shadow: 0 2px 10px rgba(0,0,0,0.12);
}
.collapse-btn {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  color: var(--sidebar-text);
  cursor: pointer;
  font-size: 20px;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s;
}
.collapse-btn:hover {
  background: var(--menu-hover-bg);
  color: var(--sidebar-active);
}
.header {
  background: var(--bg-card);
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border-color);
  height: 60px;
  padding: 0 20px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.username {
  color: var(--text-secondary);
  font-size: 14px;
}
.username.clickable {
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 8px;
  transition: all 0.2s;
}
.username.clickable:hover {
  color: var(--accent);
  background: var(--accent-light);
}
.main-content {
  background: var(--bg-primary);
  padding: 20px;
  min-height: calc(100vh - 60px);
}
.error-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>
