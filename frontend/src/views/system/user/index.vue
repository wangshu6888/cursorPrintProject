<template>
  <div class="apple-card">
    <h2 class="apple-page-title">用户管理</h2>
    <el-form inline @submit.prevent="load">
      <el-input v-model="keyword" placeholder="用户名/姓名" clearable />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="open()">新增</el-button>
    </el-form>
    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="phone" label="手机" />
      <el-table-column prop="status" label="状态" width="80" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="open(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      class="pager"
      v-model:current-page="pageNum"
      :page-size="20"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load"
    />
    <el-dialog v-model="dlg" :title="form.id ? '编辑用户' : '新增用户'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item :label="form.id ? '新密码' : '密码'"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
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
const total = ref(0)
const pageNum = ref(1)
const keyword = ref('')
const dlg = ref(false)
const form = reactive<Record<string, unknown>>({ roleIds: [] })
const roles = ref<{ id: number; roleName: string }[]>([])

async function load() {
  loading.value = true
  try {
    const r = await http.get('/system/users', { params: { pageNum: pageNum.value, keyword: keyword.value } })
    rows.value = r.data.records
    total.value = r.data.total
    const rr = await http.get('/system/roles/all')
    roles.value = rr.data
  } finally {
    loading.value = false
  }
}

async function open(row?: Record<string, unknown>) {
  dlg.value = true
  Object.assign(form, row || { id: null, username: '', password: '', realName: '', roleIds: [] })
  if (row?.id) {
    const r = await http.get(`/system/users/${row.id}/roles`)
    form.roleIds = r.data
  }
}

async function save() {
  if (form.id) {
    await http.put(`/system/users/${form.id}`, form)
  } else {
    await http.post('/system/users', form)
  }
  ElMessage.success('保存成功')
  dlg.value = false
  load()
}

async function remove(row: Record<string, unknown>) {
  await ElMessageBox.confirm('确认删除？')
  await http.delete(`/system/users/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped lang="scss">
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
