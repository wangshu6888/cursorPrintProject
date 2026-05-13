<template>
  <div class="apple-card">
    <h2 class="apple-page-title">数据统计</h2>
    <el-form inline>
      <el-form-item label="开始">
        <el-date-picker v-model="range[0]" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="结束">
        <el-date-picker v-model="range[1]" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">刷新</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="16" v-loading="loading">
      <el-col :span="12">
        <div ref="chartTrend" class="chart" />
      </el-col>
      <el-col :span="12">
        <div ref="chartShip" class="chart" />
      </el-col>
      <el-col :span="12">
        <div ref="chartCust" class="chart" />
      </el-col>
      <el-col :span="12">
        <div ref="chartMold" class="chart" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, shallowRef } from 'vue'
import * as echarts from 'echarts'
import http from '@/api/http'
import { useThemeStore } from '@/stores/theme'

const theme = useThemeStore()
const loading = ref(false)
const range = ref<[string | null, string | null]>([null, null])
const chartTrend = ref<HTMLElement | null>(null)
const chartShip = ref<HTMLElement | null>(null)
const chartCust = ref<HTMLElement | null>(null)
const chartMold = ref<HTMLElement | null>(null)
const inst = shallowRef<echarts.ECharts[]>([])

const cachedData = ref<Record<string, unknown>>({})

function textColor() { return theme.mode === 'dark' ? '#F8FAFC' : '#1E293B' }
function textSecondary() { return theme.mode === 'dark' ? '#94A3B8' : '#64748B' }

function disposeCharts() {
  inst.value.forEach((c) => c.dispose())
  inst.value = []
}

async function load() {
  loading.value = true
  try {
    disposeCharts()
    const r = await http.get('/statistics/dashboard', {
      params: { start: range.value[0] || undefined, end: range.value[1] || undefined },
    })
    cachedData.value = r.data
    renderAll()
  } finally {
    loading.value = false
  }
}

function renderAll() {
  const d = cachedData.value
  renderTrend(d.orderTrend || [])
  renderShip(d.shipped || {})
  renderCust(d.topCustomersByAmount || [])
  renderMold(d.moldModelDistribution || [])
}

function onThemeChange() {
  disposeCharts()
  renderAll()
}

function renderTrend(rows: { d?: string; c?: number; a?: number }[]) {
  if (!chartTrend.value) return
  const ch = echarts.init(chartTrend.value, theme.mode === 'dark' ? 'dark' : undefined)
  inst.value.push(ch)
  ch.setOption({
    title: { text: '订单趋势', left: 0, textStyle: { fontSize: 14, color: textColor() } },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: rows.map((x) => String(x.d)), axisLabel: { color: textSecondary() } },
    yAxis: { type: 'value', axisLabel: { color: textSecondary() } },
    series: [{ name: '单量', type: 'line', smooth: true, data: rows.map((x) => x.c) }],
  })
}

function renderShip(s: Record<string, unknown>) {
  if (!chartShip.value) return
  const ch = echarts.init(chartShip.value, theme.mode === 'dark' ? 'dark' : undefined)
  inst.value.push(ch)
  ch.setOption({
    title: { text: '出货占比', left: 0, textStyle: { fontSize: 14, color: textColor() } },
    label: { color: textColor() },
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: '62%',
        data: [
          { name: '已出货', value: Number(s.shipped || 0) },
          { name: '未出货', value: Number(s.pending || 0) },
        ],
      },
    ],
  })
}

function renderCust(rows: { name?: string; amt?: number }[]) {
  if (!chartCust.value) return
  const ch = echarts.init(chartCust.value, theme.mode === 'dark' ? 'dark' : undefined)
  inst.value.push(ch)
  ch.setOption({
    title: { text: '客户销售额 TOP', left: 0, textStyle: { fontSize: 14, color: textColor() } },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: rows.map((x) => x.name), axisLabel: { rotate: 30, color: textSecondary() } },
    yAxis: { type: 'value', axisLabel: { color: textSecondary() } },
    series: [{ type: 'bar', data: rows.map((x) => x.amt) }],
  })
}

function renderMold(rows: { name?: string; cnt?: number }[]) {
  if (!chartMold.value) return
  const ch = echarts.init(chartMold.value, theme.mode === 'dark' ? 'dark' : undefined)
  inst.value.push(ch)
  ch.setOption({
    title: { text: '刀模型号分布', left: 0, textStyle: { fontSize: 14, color: textColor() } },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: rows.map((x) => x.name), axisLabel: { rotate: 40, color: textSecondary() } },
    yAxis: { type: 'value', axisLabel: { color: textSecondary() } },
    series: [{ type: 'bar', data: rows.map((x) => x.cnt) }],
  })
}

onMounted(load)
window.addEventListener('theme-change', onThemeChange)
onUnmounted(() => window.removeEventListener('theme-change', onThemeChange))
</script>

<style scoped lang="scss">
.chart {
  height: 320px;
  margin-bottom: 16px;
}
</style>
