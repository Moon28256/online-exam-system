import { createRouter, createWebHashHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 角色权限分组
const student = ['student']
const teacher = ['teacher', 'admin']
const adminOnly = ['admin']
const all = ['student', 'teacher', 'admin']

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('../views/Home.vue'), meta: { roles: all } },
      // 题库管理（教师/管理员）
      { path: 'questions', name: 'Questions', component: () => import('../views/QuestionBank.vue'), meta: { roles: teacher } },
      // 试卷管理（教师/管理员）
      { path: 'papers', name: 'Papers', component: () => import('../views/PaperList.vue'), meta: { roles: teacher } },
      { path: 'paper/edit/:id?', name: 'PaperEdit', component: () => import('../views/PaperEdit.vue'), meta: { roles: teacher } },
      // 在线考试（学生）
      { path: 'exam-list', name: 'ExamList', component: () => import('../views/ExamList.vue'), meta: { roles: student } },
      { path: 'exam/:examRecordId', name: 'ExamTaking', component: () => import('../views/ExamTaking.vue'), meta: { roles: student } },
      { path: 'exam/result/:examRecordId', name: 'ExamResult', component: () => import('../views/ExamResult.vue'), meta: { roles: all } },
      // 成绩
      { path: 'my-scores', name: 'MyScores', component: () => import('../views/MyScores.vue'), meta: { roles: student } },
      { path: 'score-ranking', name: 'ScoreRanking', component: () => import('../views/ScoreRanking.vue'), meta: { roles: teacher } },
      // 错题本
      { path: 'wrong-questions', name: 'WrongQuestions', component: () => import('../views/WrongQuestions.vue'), meta: { roles: student } },
      // 数据分析
      { path: 'my-trend', name: 'MyTrend', component: () => import('../views/MyTrend.vue'), meta: { roles: student } },
      { path: 'analysis', name: 'Analysis', component: () => import('../views/Analysis.vue'), meta: { roles: teacher } },
      // 用户管理（仅管理员）
      { path: 'users', name: 'UserManagement', component: () => import('../views/UserManagement.vue'), meta: { roles: adminOnly } },
      // 个人中心（所有角色）
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue'), meta: { roles: all } },
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')
  const role = sessionStorage.getItem('role')

  // 未登录 → 只能去登录页
  if (!token && to.path !== '/login') {
    return next('/login')
  }

  // 已登录 → 不能去登录页
  if (token && to.path === '/login') {
    return next('/home')
  }

  // 角色权限检查
  if (to.meta && to.meta.roles) {
    const allowedRoles = to.meta.roles
    if (role && !allowedRoles.includes(role)) {
      ElMessage.warning('无权访问该页面')
      return next('/home')
    }
  }

  next()
})

export default router
