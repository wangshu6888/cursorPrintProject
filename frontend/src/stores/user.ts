import { defineStore } from 'pinia'
import { ref } from 'vue'
import http from '@/api/http'

export interface MenuItem {
  id: number
  parentId: number
  menuName: string
  path: string
  component: string
  icon?: string
  sort?: number
  menuType?: number
  children?: MenuItem[]
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const username = ref('')
  const realName = ref('')
  const roleCodes = ref<string[]>([])
  const menus = ref<MenuItem[]>([])

  function setToken(t: string | null) {
    token.value = t
    if (t) localStorage.setItem('token', t)
    else localStorage.removeItem('token')
  }

  function clear() {
    setToken(null)
    username.value = ''
    realName.value = ''
    roleCodes.value = []
    menus.value = []
  }

  async function fetchMe() {
    const res = await http.get('/auth/me')
    const d = res.data
    username.value = d.username
    realName.value = d.realName || d.username
    roleCodes.value = d.roleCodes || []
    menus.value = d.menus || []
    return d
  }

  return { token, username, realName, roleCodes, menus, setToken, clear, fetchMe }
})
