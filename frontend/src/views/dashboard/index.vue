<template>
  <div class="dashboard">
    <!-- Welcome Hero -->
    <section class="dash-welcome">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1 class="welcome-greeting">{{ greeting }}, {{ u.realName || u.username }}</h1>
          <p class="welcome-date">{{ dateStr }}</p>
          <p class="welcome-pending" v-if="pendingCount > 0">
            今天有 <strong>{{ pendingCount }}</strong> 个待出货订单
          </p>
          <p class="welcome-pending" v-else>所有订单已出货 ✨</p>
        </div>
        <div class="welcome-graphic">
          <svg viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="60" cy="60" r="54" stroke="var(--color-primary)" stroke-width="2" opacity="0.15" />
            <circle cx="60" cy="60" r="38" stroke="var(--color-accent)" stroke-width="2" opacity="0.15" />
            <circle cx="60" cy="60" r="22" stroke="var(--color-secondary)" stroke-width="2" opacity="0.15" />
            <rect x="44" y="44" width="32" height="32" rx="6" stroke="var(--color-primary)" stroke-width="2" />
            <path d="M50 60h20M60 50v20" stroke="var(--color-primary)" stroke-width="2" stroke-linecap="round" />
          </svg>
        </div>
      </div>
    </section>

    <!-- Stat Cards -->
    <section class="dash-stats" v-loading="loading">
      <div v-for="s in stats" :key="s.label" class="stat-card" :style="{ '--card-accent': s.color }">
        <div class="stat-icon">
          <el-icon :size="22"><component :is="s.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-value">
            {{ formatNum(s.value) }}
            <span class="stat-unit">{{ s.unit }}</span>
          </div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </section>

    <!-- Charts Row 1: Order Trend + Shipping -->
    <section class="dash-charts">
      <div class="chart-card chart-order-trend">
        <h3 class="chart-title">订单趋势</h3>
        <div ref="chartTrend" class="chart-box" />
      </div>
      <div class="chart-card chart-ship-pie">
        <h3 class="chart-title">出货占比</h3>
        <div ref="chartShip" class="chart-box" />
      </div>
      <div class="chart-card chart-recent">
        <h3 class="chart-title">最近订单</h3>
        <el-table :data="recentOrders" size="small" stripe class="mini-table">
          <el-table-column prop="orderNo" label="单号" width="140" />
          <el-table-column prop="customerName" label="客户" width="100" />
          <el-table-column prop="amount" label="金额" width="80" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.shipped === 1 ? 'success' : 'warning'" size="small">
                {{ row.shipped === 1 ? '已出货' : '待出货' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>

    <!-- Charts Row 2: Top Customers + Mold Distribution -->
    <section class="dash-charts2">
      <div class="chart-card">
        <h3 class="chart-title">客户销售额 TOP</h3>
        <div ref="chartCust" class="chart-box" />
      </div>
      <div class="chart-card">
        <h3 class="chart-title">刀模型号分布</h3>
        <div ref="chartMold" class="chart-box" />
      </div>
    </section>

    <!-- Quick Actions -->
    <section class="dash-actions">
      <div
        v-for="a in quickActions"
        :key="a.label"
        class="action-card"
        :style="{ '--action-from': a.from, '--action-to': a.to }"
        @click="router.push(a.route)"
      >
        <el-icon :size="26"><component :is="a.icon" /></el-icon>
        <span class="action-label">{{ a.label }}</span>
        <span class="action-desc">{{ a.desc }}</span>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { useDashboard } from './composables/useDashboard'

const u = useUserStore()
const theme = useThemeStore()
const router = useRouter()

const { loading, stats, orderTrend, shipping, topCustomers, moldDistribution, recentOrders, pendingCount, refetch } = useDashboard()

const chartTrend = ref<HTMLElement | null>(null)
const chartShip = ref<HTMLElement | null>(null)
const chartCust = ref<HTMLElement | null>(null)
const chartMold = ref<HTMLElement | null>(null)
const inst = shallowRef<echarts.ECharts[]>([])

/* ── Greeting ── */
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const dateStr = computed(() => {
  const d = new Date()
  const days = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${days[d.getDay()]}`
})

function formatNum(v: number): string {
  if (v >= 10000) return (v / 10000).toFixed(1) + '万'
  return v.toLocaleString()
}

/* ── Quick Actions ── */
const quickActions = [
  { label: '新增订单', desc: '创建新的生产订单', icon: 'Plus', route: '/order', from: '#2563EB', to: '#3B82F6' },
  { label: '新增客户', desc: '添加新的客户资料', icon: 'User', route: '/customer', from: '#22C55E', to: '#4ADE80' },
  { label: '新增刀模', desc: '录入新的刀模信息', icon: 'Cpu', route: '/knife-mold', from: '#F97316', to: '#FB923C' },
  { label: '数据统计', desc: '查看详细统计分析', icon: 'DataAnalysis', route: '/statistics', from: '#7C3AED', to: '#A78BFA' },
]

/* ── Theme helpers ── */
function tc() { return theme.mode === 'dark' ? '#F8FAFC' : '#1E293B' }
function t2() { return theme.mode === 'dark' ? '#94A3B8' : '#64748B' }

/* ── Charts ── */
function disposeCharts() {
  inst.value.forEach((c) => c.dispose())
  inst.value = []
}

function initChart(el: HTMLElement | null) {
  if (!el) return null
  const ch = echarts.init(el, theme.mode === 'dark' ? 'dark' : undefined)
  inst.value.push(ch)
  return ch
}

function renderAll() {
  disposeCharts()

  const ch1 = initChart(chartTrend.value)
  if (ch1) {
    ch1.setOption({
      backgroundColor: 'transparent',
      grid: { top: 8, right: 16, bottom: 4, left: 8, containLabel: true },
      tooltip: { trigger: 'axis' },
      legend: { bottom: 0, textStyle: { color: t2(), fontSize: 12 }, itemWidth: 12, itemHeight: 8 },
      xAxis: { type: 'category', data: orderTrend.value.map((x) => String(x.d || '')), axisLabel: { color: t2(), fontSize: 11 }, axisLine: { lineStyle: { color: 'var(--border-color)' } } },
      yAxis: { type: 'value', axisLabel: { color: t2(), fontSize: 11 }, splitLine: { lineStyle: { color: 'var(--border-color)' } } },
      series: [{
        name: '单量', type: 'line', smooth: true, data: orderTrend.value.map((x) => x.c || 0),
        lineStyle: { color: '#06B6D4', width: 3 },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(6,182,212,0.25)' }, { offset: 1, color: 'rgba(6,182,212,0.01)' }]) },
        symbol: 'circle', symbolSize: 5,
        itemStyle: { color: '#06B6D4' },
      }],
    })
  }

  const ch2 = initChart(chartShip.value)
  if (ch2) {
    ch2.setOption({
      backgroundColor: 'transparent',
      grid: { top: 0, bottom: 0 },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie', radius: ['45%', '72%'], roseType: 'area',
        itemStyle: { borderRadius: 4, borderColor: 'var(--bg-surface)', borderWidth: 2 },
        label: { color: tc(), fontSize: 12 },
        data: [
          { name: '已出货', value: shipping.value.shipped, itemStyle: { color: '#22C55E' } },
          { name: '未出货', value: shipping.value.pending, itemStyle: { color: '#F59E0B' } },
        ],
      }],
    })
  }

  const ch3 = initChart(chartCust.value)
  if (ch3) {
    ch3.setOption({
      backgroundColor: 'transparent',
      grid: { top: 4, right: 16, bottom: 4, left: 8, containLabel: true },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'value', axisLabel: { color: t2(), fontSize: 11 }, splitLine: { lineStyle: { color: 'var(--border-color)' } } },
      yAxis: { type: 'category', data: topCustomers.value.map((x) => x.name), inverse: true, axisLabel: { color: tc(), fontSize: 11 }, axisLine: { show: false }, axisTick: { show: false } },
      series: [{ type: 'bar', data: topCustomers.value.map((x) => x.amt), itemStyle: { color: '#3B82F6', borderRadius: [0, 4, 4, 0] }, barWidth: 14 }],
    })
  }

  const ch4 = initChart(chartMold.value)
  if (ch4) {
    ch4.setOption({
      backgroundColor: 'transparent',
      grid: { top: 8, right: 8, bottom: 4, left: 8, containLabel: true },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: moldDistribution.value.map((x) => x.name), axisLabel: { color: t2(), fontSize: 11, rotate: 25 }, axisLine: { lineStyle: { color: 'var(--border-color)' } } },
      yAxis: { type: 'value', axisLabel: { color: t2(), fontSize: 11 }, splitLine: { lineStyle: { color: 'var(--border-color)' } } },
      series: [{ type: 'bar', data: moldDistribution.value.map((x) => x.cnt), itemStyle: { color: '#F97316', borderRadius: [4, 4, 0, 0] }, barWidth: 20, barCategoryGap: '30%' }],
    })
  }
}

function onThemeChange() {
  renderAll()
}

watch([orderTrend, shipping, topCustomers, moldDistribution], () => {
  nextTick(renderAll)
}, { deep: true })

onMounted(() => {
  window.addEventListener('theme-change', onThemeChange)
})

onUnmounted(() => {
  window.removeEventListener('theme-change', onThemeChange)
  disposeCharts()
})
</script>

<style scoped lang="scss">
.dashboard {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  max-width: 1400px;
}

/* ── Welcome Hero ── */
.dash-welcome {
  background: linear-gradient(135deg, var(--color-primary-lighter) 0%, var(--bg-surface) 40%);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: var(--space-8) var(--space-8);
  overflow: hidden;
}
.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.welcome-greeting {
  font-size: var(--text-2xl);
  font-weight: 700;
  margin: 0 0 var(--space-2);
  color: var(--text-primary);
}
.welcome-date {
  margin: 0;
  color: var(--text-secondary);
  font-size: var(--text-sm);
}
.welcome-pending {
  margin: var(--space-2) 0 0;
  color: var(--text-secondary);
  font-size: var(--text-base);
  strong { color: var(--color-accent); }
}
.welcome-graphic {
  flex-shrink: 0;
  width: 120px;
  height: 120px;
  opacity: 0.6;
}

/* ── Stat Cards ── */
.dash-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-5);
}
.stat-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  display: flex;
  gap: var(--space-4);
  align-items: flex-start;
  position: relative;
  overflow: hidden;
  transition: box-shadow var(--transition-fast), transform var(--transition-fast);
  &::before {
    content: '';
    position: absolute;
    top: 0; left: 0;
    width: 4px;
    height: 100%;
    background: var(--card-accent);
    border-radius: var(--radius-lg) 0 0 var(--radius-lg);
  }
  &:hover {
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
  }
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  background: color-mix(in srgb, var(--card-accent) 12%, transparent);
  color: var(--card-accent);
  flex-shrink: 0;
}
.stat-body { flex: 1; min-width: 0; }
.stat-value {
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--text-primary);
}
.stat-unit {
  font-size: var(--text-xs);
  font-weight: 400;
  color: var(--text-muted);
  margin-left: 2px;
}
.stat-label {
  font-size: var(--text-xs);
  color: var(--text-muted);
  margin-top: 2px;
}

/* ── Charts Grid ── */
.dash-charts {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--space-5);
}
.dash-charts2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-5);
}
.chart-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
}
.chart-order-trend {
  grid-row: 1 / 3;
}
.chart-title {
  margin: 0 0 var(--space-3);
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--text-primary);
}
.chart-box {
  height: 260px;
}
.mini-table {
  width: 100%;
  --el-table-border-color: var(--border-color);
  --el-table-header-bg-color: var(--bg-muted);
  --el-table-tr-bg-color: var(--bg-surface);
  --el-table-row-hover-bg-color: var(--bg-hover);
}

/* ── Quick Actions ── */
.dash-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-5);
}
.action-card {
  background: linear-gradient(135deg, var(--action-from), var(--action-to));
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  color: #fff;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  text-align: center;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg);
  }
}
.action-label {
  font-weight: 600;
  font-size: var(--text-base);
}
.action-desc {
  font-size: var(--text-xs);
  opacity: 0.8;
}
</style>
