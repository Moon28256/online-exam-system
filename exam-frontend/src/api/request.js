import axios from 'axios'
import { ElMessage } from 'element-plus'

// baseURL 通过环境变量配置：
//  - 开发环境 (.env.development): VITE_API_BASE=http://localhost:8080
//  - 生产构建 (.env.production): 留空 = 同源（前端与后端同端口部署时）
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 10000
})

// 请求拦截器 — 自动带 Token
request.interceptors.request.use(
  config => {
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器 — 统一错误提示（不 reject，避免触发全局错误页）
request.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code && data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
    }
    return data
  },
  error => {
    let code = 500
    let msg = '网络异常，请稍后重试'
    if (error.response) {
      code = error.response.status
      if (code === 401) {
        ElMessage.error('请先登录')
        sessionStorage.removeItem('token')
        window.location.href = '/#/login'
        return { code: 401, message: '请先登录', data: null }
      }
      msg = (error.response.data && error.response.data.message) || msg
    }
    ElMessage.error(msg)
    return { code, message: msg, data: null }
  }
)

export default request
