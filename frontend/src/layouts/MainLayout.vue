<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside apple-card">
      <div class="brand">印刷 IMS</div>
      <el-menu :router="true" :default-active="active" background-color="transparent">
        <menu-tree :items="u.menus" />
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header apple-card">
        <span class="crumb">{{ title }}</span>
        <div class="right">
          <span class="name">{{ u.realName || u.username }}</span>
          <el-button link type="danger" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import MenuTree from '@/layouts/MenuTree.vue'

const u = useUserStore()
const route = useRoute()
const router = useRouter()

const active = computed(() => route.path)
const title = computed(() => (route.meta.title as string) || '')

function logout() {
  u.clear()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.layout {
  min-height: 100%;
  background: var(--apple-bg);
}
.aside {
  margin: 16px;
  margin-right: 0;
  padding: 16px 8px;
  border-radius: var(--apple-radius);
  height: calc(100vh - 32px);
  overflow-y: auto;
}
.brand {
  font-weight: 600;
  padding: 8px 16px 16px;
  font-size: 15px;
}
.header {
  margin: 16px;
  margin-left: 8px;
  height: 56px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.crumb {
  font-weight: 500;
}
.right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.name {
  color: var(--apple-muted);
  font-size: 14px;
}
.main {
  padding: 0 16px 24px 24px;
}
</style>
