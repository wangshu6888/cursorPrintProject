<template>
  <template v-for="m in items" :key="m.id">
    <el-sub-menu v-if="m.menuType === 0 && m.children && m.children.length" :index="m.path || String(m.id)">
      <template #title>
        <el-icon v-if="m.icon"><component :is="m.icon" /></el-icon>
        <span v-else class="menu-dot" />
        <span>{{ m.menuName }}</span>
      </template>
      <menu-tree :items="m.children" />
    </el-sub-menu>
    <el-menu-item v-else-if="m.menuType === 1 && m.path" :index="m.path">
      <el-icon v-if="m.icon"><component :is="m.icon" /></el-icon>
      <span v-else class="menu-dot" />
      <span>{{ m.menuName }}</span>
    </el-menu-item>
  </template>
</template>

<script setup lang="ts">
import type { MenuItem } from '@/stores/user'
import MenuTree from '@/layouts/MenuTree.vue'

defineProps<{ items: MenuItem[] }>()
</script>

<style scoped lang="scss">
.menu-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--text-muted);
  margin-right: 4px;
  flex-shrink: 0;
}
</style>
