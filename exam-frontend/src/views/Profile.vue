<template>
  <div v-loading="loading">
    <el-row :gutter="20">
      <!-- 左侧：个人卡片 -->
      <el-col :span="8">
        <el-card shadow="never" class="profile-card glass-card">
          <div class="avatar-wrap">
            <div class="avatar">{{ avatarText }}</div>
          </div>
          <div class="user-name">{{ profile.realName || profile.username }}</div>
          <div class="user-username">@{{ profile.username }}</div>
          <el-tag :type="roleTagType" effect="dark" size="large" class="role-tag">
            {{ roleMap[profile.role] || profile.role }}
          </el-tag>
          <el-divider />
          <div class="meta-list">
            <div class="meta-item">
              <el-icon><Message /></el-icon>
              <span>{{ profile.email || '未填写邮箱' }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Calendar /></el-icon>
              <span>注册于 {{ formatDate(profile.createdTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：编辑区 -->
      <el-col :span="16">
        <!-- 资料编辑 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="section-header">
              <el-icon><Edit /></el-icon>
              <span>基本资料</span>
            </div>
          </template>
          <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="90px" size="large">
            <el-form-item label="用户名">
              <el-input :model-value="profile.username" disabled>
                <template #suffix><span class="disabled-tip">不可修改</span></template>
              </el-input>
            </el-form-item>
            <el-form-item label="角色">
              <el-input :model-value="roleMap[profile.role]" disabled />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" :prefix-icon="User" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" :prefix-icon="Message" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="savingProfile">
                <el-icon><Check /></el-icon> 保存修改
              </el-button>
              <el-button @click="resetProfile">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 修改密码 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="section-header">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </div>
          </template>
          <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="90px" size="large" style="max-width:480px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" :prefix-icon="Lock" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" :prefix-icon="Lock" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" :prefix-icon="Lock" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword" :loading="savingPwd">
                <el-icon><Key /></el-icon> 确认修改
              </el-button>
              <el-button @click="resetPwd">重置</el-button>
            </el-form-item>
          </el-form>
          <el-alert type="info" :closable="false" show-icon>
            <template #title>修改密码后需要重新登录</template>
          </el-alert>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Message, Edit, Lock, Check, Key, Calendar } from '@element-plus/icons-vue'
import request from '../api/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const savingProfile = ref(false)
const savingPwd = ref(false)
const profileFormRef = ref(null)
const pwdFormRef = ref(null)

const profile = reactive({
  username: '', role: '', realName: '', email: '', createdTime: null
})

const profileForm = reactive({ realName: '', email: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const roleMap = { student: '学生', teacher: '教师', admin: '管理员' }
const roleTagType = computed(() => ({ admin: 'danger', teacher: 'warning', student: 'success' }[profile.role] || 'info'))
const avatarText = computed(() => {
  const name = profile.realName || profile.username || '?'
  return name.charAt(0).toUpperCase()
})

const profileRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const formatDate = (d) => {
  if (!d) return '未知'
  return new Date(d).toLocaleDateString('zh-CN')
}

const loadProfile = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/profile')
    if (res && res.code === 200 && res.data) {
      Object.assign(profile, res.data)
      profileForm.realName = res.data.realName || ''
      profileForm.email = res.data.email || ''
    }
  } finally { loading.value = false }
}

const saveProfile = async () => {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return
  savingProfile.value = true
  try {
    const res = await request.post('/user/update-profile', { ...profileForm })
    if (res && res.code === 200) {
      ElMessage.success('资料更新成功')
      Object.assign(profile, profileForm)
      loadProfile()
    }
  } catch (e) {
    // 错误已由响应拦截器统一提示，此处静默处理，避免触发全局错误页
  } finally { savingProfile.value = false }
}

const resetProfile = () => {
  profileForm.realName = profile.realName || ''
  profileForm.email = profile.email || ''
  profileFormRef.value?.clearValidate()
}

const changePassword = async () => {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  savingPwd.value = true
  try {
    const res = await request.post('/user/change-password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    if (res && res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      setTimeout(() => {
        sessionStorage.clear()
        router.push('/login')
      }, 1500)
    }
  } catch (e) {
    // 错误已由响应拦截器统一提示（如"原密码错误"），静默处理
  } finally { savingPwd.value = false }
}

const resetPwd = () => {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdFormRef.value?.clearValidate()
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-card { text-align: center; padding: 20px 10px; }
.avatar-wrap { margin-bottom: 16px; }
.avatar {
  width: 90px; height: 90px;
  border-radius: 50%;
  margin: 0 auto;
  background: linear-gradient(135deg, #ff8a65, #ff6b6b);
  color: #fff;
  font-size: 40px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8px 24px rgba(255, 138, 101, 0.35);
}
.user-name { font-size: 22px; font-weight: 700; color: var(--text-primary); }
.user-username { font-size: 13px; color: var(--text-muted); margin: 4px 0 12px; }
.role-tag { margin-bottom: 8px; }

.meta-list { text-align: left; }
.meta-item {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 0; color: var(--text-secondary); font-size: 14px;
}
.meta-item .el-icon { color: var(--accent); }

.section-card { margin-bottom: 20px; }
.section-header {
  display: flex; align-items: center; gap: 8px;
  font-weight: 600; color: var(--text-primary);
}
.section-header .el-icon { color: var(--accent); }

.disabled-tip { font-size: 12px; color: var(--text-muted); }
</style>
