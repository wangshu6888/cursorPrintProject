<template>
  <div class="apple-card">
    <h2 class="apple-page-title">菜单管理</h2>
    <el-button type="primary" @click="open()" class="mb16">新增</el-button>
    <el-table :data="flat" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="menuName" label="名称" />
      <el-table-column prop="path" label="路径" />
      <el-table-column prop="component" label="组件" />
      <el-table-column prop="menuType" label="类型" width="70" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button link type="primary" @click="open(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dlg" title="菜单" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="父级ID"><el-input-number v-model="form.parentId" :min="0" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.menuName" /></el-form-item>
        <el-form-item label="路径"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="组件"><el-input v-model="form.component" /></el-form-item>
        <el-form-item label="类型"><el-input-number v-model="form.menuType" :min="0" :max="2" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" /></el-form-item>
        <el-form-item label="权限标识"><el-input v-model="form.permission" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import http from '@/api/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref<Record<string, unknown>[]>([])
const dlg = ref(false)
const form = reactive<Record<string, unknown>>({ parentId: 0, menuType: 1, sort: 0, status: 1 })

const flat = computed(() => list.value)

async function load() {
  loading.value = true
  try {
    const r = await http.get('/system/menus')
    list.value = r.data
  } finally {
    loading.value = false
  }
}

function open(row?: Record<string, unknown>) {
  dlg.value = true
  Object.assign(
    form,
    row || {
      id: null,
      parentId: 0,
      menuName: '',
      path: '',
      component: '',
      menuType: 1,
      sort: 0,
      permission: '',
      status: 1,
    }
  )
}

async function save() {
  if (form.id) await http.put(`/system/menus/${form.id}`, form)
  else await http.post('/system/menus', form)
  ElMessage.success('保存成功')
  dlg.value = false
  load()
}

async function remove(row: Record<string, unknown>) {
  await ElMessageBox.confirm('确认删除？')
  await http.delete(`/system/menus/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped lang="scss">
.mb16 {
  margin-bottom: 16px;
}
</style>
