import { onMounted, ref } from 'vue'
import http from '@/api/http'

export interface StatCard {
  label: string
  value: number
  unit: string
  icon: string
  color: string
  trend: number
}

export interface OrderTrendItem {
  d?: string
  c?: number
  a?: number
}

export interface TopItem {
  name?: string
  amt?: number
  cnt?: number
}

export interface RecentOrder {
  id: number
  orderNo: string
  customerName: string
  printName: string
  amount: number
  shipped: number
}

export function useDashboard() {
  const loading = ref(false)
  const error = ref<string | null>(null)

  const stats = ref<StatCard[]>([])
  const orderTrend = ref<OrderTrendItem[]>([])
  const shipping = ref<{ shipped: number; pending: number }>({ shipped: 0, pending: 0 })
  const topCustomers = ref<TopItem[]>([])
  const moldDistribution = ref<TopItem[]>([])
  const recentOrders = ref<RecentOrder[]>([])
  const pendingCount = ref(0)

  async function fetchDashboard() {
    loading.value = true
    error.value = null
    try {
      const [dashRes, orderRes] = await Promise.all([
        http.get('/statistics/dashboard'),
        http.get('/orders', { params: { pageNum: 1, pageSize: 5 } }),
      ])
      const d = dashRes.data

      stats.value = [
        { label: '订单数（区间）', value: d.orderTotal ?? 0, unit: '单', icon: 'Document', color: '#3B82F6', trend: 0 },
        { label: '销售额', value: d.orderAmount ?? 0, unit: '元', icon: 'Money', color: '#22C55E', trend: 0 },
        { label: '刀模总数', value: d.moldTotal ?? 0, unit: '个', icon: 'Cpu', color: '#F97316', trend: 0 },
        { label: '客户总数', value: d.customerTotal ?? 0, unit: '家', icon: 'UserFilled', color: '#D946EF', trend: 0 },
      ]
      orderTrend.value = d.orderTrend || []
      shipping.value = { shipped: d.shipped?.shipped ?? 0, pending: d.shipped?.pending ?? 0 }
      topCustomers.value = (d.topCustomersByAmount || []).slice(0, 8)
      moldDistribution.value = d.moldModelDistribution || []
      recentOrders.value = (orderRes.data?.records || []).slice(0, 5)
      pendingCount.value = d.shipped?.pending ?? 0
    } catch (e) {
      error.value = '加载仪表盘数据失败'
    } finally {
      loading.value = false
    }
  }

  onMounted(fetchDashboard)

  return { loading, error, stats, orderTrend, shipping, topCustomers, moldDistribution, recentOrders, pendingCount, refetch: fetchDashboard }
}
