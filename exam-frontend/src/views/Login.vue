<template>
  <div class="login-page">
    <!-- 柔和有机形状背景 -->
    <div class="soft-bg">
      <div class="soft-blob blob-1"></div>
      <div class="soft-blob blob-2"></div>
      <div class="soft-blob blob-3"></div>
      <div class="soft-blob blob-4"></div>
    </div>

    <!-- 漂浮文具 -->
    <div class="stationery-bg">
      <!-- 铅笔 -->
      <div class="stationery st-pencil" style="top:12%;left:8%;animation-delay:0s">
        <svg viewBox="0 0 40 40" width="50" height="50">
          <polygon points="28,4 36,12 14,34 6,34 6,26" fill="#feca57" stroke="#2d3436" stroke-width="1.5"/>
          <polygon points="28,4 36,12 32,8 32,8" fill="#ff9ff3"/>
          <polygon points="6,26 14,34 6,34" fill="#2d3436"/>
          <line x1="10" y1="30" x2="30" y2="10" stroke="#2d3436" stroke-width="1"/>
        </svg>
      </div>
      <!-- 书本 -->
      <div class="stationery st-book" style="top:18%;right:10%;animation-delay:-3s">
        <svg viewBox="0 0 40 40" width="56" height="56">
          <rect x="6" y="8" width="28" height="24" rx="2" fill="#ff6b6b" stroke="#2d3436" stroke-width="1.5"/>
          <rect x="6" y="8" width="28" height="6" fill="#ee5a6f"/>
          <line x1="20" y1="14" x2="20" y2="32" stroke="#2d3436" stroke-width="1"/>
          <line x1="11" y1="20" x2="17" y2="20" stroke="#fff" stroke-width="1"/>
          <line x1="11" y1="24" x2="17" y2="24" stroke="#fff" stroke-width="1"/>
          <line x1="23" y1="20" x2="29" y2="20" stroke="#fff" stroke-width="1"/>
        </svg>
      </div>
      <!-- 尺子 -->
      <div class="stationery st-ruler" style="bottom:18%;left:12%;animation-delay:-6s">
        <svg viewBox="0 0 40 40" width="60" height="40">
          <rect x="4" y="14" width="32" height="12" rx="2" fill="#48dbfb" stroke="#2d3436" stroke-width="1.5"/>
          <line x1="8" y1="14" x2="8" y2="20" stroke="#2d3436" stroke-width="1"/>
          <line x1="14" y1="14" x2="14" y2="18" stroke="#2d3436" stroke-width="1"/>
          <line x1="20" y1="14" x2="20" y2="20" stroke="#2d3436" stroke-width="1"/>
          <line x1="26" y1="14" x2="26" y2="18" stroke="#2d3436" stroke-width="1"/>
          <line x1="32" y1="14" x2="32" y2="20" stroke="#2d3436" stroke-width="1"/>
        </svg>
      </div>
      <!-- 橡皮 -->
      <div class="stationery st-eraser" style="bottom:12%;right:14%;animation-delay:-9s">
        <svg viewBox="0 0 40 40" width="48" height="48">
          <rect x="8" y="12" width="24" height="16" rx="3" fill="#ff9ff3" stroke="#2d3436" stroke-width="1.5"/>
          <rect x="8" y="12" width="24" height="6" rx="3" fill="#feca57"/>
          <line x1="14" y1="20" x2="26" y2="20" stroke="#2d3436" stroke-width="0.8" opacity="0.4"/>
        </svg>
      </div>
      <!-- 毕业帽 -->
      <div class="stationery st-cap" style="top:48%;left:5%;animation-delay:-2s">
        <svg viewBox="0 0 40 40" width="52" height="52">
          <polygon points="20,8 36,16 20,24 4,16" fill="#2d3436"/>
          <polygon points="20,8 36,16 20,24 4,16" fill="none" stroke="#2d3436" stroke-width="1"/>
          <path d="M12,19 L12,26 Q20,30 28,26 L28,19" fill="#2d3436" stroke="#2d3436" stroke-width="1"/>
          <line x1="36" y1="16" x2="36" y2="24" stroke="#feca57" stroke-width="1.5"/>
          <circle cx="36" cy="25" r="2" fill="#feca57"/>
        </svg>
      </div>
      <!-- 笔记本 -->
      <div class="stationery st-notebook" style="top:55%;right:6%;animation-delay:-5s">
        <svg viewBox="0 0 40 40" width="50" height="50">
          <rect x="8" y="6" width="24" height="28" rx="2" fill="#a29bfe" stroke="#2d3436" stroke-width="1.5"/>
          <line x1="12" y1="6" x2="12" y2="34" stroke="#fff" stroke-width="1.5"/>
          <line x1="16" y1="14" x2="28" y2="14" stroke="#fff" stroke-width="1"/>
          <line x1="16" y1="20" x2="28" y2="20" stroke="#fff" stroke-width="1"/>
          <line x1="16" y1="26" x2="24" y2="26" stroke="#fff" stroke-width="1"/>
        </svg>
      </div>
      <!-- 对勾 -->
      <div class="stationery st-check" style="top:30%;left:20%;animation-delay:-7s">
        <svg viewBox="0 0 40 40" width="44" height="44">
          <circle cx="20" cy="20" r="14" fill="#00b894" stroke="#2d3436" stroke-width="1.5"/>
          <path d="M13,20 L18,25 L27,15" fill="none" stroke="#fff" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <!-- 回形针 -->
      <div class="stationery st-clip" style="bottom:40%;right:25%;animation-delay:-4s">
        <svg viewBox="0 0 40 40" width="40" height="40">
          <path d="M14,8 L14,28 Q14,32 18,32 Q22,32 22,28 L22,14 Q22,12 20,12 Q18,12 18,14 L18,26" fill="none" stroke="#576574" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
    </div>

    <!-- 分屏布局 -->
    <div class="login-container">
      <!-- 左侧：品牌展示 -->
      <div class="brand-panel">
        <div class="brand-content">
          <!-- 顶部 Logo -->
          <div class="brand-logo">
            <div class="logo-icon">
              <svg viewBox="0 0 40 40" width="40" height="40">
                <rect x="4" y="6" width="32" height="28" rx="3" fill="none" stroke="currentColor" stroke-width="2"/>
                <line x1="12" y1="14" x2="28" y2="14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <line x1="12" y1="20" x2="24" y2="20" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <line x1="12" y1="26" x2="20" y2="26" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <circle cx="30" cy="26" r="4" fill="#67c23a"/>
                <path d="M28 26 L29.5 27.5 L32 24.5" fill="none" stroke="white" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <span class="logo-text">ExamPro</span>
          </div>

          <!-- 打字机标题 -->
          <h1 class="typing-title">
            <span class="typing-text">{{ displayTitle }}</span>
            <span class="typing-cursor">|</span>
          </h1>
          <p class="brand-subtitle">Online Examination System</p>

          <!-- 系统特性标签 -->
          <div class="features">
            <div class="feature-tag" v-for="(f, i) in features" :key="f.icon" :style="{ animationDelay: i * 0.15 + 's' }">
              <span class="f-icon">{{ f.icon }}</span>
              <span>{{ f.text }}</span>
            </div>
          </div>

          <!-- 统计数据 -->
          <div class="stats">
            <div class="stat-item" v-for="(s, i) in stats" :key="s.label">
              <div class="stat-num">{{ animatedStats[i] }}<span class="stat-unit">{{ s.unit }}</span></div>
              <div class="stat-label">{{ s.label }}</div>
            </div>
          </div>

          <!-- 浮动装饰图标 -->
          <div class="float-icons">
            <div class="float-icon" v-for="i in 6" :key="i" :style="floatStyle(i)">
              <component :is="floatIcons[i-1]" />
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：登录表单 -->
      <div class="form-panel">
        <div class="form-card">
          <div class="form-header">
            <h2>欢迎回来 👋</h2>
            <p>登录您的账户以继续</p>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" size="large" class="login-form">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" @click="login" :loading="loading">
                <span v-if="!loading">登 录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span class="footer-text">还没有账号？</span>
            <el-button class="register-link" size="large" @click="showRegister = true">
              <el-icon><Plus /></el-icon> 立即注册
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 注册弹窗 -->
    <el-dialog v-model="showRegister" title="注册新账号" width="440px" destroy-on-close append-to-body>
      <el-form :model="regForm" :rules="regRules" ref="regFormRef" size="large">
        <el-form-item prop="username">
          <el-input v-model="regForm.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="regForm.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item prop="realName">
          <el-input v-model="regForm.realName" placeholder="真实姓名" :prefix-icon="Avatar" />
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="regForm.role" placeholder="选择角色" style="width:100%">
            <el-option label="🧑‍🎓 学生" value="student" />
            <el-option label="🧑‍🏫 教师" value="teacher" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegister = false">取消</el-button>
        <el-button type="primary" @click="register" :loading="regLoading">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, Plus, Avatar, EditPen, DataLine, TrophyBase, Reading, Timer, CircleCheck } from '@element-plus/icons-vue'
import request from '../api/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const regLoading = ref(false)
const showRegister = ref(false)
const formRef = ref(null)
const regFormRef = ref(null)

const form = ref({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const regForm = ref({ username: '', password: '', realName: '', role: 'student' })
const regRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

// 打字机效果
const fullTitle = '在线考试系统'
const displayTitle = ref('')
let typingTimer = null

const startTyping = () => {
  let i = 0
  typingTimer = setInterval(() => {
    if (i < fullTitle.length) {
      displayTitle.value += fullTitle[i]
      i++
    } else {
      clearInterval(typingTimer)
    }
  }, 180)
}

// 特性标签
const features = [
  { icon: '📝', text: '智能组卷' },
  { icon: '📊', text: '实时分析' },
  { icon: '📖', text: '错题归纳' },
  { icon: '🏆', text: '成绩排名' },
]

// 统计数据（动画数字）
const stats = [
  { label: '题库数量', value: 54, unit: '+' },
  { label: '试卷数量', value: 10, unit: '份' },
  { label: '考试人次', value: 115, unit: '次' },
]
const animatedStats = reactive([0, 0, 0])

const animateStats = () => {
  stats.forEach((s, i) => {
    let current = 0
    const step = Math.max(1, Math.floor(s.value / 40))
    const timer = setInterval(() => {
      current += step
      if (current >= s.value) {
        current = s.value
        clearInterval(timer)
      }
      animatedStats[i] = current
    }, 30)
  })
}

// 浮动图标
const floatIcons = [EditPen, DataLine, TrophyBase, Reading, Timer, CircleCheck]
const floatStyle = (i) => {
  const positions = [
    { top: '8%', left: '10%', size: '32px', delay: '0s' },
    { top: '15%', right: '8%', size: '28px', delay: '1s' },
    { bottom: '20%', left: '5%', size: '26px', delay: '2s' },
    { bottom: '12%', right: '12%', size: '30px', delay: '0.5s' },
    { top: '50%', left: '3%', size: '24px', delay: '1.5s' },
    { top: '40%', right: '5%', size: '22px', delay: '2.5s' },
  ]
  const p = positions[i - 1]
  return {
    top: p.top || 'auto',
    left: p.left || 'auto',
    right: p.right || 'auto',
    bottom: p.bottom || 'auto',
    fontSize: p.size,
    animationDelay: p.delay,
  }
}

// 登录
const login = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await request.post('/user/login', form.value)
    if (res.code === 200 && res.data) {
      sessionStorage.setItem('token', res.data.token)
      sessionStorage.setItem('username', form.value.username)
      const infoRes = await request.get('/user/info')
      if (infoRes.code === 200 && infoRes.data) {
        sessionStorage.setItem('role', infoRes.data.role)
        sessionStorage.setItem('userId', infoRes.data.userId)
        sessionStorage.setItem('realName', infoRes.data.realName || '')
      }
      ElMessage.success('登录成功')
      router.push('/home')
    }
  } finally { loading.value = false }
}

// 注册
const register = async () => {
  const valid = await regFormRef.value.validate().catch(() => false)
  if (!valid) return
  regLoading.value = true
  try {
    const res = await request.post('/user/register', regForm.value)
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      showRegister.value = false
      regForm.value = { username: '', password: '', realName: '', role: 'student' }
    }
  } finally { regLoading.value = false }
}

onMounted(() => {
  startTyping()
  setTimeout(animateStats, 500)
})

onBeforeUnmount(() => {
  if (typingTimer) clearInterval(typingTimer)
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 50%, #ff9a9e 100%);
  position: relative;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 柔和有机形状背景 */
.soft-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}
.soft-blob {
  position: absolute;
  border-radius: 50% 45% 55% 48% / 52% 48% 52% 48%;
  filter: blur(60px);
  opacity: 0.5;
  animation: blobFloat 18s ease-in-out infinite;
}
.blob-1 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, #ff6b6b, transparent 70%);
  top: -15%; left: -10%;
  animation-delay: 0s;
}
.blob-2 {
  width: 450px; height: 450px;
  background: radial-gradient(circle, #feca57, transparent 70%);
  bottom: -12%; right: -8%;
  animation-delay: -6s;
}
.blob-3 {
  width: 400px; height: 400px;
  background: radial-gradient(circle, #ff9ff3, transparent 70%);
  top: 45%; left: 25%;
  animation-delay: -12s;
}
.blob-4 {
  width: 350px; height: 350px;
  background: radial-gradient(circle, #48dbfb, transparent 70%);
  top: 15%; right: 15%;
  animation-delay: -3s;
}

@keyframes blobFloat {
  0%, 100% { transform: translate(0, 0) scale(1) rotate(0deg); }
  25% { transform: translate(80px, -60px) scale(1.15) rotate(12deg); }
  50% { transform: translate(-60px, 80px) scale(0.9) rotate(-8deg); }
  75% { transform: translate(-80px, -50px) scale(1.12) rotate(10deg); }
}

/* 漂浮文具背景 */
.stationery-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}
.stationery {
  position: absolute;
  opacity: 0.5;
  filter: drop-shadow(0 4px 8px rgba(0,0,0,0.08));
  animation: stationeryFloat 14s ease-in-out infinite;
}
@keyframes stationeryFloat {
  0% { transform: translateY(0) rotate(-8deg); }
  25% { transform: translateY(-25px) rotate(5deg); }
  50% { transform: translateY(-12px) rotate(-12deg); }
  75% { transform: translateY(-30px) rotate(8deg); }
  100% { transform: translateY(0) rotate(-8deg); }
}
/* 各文具不同的漂浮节奏 */
.st-pencil { animation-duration: 16s; }
.st-book { animation-duration: 13s; }
.st-ruler { animation-duration: 18s; }
.st-eraser { animation-duration: 15s; }
.st-cap { animation-duration: 17s; }
.st-notebook { animation-duration: 14s; }
.st-check { animation-duration: 12s; }
.st-clip { animation-duration: 19s; }

/* 分屏容器 */
.login-container {
  display: flex;
  width: 1050px;
  min-height: 620px;
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.15);
  z-index: 1;
  position: relative;
}

/* 左侧品牌面板 - 清新绿色 */
.brand-panel {
  flex: 1.1;
  background: linear-gradient(135deg, #00b894 0%, #00cec9 40%, #55efc4 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 44px;
  overflow: hidden;
}
.brand-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 30% 70%, rgba(255,255,255,0.2) 0%, transparent 50%),
    radial-gradient(circle at 70% 30%, rgba(255,255,255,0.15) 0%, transparent 50%);
  pointer-events: none;
}

/* 左侧浮动装饰 - 可爱图标 */
.float-icons {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}
.float-icon {
  position: absolute;
  color: rgba(255,255,255,0.3);
  animation: iconFloat 12s ease-in-out infinite;
}
@keyframes iconFloat {
  0%, 100% { transform: translateY(0) rotate(-5deg) scale(1); }
  33% { transform: translateY(-18px) rotate(8deg) scale(1.08); }
  66% { transform: translateY(-8px) rotate(-6deg) scale(1.05); }
}

.brand-content {
  position: relative;
  z-index: 2;
  color: #fff;
  width: 100%;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 36px;
}
.logo-icon {
  width: 56px; height: 56px;
  background: rgba(255,255,255,0.25);
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255,255,255,0.3);
  color: #fff;
  box-shadow: 0 6px 16px rgba(0,0,0,0.15);
}
.logo-text {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 1px;
  color: #fff;
}

/* 打字机标题 */
.typing-title {
  font-size: 40px;
  font-weight: 900;
  margin: 0 0 10px 0;
  line-height: 1.3;
  color: #fff;
}
.typing-text {
  display: inline;
}
.typing-cursor {
  display: inline-block;
  margin-left: 2px;
  color: #fff;
  animation: blink 1s step-end infinite;
  font-weight: 300;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.brand-subtitle {
  font-size: 15px;
  color: rgba(255,255,255,0.9);
  letter-spacing: 2px;
  margin: 0 0 32px 0;
  font-weight: 500;
}

/* 特性标签 - 活泼风格 */
.features {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 36px;
}
.feature-tag {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 10px 18px;
  background: rgba(255,255,255,0.25);
  border-radius: 50px;
  font-size: 14px;
  font-weight: 600;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255,255,255,0.35);
  animation: fadeInUp 0.6s ease both;
  transition: all 0.3s;
}
.feature-tag:hover {
  transform: translateY(-3px) scale(1.03);
  background: rgba(255,255,255,0.35);
}
.f-icon { font-size: 18px; }

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(18px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 统计数据 - 温暖风格 */
.stats {
  display: flex;
  gap: 36px;
}
.stat-item { text-align: left; }
.stat-num {
  font-size: 34px;
  font-weight: 800;
  color: #fff;
  line-height: 1.2;
}
.stat-unit {
  font-size: 18px;
  font-weight: 600;
  margin-left: 3px;
}
.stat-label {
  font-size: 13px;
  color: rgba(255,255,255,0.8);
  margin-top: 4px;
  font-weight: 500;
}

/* 右侧表单面板 */
.form-panel {
  width: 440px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 44px;
}

.form-card { width: 100%; max-width: 360px; }

.form-header {
  margin-bottom: 36px;
}
.form-header h2 {
  font-size: 28px;
  font-weight: 800;
  color: #2d3436;
  margin: 0 0 10px 0;
}
.form-header p {
  font-size: 15px;
  color: #636e72;
  margin: 0;
}

.login-form { margin-bottom: 24px; }

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 14px;
  background: linear-gradient(135deg, #00b894, #00cec9);
  border: none;
  transition: all 0.3s;
  letter-spacing: 1px;
  color: #fff;
}
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(0, 184, 148, 0.35);
  background: linear-gradient(135deg, #00cec9, #00b894);
}

.form-footer {
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}
.footer-text {
  font-size: 15px;
  color: #636e72;
}
.register-link {
  height: 42px !important;
  padding: 0 28px !important;
  font-size: 16px !important;
  font-weight: 700 !important;
  color: #c44569 !important;
  border: 2px solid #c44569 !important;
  background: #fff !important;
  border-radius: 12px !important;
  letter-spacing: 1px;
  transition: all 0.3s !important;
}
.register-link:hover {
  color: #fff !important;
  background: #c44569 !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(196, 69, 105, 0.35);
}

/* 响应式 */
@media (max-width: 1024px) {
  .login-container {
    flex-direction: column;
    width: 90%;
    max-width: 520px;
    min-height: auto;
  }
  .brand-panel { padding: 32px; }
  .form-panel { width: 100%; padding: 32px; }
  .typing-title { font-size: 32px; }
  .stats { gap: 24px; }
  .stat-num { font-size: 28px; }
}
</style>
