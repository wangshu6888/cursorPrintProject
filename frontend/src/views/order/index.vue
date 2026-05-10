<template>
  <div class="apple-card">
    <h2 class="apple-page-title">订单管理</h2>
    <el-form inline @submit.prevent="load">
      <el-form-item>
        <el-input v-model="query.keyword" placeholder="订单号/客户/品名" clearable />
      </el-form-item>
      <el-form-item>
        <el-select v-model="query.shipped" placeholder="出货" clearable style="width: 100px">
          <el-option :value="0" label="未出货" />
          <el-option :value="1" label="已出货" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button v-if="canWrite" @click="open()">新增</el-button>
        <el-button @click="exportFile">导出</el-button>
        <el-upload v-if="canWrite" :show-file-list="false" accept=".xlsx,.xls" :http-request="onImport" class="inline-upload">
          <el-button>导入</el-button>
        </el-upload>
        <el-button v-if="canWrite" type="success" :disabled="!selection.length" @click="openPrint">打印送货单</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="rows" v-loading="loading" stripe @selection-change="(s: any[]) => (selection = s)">
      <el-table-column v-if="canWrite" type="selection" width="48" />
      <el-table-column prop="orderNo" label="订单号" width="170" />
      <el-table-column prop="orderDate" label="下单日期" width="170" />
      <el-table-column prop="deliveryNo" label="送货单号" width="120" />
      <el-table-column prop="customerName" label="客户" width="120" />
      <el-table-column prop="printName" label="印刷名称" show-overflow-tooltip />
      <el-table-column prop="quantity" label="数量" width="70" />
      <el-table-column prop="amount" label="金额" width="90" />
      <el-table-column prop="shipped" label="出货" width="80">
        <template #default="{ row }">{{ row.shipped === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column v-if="canWrite" label="操作" width="140" fixed="right">
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

    <el-dialog v-model="dlg" :title="form.id ? '编辑订单' : '新增订单'" width="640px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="下单日期" required>
          <el-date-picker v-model="form.orderDate" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="送货单号" required>
          <el-input v-model="form.deliveryNo" />
        </el-form-item>
        <el-form-item label="印刷名称" required>
          <el-input v-model="form.printName" />
        </el-form-item>
        <el-form-item label="客户" required>
          <el-select v-model="form.customerId" filterable remote :remote-method="searchCust" placeholder="选择客户" style="width: 100%">
            <el-option v-for="c in custOpts" :key="c.id" :label="c.customerName" :value="c.id" />
          </el-select>
          <el-button v-if="canWrite" class="ml8" @click="quickCustomer">快速新增</el-button>
        </el-form-item>
        <el-form-item label="刀模">
          <el-select v-model="form.moldId" clearable filterable remote :remote-method="searchMold" placeholder="可选" style="width: 100%">
            <el-option v-for="m in moldOpts" :key="m.id" :label="m.moldName + ' / ' + m.model" :value="m.id" />
          </el-select>
          <el-button v-if="canWrite" class="ml8" @click="quickMold">快速新增</el-button>
        </el-form-item>
        <el-form-item label="数量" required>
          <el-input-number v-model="form.quantity" :min="1" />
        </el-form-item>
        <el-form-item label="单价" required>
          <el-input-number v-model="form.unitPrice" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="材料">
          <el-input v-model="form.material" />
        </el-form-item>
        <el-form-item label="排单号">
          <el-input v-model="form.scheduleNo" />
        </el-form-item>
        <el-form-item label="出货">
          <el-switch v-model="form.shipped" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="saveOrder">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="printDlg" title="送货单" width="720px" class="print-dlg">
      <div id="print-area" v-if="printData">
        <h3>送货单 — {{ printData.customerName }}</h3>
        <p>送货单号：{{ printData.deliveryNo }} &nbsp; 打印时间：{{ printData.printTime }}</p>
        <table class="ptable">
          <thead>
            <tr>
              <th>订单号</th>
              <th>品名</th>
              <th>数量</th>
              <th>单价</th>
              <th>金额</th>
              <th>备注</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(l, i) in printData.lines" :key="i">
              <td>{{ l.orderNo }}</td>
              <td>{{ l.printName }}</td>
              <td>{{ l.quantity }}</td>
              <td>{{ l.unitPrice }}</td>
              <td>{{ l.amount }}</td>
              <td>{{ l.remark }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <template #footer>
        <el-button @click="printDlg = false">关闭</el-button>
        <el-button type="primary" @click="doPrint">打印</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="qcDlg" title="快速新增客户" width="480px">
      <el-form :model="qc" label-width="80px">
        <el-form-item label="名称" required><el-input v-model="qc.customerName" /></el-form-item>
        <el-form-item label="电话" required><el-input v-model="qc.phone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="qcDlg = false">取消</el-button>
        <el-button type="primary" @click="saveQuickCustomer">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue'
import axios from 'axios'
import http from '@/api/http'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'

const u = useUserStore()
const canWrite = computed(
  () => u.roleCodes.includes('SUPER_ADMIN') || u.roleCodes.includes('EMPLOYEE')
)

const loading = ref(false)
const rows = ref<Record<string, unknown>[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 20, keyword: '', shipped: undefined as number | undefined })
const selection = ref<Record<string, unknown>[]>([])
const dlg = ref(false)
const form = reactive<Record<string, unknown>>({ shipped: 0, quantity: 1, unitPrice: 0 })
const custOpts = ref<{ id: number; customerName: string }[]>([])
const moldOpts = ref<{ id: number; moldName: string; model: string }[]>([])
const printDlg = ref(false)
const printData = ref<Record<string, unknown> | null>(null)
const qcDlg = ref(false)
const qc = reactive({ customerName: '', phone: '' })

async function load() {
  loading.value = true
  try {
    const r = await http.get('/orders', { params: query })
    rows.value = r.data.records
    total.value = r.data.total
  } finally {
    loading.value = false
  }
}

async function searchCust(q: string) {
  if (!q) return
  const r = await http.get('/customers', { params: { keyword: q, pageNum: 1, pageSize: 20 } })
  custOpts.value = r.data.records
}

async function searchMold(q: string) {
  if (!q) return
  const r = await http.get('/knife-molds', { params: { keyword: q, pageNum: 1, pageSize: 20 } })
  moldOpts.value = r.data.records
}

function open(row?: Record<string, unknown>) {
  dlg.value = true
  Object.assign(
    form,
    row || {
      id: null,
      orderDate: '',
      deliveryNo: '',
      printName: '',
      customerId: undefined,
      moldId: undefined,
      quantity: 1,
      unitPrice: 0,
      material: '',
      scheduleNo: '',
      shipped: 0,
      remark: '',
    }
  )
  if (row?.customerId) {
    custOpts.value = [{ id: row.customerId as number, customerName: row.customerName as string }]
  }
}

async function saveOrder() {
  if (form.id) {
    await http.put(`/orders/${form.id}`, form)
  } else {
    await http.post('/orders', form)
  }
  ElMessage.success('保存成功')
  dlg.value = false
  load()
}

async function remove(row: Record<string, unknown>) {
  await ElMessageBox.confirm('确认删除？')
  await http.delete(`/orders/${row.id}`)
  ElMessage.success('已删除')
  load()
}

async function exportFile() {
  const res = await axios.get('/api/orders/export', {
    params: query,
    responseType: 'blob',
    headers: { Authorization: `Bearer ${u.token}` },
  })
  const url = URL.createObjectURL(res.data)
  const a = document.createElement('a')
  a.href = url
  a.download = 'orders.xlsx'
  a.click()
  URL.revokeObjectURL(url)
}

async function onImport(opt: UploadRequestOptions) {
  const fd = new FormData()
  fd.append('file', opt.file as File)
  await http.post('/orders/import', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  ElMessage.success('导入完成')
  load()
}

async function openPrint() {
  const ids = selection.value.map((x) => x.id as number)
  const r = await http.post('/orders/print', { ids })
  printData.value = r.data
  printDlg.value = true
}

function doPrint() {
  window.print()
}

function quickCustomer() {
  qc.customerName = ''
  qc.phone = ''
  qcDlg.value = true
}

async function saveQuickCustomer() {
  await http.post('/customers', { customerName: qc.customerName, phone: qc.phone })
  ElMessage.success('客户已创建')
  qcDlg.value = false
  await searchCust(qc.customerName)
  const last = custOpts.value[0]
  if (last) form.customerId = last.id
}

function quickMold() {
  ElMessage.info('请前往刀模管理新增后在此搜索选择')
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
.ml8 {
  margin-left: 8px;
}
.ptable {
  width: 100%;
  border-collapse: collapse;
  th,
  td {
    border: 1px solid #ddd;
    padding: 8px;
    font-size: 13px;
  }
}
@media print {
  .print-dlg :deep(.el-dialog__header),
  .print-dlg :deep(.el-dialog__footer) {
    display: none !important;
  }
  .print-dlg :deep(.el-dialog__body) {
    padding: 0 !important;
  }
}
</style>
