<template>
  <div class="theme-switch" :class="{ 'is-dark': isDark }" @click="toggle">
    <!-- 轨道背景 -->
    <div class="track">
      <!-- 白天：太阳 + 云朵 -->
      <div class="day-scene">
        <div class="sun">
          <div class="sun-core"></div>
          <div class="sun-ray" v-for="i in 8" :key="i" :style="{ transform: `rotate(${i * 45}deg) translateY(-14px)` }"></div>
        </div>
        <div class="cloud cloud-1"></div>
        <div class="cloud cloud-2"></div>
      </div>
      <!-- 夜晚：月亮 + 星星 -->
      <div class="night-scene">
        <div class="moon">
          <div class="moon-shadow"></div>
        </div>
        <div class="star star-1"></div>
        <div class="star star-2"></div>
        <div class="star star-3"></div>
      </div>
    </div>
    <!-- 滑块 -->
    <div class="thumb">
      <div class="thumb-icon">{{ isDark ? '🌙' : '☀️' }}</div>
    </div>
  </div>
</template>

<script setup>
import { useTheme } from '../composables/useTheme'

const { isDark, toggle: doToggle } = useTheme()

const toggle = (e) => {
  // 创建波纹动画
  const ripple = document.createElement('div')
  ripple.className = 'theme-ripple'
  ripple.style.left = e.clientX + 'px'
  ripple.style.top = e.clientY + 'px'
  document.body.appendChild(ripple)

  // 执行主题切换
  doToggle()

  // 波纹动画结束后移除
  setTimeout(() => ripple.remove(), 800)
}
</script>

<style scoped>
.theme-switch {
  position: relative;
  width: 64px;
  height: 32px;
  cursor: pointer;
  user-select: none;
}

/* 轨道 */
.track {
  position: absolute;
  inset: 0;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #87CEEB 0%, #E0F6FF 50%, #FFD89B 100%);
  transition: background 0.4s ease;
}
.is-dark .track {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

/* 白天场景 */
.day-scene, .night-scene {
  position: absolute;
  inset: 0;
  transition: opacity 0.4s ease;
}
.is-dark .day-scene { opacity: 0; }
.is-dark .night-scene { opacity: 1; }
.day-scene { opacity: 1; }
.night-scene { opacity: 0; }

/* 太阳 */
.sun {
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
}
.sun-core {
  position: absolute;
  inset: 4px;
  background: #FFD700;
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(255, 215, 0, 0.6);
}
.sun-ray {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 2px;
  height: 6px;
  background: #FFD700;
  transform-origin: center 14px;
  border-radius: 1px;
}

/* 云朵 */
.cloud {
  position: absolute;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 10px;
}
.cloud-1 {
  width: 16px;
  height: 6px;
  right: 12px;
  top: 10px;
}
.cloud-2 {
  width: 12px;
  height: 5px;
  right: 18px;
  bottom: 8px;
}

/* 月亮 */
.moon {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  background: #F4E4BA;
  border-radius: 50%;
  box-shadow: 0 0 6px rgba(244, 228, 186, 0.5);
}
.moon-shadow {
  position: absolute;
  top: -2px;
  left: 4px;
  width: 14px;
  height: 14px;
  background: inherit;
  background: #16213e;
  border-radius: 50%;
}

/* 星星 */
.star {
  position: absolute;
  width: 2px;
  height: 2px;
  background: #fff;
  border-radius: 50%;
  animation: twinkle 2s ease-in-out infinite;
}
.star-1 { top: 6px; left: 12px; animation-delay: 0s; }
.star-2 { top: 14px; left: 20px; animation-delay: 0.5s; }
.star-3 { bottom: 8px; left: 16px; animation-delay: 1s; }

@keyframes twinkle {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.5); }
}

/* 滑块 */
.thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 28px;
  height: 28px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  z-index: 10;
}
.is-dark .thumb {
  transform: translateX(32px);
  background: #2a2a3e;
}
.thumb-icon {
  font-size: 16px;
  line-height: 1;
}
</style>

<style>
/* 全局波纹动画（不在 scoped 内） */
.theme-ripple {
  position: fixed;
  width: 0;
  height: 0;
  border-radius: 50%;
  pointer-events: none;
  z-index: 9999;
  transform: translate(-50%, -50%);
  animation: ripple-expand 0.8s ease-out forwards;
}
html:not(.dark) .theme-ripple {
  background: rgba(255, 255, 255, 0.6);
}
html.dark .theme-ripple {
  background: rgba(30, 30, 50, 0.6);
}
@keyframes ripple-expand {
  0% {
    width: 0;
    height: 0;
    opacity: 1;
  }
  100% {
    width: 300vmax;
    height: 300vmax;
    opacity: 0;
  }
}
</style>
