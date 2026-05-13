import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export type ThemeMode = 'light' | 'dark'

export const useThemeStore = defineStore('theme', () => {
  const mode = ref<ThemeMode>(
    (localStorage.getItem('theme-mode') as ThemeMode) || 'light'
  )

  function applyTheme(m: ThemeMode) {
    document.documentElement.setAttribute('data-theme', m)
    localStorage.setItem('theme-mode', m)
    window.dispatchEvent(new CustomEvent('theme-change', { detail: m }))
  }

  function toggle() {
    mode.value = mode.value === 'light' ? 'dark' : 'light'
  }

  function setMode(m: ThemeMode) {
    mode.value = m
  }

  watch(mode, applyTheme, { immediate: true })

  return { mode, toggle, setMode }
})
