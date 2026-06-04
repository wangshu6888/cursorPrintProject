<template>
  <div class="apple-card">
    <h2 class="apple-page-title">操作日志</h2>
    <el-form inline @submit.prevent="load">
      <el-input v-model="keyword" placeholder="用户/操作" clearable />
      <el-button type="primary" @click="load">查询</el-button>
    </el-form>
    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column label="时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column prop="username" label="用户" width="100" />
      <el-table-column prop="operation" label="操作" />
      <el-table-column prop="ip" label="IP" width="120" />
      <el-table-column prop="status" label="成功" width="70" />
      <el-table-column prop="costTime" label="耗时ms" width="90" />
    </el-table>
    <el-pagination
      class="pager"
      v-model:current-page="pageNum"
      :page-size="20"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '@/api/http'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const rows = ref<Record<string, unknown>[]>([])
const total = ref(0)
const pageNum = ref(1)
const keyword = ref('')

async function load() {
  loading.value = true
  try {
    const r = await http.get('/system/logs', { params: { pageNum: pageNum.value, keyword: keyword.value } })
    rows.value = r.data.records
    total.value = r.data.total
  } finally {
    loading.value = false
  }
}

onMounted(load)


</script>

<style scoped lang="scss">
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
