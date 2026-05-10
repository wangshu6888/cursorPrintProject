<template>
  <template v-for="m in items" :key="m.id">
    <el-sub-menu v-if="m.menuType === 0 && m.children && m.children.length" :index="m.path || String(m.id)">
      <template #title>
        <span>{{ m.menuName }}</span>
      </template>
      <menu-tree :items="m.children" />
    </el-sub-menu>
    <el-menu-item v-else-if="m.menuType === 1 && m.path" :index="m.path">
      {{ m.menuName }}
    </el-menu-item>
  </template>
</template>

<script setup lang="ts">
import type { MenuItem } from '@/stores/user'
import MenuTree from '@/layouts/MenuTree.vue'

defineProps<{ items: MenuItem[] }>()
</script>
