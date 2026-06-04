<template>
  <div class="apple-card">
    <h2 class="apple-page-title">客户管理</h2>
    <el-form inline @submit.prevent="load">
      <el-form-item>
        <el-input v-model="query.keyword" placeholder="名称/电话/编号" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="open()">新增</el-button>
        <el-button @click="exportFile">导出</el-button>
        <el-upload
          :show-file-list="false"
          accept=".xlsx,.xls"
          :http-request="onImport"
          class="inline-upload"
        >
          <el-button>导入</el-button>
        </el-upload>
      </el-form-item>
    </el-form>
    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="customerNo" label="客户编号" width="160" />
      <el-table-column prop="customerName" label="客户名称" />
      <el-table-column prop="contactPerson" label="联系人" width="100" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="address" label="地址" show-overflow-tooltip />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="open(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      class="pager"
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load"
    />

    <el-dialog v-model="dlg" :title="form.id ? '编辑客户' : '新增客户'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称" required>
          <el-input v-model="form.customerName" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" />
        </el-form-item>
        <el-form-item label="电话" required>
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
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
import { reactive, ref, onMounted } from 'vue'
import axios from 'axios'
import http from '@/api/http'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'

const loading = ref(false)
const rows = ref<Record<string, unknown>[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 20, keyword: '' })
const dlg = ref(false)
const form = reactive<Record<string, unknown>>({})

async function load() {
  loading.value = true
  try {
    const r = await http.get('/customers', { params: query })
    rows.value = r.data.records
    total.value = r.data.total
  } finally {
    loading.value = false
  }
}

function open(row?: Record<string, unknown>) {
  dlg.value = true
  Object.assign(form, row || { id: null, customerName: '', phone: '', contactPerson: '', address: '', email: '', remark: '' })
}

async function save() {
  if (form.id) {
    await http.put(`/customers/${form.id}`, form)
  } else {
    await http.post('/customers', form)
  }
  ElMessage.success('保存成功')
  dlg.value = false
  load()
}

async function remove(row: Record<string, unknown>) {
  await ElMessageBox.confirm('确认删除？')
  await http.delete(`/customers/${row.id}`)
  ElMessage.success('已删除')
  load()
}

async function exportFile() {
  const u = useUserStore()
  const res = await axios.get('/api/customers/export', {
    responseType: 'blob',
    headers: { Authorization: `Bearer ${u.token}` },
  })
  const url = URL.createObjectURL(res.data)
  const a = document.createElement('a')
  a.href = url
  a.download = `客户导出_${new Date().toISOString().slice(0, 16).replace('T', '_').replace(/:/g, '')}.xlsx`
  a.click()
  URL.revokeObjectURL(url)
}

async function onImport(opt: UploadRequestOptions) {
  const fd = new FormData()
  fd.append('file', opt.file as File)
  await http.post('/customers/import', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  ElMessage.success('导入完成')
  load()
}

onMounted(load)
</script>

<style scoped lang="scss">
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.inline-upload {
  display: inline-block;
  margin-left: 12px;
}
</style>
