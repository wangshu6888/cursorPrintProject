<template>
  <div class="apple-card">
    <h2 class="apple-page-title">角色管理</h2>
    <el-button type="primary" @click="open()" class="mb16">新增角色</el-button>
    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="roleName" label="名称" />
      <el-table-column prop="roleCode" label="编码" />
      <el-table-column prop="description" label="说明" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="open(row)">编辑</el-button>
          <el-button link @click="openMenus(row)">菜单</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dlg" title="角色" width="440px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.roleName" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="form.roleCode" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="menuDlg" title="分配菜单" width="480px">
      <el-tree
        ref="treeRef"
        :data="menuTree"
        show-checkbox
        node-key="id"
        :props="{ label: 'menuName', children: 'children' }"
        default-expand-all
      />
      <template #footer>
        <el-button @click="menuDlg = false">取消</el-button>
        <el-button type="primary" @click="saveMenus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import http from '@/api/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const rows = ref<Record<string, unknown>[]>([])
const dlg = ref(false)
const menuDlg = ref(false)
const form = reactive<Record<string, unknown>>({})
const menuTree = ref<Record<string, unknown>[]>([])
const treeRef = ref()
const currentRoleId = ref<number | null>(null)

async function load() {
  loading.value = true
  try {
    const r = await http.get('/system/roles', { params: { pageNum: 1, pageSize: 100 } })
    rows.value = r.data.records
  } finally {
    loading.value = false
  }
}

function open(row?: Record<string, unknown>) {
  dlg.value = true
  Object.assign(form, row || { id: null, roleName: '', roleCode: '', description: '' })
}

async function save() {
  if (form.id) await http.put(`/system/roles/${form.id}`, form)
  else await http.post('/system/roles', form)
  ElMessage.success('保存成功')
  dlg.value = false
  load()
}

async function remove(row: Record<string, unknown>) {
  await ElMessageBox.confirm('确认删除？')
  await http.delete(`/system/roles/${row.id}`)
  ElMessage.success('已删除')
  load()
}

async function openMenus(row: Record<string, unknown>) {
  currentRoleId.value = row.id as number
  const tr = await http.get('/system/menus/tree')
  menuTree.value = tr.data
  const mr = await http.get(`/system/roles/${row.id}/menus`)
  menuDlg.value = true
  setTimeout(() => treeRef.value?.setCheckedKeys(mr.data), 100)
}

async function saveMenus() {
  const keys = treeRef.value?.getCheckedKeys(false) as number[]
  await http.put(`/system/roles/${currentRoleId.value}/menus`, { ids: keys })
  ElMessage.success('已保存')
  menuDlg.value = false
}

onMounted(load)
</script>

<style scoped lang="scss">
.mb16 {
  margin-bottom: 16px;
}
</style>
