import { ref, watch } from 'vue'

const isDark = ref(localStorage.getItem('theme') === 'dark')

watch(isDark, (val) => {
  localStorage.setItem('theme', val ? 'dark' : 'light')
  document.documentElement.classList.toggle('dark', val)
}, { immediate: true })

// 页面加载时立即应用
if (localStorage.getItem('theme') === 'dark') {
  document.documentElement.classList.add('dark')
}

export function useTheme() {
  const toggle = () => { isDark.value = !isDark.value }
  return { isDark, toggle }
}
