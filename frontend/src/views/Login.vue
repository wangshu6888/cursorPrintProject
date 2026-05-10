<template>
  <div class="login-wrap">
    <div class="login-card apple-card">
      <h1>印刷行业综合管理系统</h1>
      <p class="sub">请登录或注册账号</p>
      <el-tabs v-model="tab">
        <el-tab-pane label="登录" name="in">
          <el-form :model="loginForm" @submit.prevent="onLogin">
            <el-form-item>
              <el-input v-model="loginForm.username" placeholder="用户名" size="large" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="loginForm.password" type="password" placeholder="密码" size="large" show-password />
            </el-form-item>
            <el-button type="primary" size="large" class="w100" native-type="submit" :loading="loading">登录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="up">
          <el-form :model="regForm" @submit.prevent="onReg">
            <el-form-item>
              <el-input v-model="regForm.username" placeholder="用户名" size="large" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="regForm.password" type="password" placeholder="密码" size="large" show-password />
            </el-form-item>
            <el-form-item>
              <el-input v-model="regForm.realName" placeholder="姓名（可选）" size="large" />
            </el-form-item>
            <el-button type="primary" size="large" class="w100" native-type="submit" :loading="loading">注册</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <p class="hint">默认管理员：admin / admin123（首次启动数据库后自动创建）</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const u = useUserStore()
const tab = ref('in')
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', realName: '' })

async function onLogin() {
  loading.value = true
  try {
    const r = await http.post('/auth/login', loginForm)
    u.setToken(r.data.token)
    await u.fetchMe()
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}

async function onReg() {
  loading.value = true
  try {
    await http.post('/auth/register', regForm)
    ElMessage.success('注册成功，请登录')
    tab.value = 'in'
    loginForm.username = regForm.username
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-wrap {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.login-card {
  width: 100%;
  max-width: 400px;
  h1 {
    font-size: 1.35rem;
    font-weight: 600;
    margin: 0 0 8px;
  }
  .sub {
    color: var(--apple-muted);
    margin: 0 0 20px;
    font-size: 14px;
  }
  .w100 {
    width: 100%;
  }
  .hint {
    margin-top: 16px;
    font-size: 12px;
    color: var(--apple-muted);
  }
}
</style>
