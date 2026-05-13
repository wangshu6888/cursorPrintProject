<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside apple-card">
      <div class="brand">
        <span class="brand-icon">🖨</span>
        <span v-show="!collapsed" class="brand-text">印刷 IMS</span>
      </div>
      <el-menu
        :router="true"
        :default-active="active"
        :collapse="collapsed"
        background-color="transparent"
      >
        <menu-tree :items="u.menus" />
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header apple-card">
        <div class="header-left">
          <el-button text @click="collapsed = !collapsed">
            <el-icon :size="18"><Expand v-if="collapsed" /><Fold v-else /></el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="title">{{ title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-button
            :icon="theme.mode === 'dark' ? Sunny : Moon"
            circle
            @click="theme.toggle()"
            title="切换主题"
          />
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ (u.realName || u.username || '?').charAt(0) }}
              </el-avatar>
              <span class="user-name">{{ u.realName || u.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { Sunny, Moon, Expand, Fold } from '@element-plus/icons-vue'
import MenuTree from '@/layouts/MenuTree.vue'

const u = useUserStore()
const theme = useThemeStore()
const route = useRoute()
const router = useRouter()

const collapsed = ref(false)
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
  background: var(--bg-primary);
}
.aside {
  margin: var(--space-4);
  margin-right: 0;
  padding: var(--space-3) 0;
  border-radius: var(--radius-lg);
  height: calc(100vh - var(--space-8));
  overflow-y: auto;
  transition: width var(--transition-normal);
  border-right: 1px solid var(--border-color);

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: var(--border-color); border-radius: 2px; }
  &::-webkit-scrollbar-track { background: transparent; }
}
.brand {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  font-weight: 700;
  font-size: var(--text-lg);
  color: var(--color-primary);
}
.brand-icon {
  font-size: 20px;
  line-height: 1;
}
.brand-text {
  white-space: nowrap;
}
.header {
  margin: var(--space-4);
  margin-left: var(--space-2);
  margin-bottom: 0;
  height: var(--header-height) !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-5);
}
.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}
.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}
.user-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
  color: var(--text-primary);
}
.user-avatar {
  background: var(--color-primary);
}
.user-name {
  font-size: var(--text-sm);
}
.main {
  padding: 0 var(--space-6) var(--space-6) var(--space-8);
}

/* Page transition */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity var(--transition-normal), transform var(--transition-normal);
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
