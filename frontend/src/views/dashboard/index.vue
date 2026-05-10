<template>
  <div class="apple-card">
    <h2 class="apple-page-title">工作台</h2>
    <el-row :gutter="16" v-loading="loading">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <div class="stat">
          <div class="label">{{ c.label }}</div>
          <div class="val">{{ c.value }}</div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '@/api/http'

const loading = ref(false)
const cards = ref<{ label: string; value: string }[]>([])

onMounted(async () => {
  loading.value = true
  try {
    const r = await http.get('/statistics/dashboard')
    const d = r.data
    cards.value = [
      { label: '订单数（区间）', value: String(d.orderTotal ?? 0) },
      { label: '销售额', value: String(d.orderAmount ?? 0) },
      { label: '刀模总数', value: String(d.moldTotal ?? 0) },
      { label: '客户总数', value: String(d.customerTotal ?? 0) },
    ]
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.stat {
  padding: 16px;
  background: #fafafa;
  border-radius: 12px;
  margin-bottom: 8px;
}
.label {
  font-size: 13px;
  color: var(--apple-muted);
}
.val {
  font-size: 22px;
  font-weight: 600;
  margin-top: 8px;
}
</style>
