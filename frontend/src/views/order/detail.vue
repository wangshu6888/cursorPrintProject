<template>
  <div class="detail-page" v-loading="loading">
    <div class="detail-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
      <div class="header-actions">
        <el-button type="primary" @click="$router.push('/order')">到订单管理</el-button>
      </div>
    </div>

    <template v-if="order">
      <div class="detail-title">
        <h2>订单详情</h2>
        <el-tag :type="order.shipped === 1 ? 'success' : 'warning'" size="large">
          {{ order.shipped === 1 ? '已出货' : '未出货' }}
        </el-tag>
      </div>

      <!-- 基础信息 -->
      <h3 class="section-title">基础信息</h3>
      <el-descriptions :column="2" border size="large" label-class-name="auto-label">
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="送货单号">{{ order.deliveryNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="下单日期">{{ formatTime(order.orderDate) }}</el-descriptions-item>
        <el-descriptions-item label="送货日期">{{ formatTime(order.deliveryDate) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="印刷名称" :span="2">{{ order.printName }}</el-descriptions-item>
      </el-descriptions>

      <!-- 客户与刀模 -->
      <h3 class="section-title">客户与刀模</h3>
      <el-descriptions :column="2" border size="large" label-class-name="auto-label">
        <el-descriptions-item label="客户名称">{{ order.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="刀模名称">{{ order.moldName || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 价格明细 -->
      <h3 class="section-title">价格明细</h3>
      <el-descriptions :column="3" border size="large" label-class-name="auto-label">
        <el-descriptions-item label="数量">{{ order.quantity ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="单价">{{ order.unitPrice != null ? '¥' + order.unitPrice : '-' }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ order.amount != null ? '¥' + order.amount : '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 生产信息 -->
      <h3 class="section-title">生产信息</h3>
      <el-descriptions :column="2" border size="large" label-class-name="auto-label">
        <el-descriptions-item label="排单号">{{ order.scheduleNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="材料">{{ order.material || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ order.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="额外信息" :span="2">{{ order.extraInfo || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 系统信息 -->
      <h3 class="section-title">系统信息</h3>
      <el-descriptions :column="2" border size="small" label-class-name="auto-label">
        <el-descriptions-item label="创建时间">{{ formatTime(order.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(order.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="创建人ID">{{ order.createBy ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新人ID">{{ order.updateBy ?? '-' }}</el-descriptions-item>
      </el-descriptions>
    </template>

    <el-empty v-else-if="!loading" description="订单不存在" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import http from '@/api/http'

const route = useRoute()
const loading = ref(false)
const order = ref<Record<string, unknown> | null>(null)

function formatTime(v: unknown): string {
  if (!v) return ''
  if (Array.isArray(v) && v.length >= 5) {
    const [y, m, d, hh, mm] = v as number[]
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(hh).padStart(2, '0')}:${String(mm).padStart(2, '0')}`
  }
  const s = String(v)
  if (s.includes('T')) {
    return s.replace('T', ' ').substring(0, 16)
  }
  return s
}

onMounted(async () => {
  loading.value = true
  try {
    const id = route.params.id
    const r = await http.get(`/orders/${id}`)
    order.value = r.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.detail-page {
  background: var(--bg-surface);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
}
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: var(--space-4);
  margin-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-color);
}
.detail-title {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  h2 {
    margin: 0;
    font-size: var(--text-xl);
    font-weight: 600;
  }
}
.section-title {
  font-size: var(--text-base);
  font-weight: 600;
  margin: var(--space-5) 0 var(--space-3);
  color: var(--text-primary);
  padding-bottom: var(--space-1);
  border-bottom: 2px solid var(--color-primary-lighter);
}
.header-actions {
  display: flex;
  gap: var(--space-2);
}

:deep(.auto-label) {
  width: auto !important;
  white-space: nowrap;
  min-width: auto !important;
}
</style>
