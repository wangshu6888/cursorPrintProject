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
        <el-button v-if="canWrite" type="success" :disabled="!selection.length" @click="openPrint" style="margin-left:8px">打印送货单</el-button>
        <el-button v-if="canWrite" type="warning" :disabled="!selection.length" @click="batchShip">一键出货</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="rows" v-loading="loading" stripe @selection-change="(s: any[]) => (selection = s)">
      <el-table-column v-if="canWrite" type="selection" width="48" />
      <el-table-column prop="orderNo" label="订单号" width="170" />
      <el-table-column label="下单日期" width="170">
        <template #default="{ row }">{{ formatTime(row.orderDate) }}</template>
      </el-table-column>
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
          <el-date-picker v-model="form.orderDate" type="datetime" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="送货单号">
          <el-input v-model="form.deliveryNo" />
        </el-form-item>
        <el-form-item label="印刷名称" required>
          <el-input v-model="form.printName" />
        </el-form-item>
        <el-form-item label="客户">
          <el-select v-model="form.customerId" filterable remote :remote-method="searchCust" @focus="loadCustList" placeholder="选择客户" style="width: 100%">
            <el-option v-for="c in custOpts" :key="c.id" :label="c.customerName" :value="c.id" />
          </el-select>
          <el-button v-if="canWrite" class="ml8" @click="quickCustomer">快速新增</el-button>
        </el-form-item>
        <el-form-item label="刀模">
          <el-select v-model="form.moldId" clearable filterable remote :remote-method="searchMold" @focus="loadMoldList" placeholder="可选" style="width: 100%">
            <el-option v-for="m in moldOpts" :key="m.id" :label="m.moldName + ' / ' + m.model" :value="m.id" />
          </el-select>
          <el-button v-if="canWrite" class="ml8" @click="quickMold">快速新增</el-button>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.quantity" :min="0" @change="calcAmount" />
        </el-form-item>
        <el-form-item label="单价">
          <el-input-number v-model="form.unitPrice" :min="0" :precision="2" @change="calcAmount" />
        </el-form-item>
        <el-form-item label="总价">
          <el-input-number v-model="form.amount" :min="0" :precision="2" @change="calcUnitPrice" />
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
        <p>送货单号：{{ printData.deliveryNo }} &nbsp; 打印时间：{{ formatTime(printData.printTime) }}</p>
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
        <el-form-item label="电话"><el-input v-model="qc.phone" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="qc.contactPerson" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="qcDlg = false">取消</el-button>
        <el-button type="primary" @click="saveQuickCustomer">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="qmDlg" title="快速新增刀模" width="480px" destroy-on-close>
      <el-form :model="qm" label-width="80px">
        <el-form-item label="名称" required><el-input v-model="qm.moldName" /></el-form-item>
        <el-form-item label="形状" required>
          <el-select v-model="qm.shapeType" style="width: 100%">
            <el-option value="RECTANGLE" label="矩形" />
            <el-option value="SQUARE" label="正方形" />
            <el-option value="CIRCLE" label="圆形" />
            <el-option value="CUSTOM" label="异型" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="qm.shapeType === 'RECTANGLE' || qm.shapeType === 'SQUARE'" label="长*宽 mm">
          <el-input-number v-model="qm.length" :min="0" /> ×
          <el-input-number v-model="qm.width" :min="0" />
        </el-form-item>
        <el-form-item v-if="qm.shapeType === 'CIRCLE'" label="直径 mm">
          <el-input-number v-model="qm.diameter" :min="0" />
        </el-form-item>
        <el-form-item label="区域"><el-input v-model="qm.areaCode" /></el-form-item>
        <el-form-item label="排号"><el-input v-model="qm.shelfNo" /></el-form-item>
        <el-form-item label="层号"><el-input v-model="qm.layerNo" /></el-form-item>
        <el-form-item label="序号"><el-input v-model="qm.positionNo" placeholder="留空自动生成" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="qm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="qmDlg = false">取消</el-button>
        <el-button type="primary" @click="saveQuickMold">保存</el-button>
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
const form = reactive<Record<string, unknown>>({ shipped: 0 })
const custOpts = ref<{ id: number; customerName: string }[]>([])
const moldOpts = ref<{ id: number; moldName: string; model: string }[]>([])
const printDlg = ref(false)
const printData = ref<{ customerName: string; deliveryNo: string; printTime: string; lines: Record<string, unknown>[] } | null>(null)
const qcDlg = ref(false)
const qc = reactive({ customerName: '', phone: '', contactPerson: '' })
const qmDlg = ref(false)
const qm = reactive({ moldName: '', shapeType: 'RECTANGLE', length: 0, width: 0, diameter: 0, areaCode: 'A', shelfNo: '1', layerNo: '01', positionNo: '', remark: '' })

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

async function loadCustList() {
  const r = await http.get('/customers', { params: { pageNum: 1, pageSize: 200 } })
  custOpts.value = r.data.records
}

async function loadMoldList() {
  const r = await http.get('/knife-molds', { params: { pageNum: 1, pageSize: 200 } })
  moldOpts.value = r.data.records
}

async function searchCust(q: string) {
  const r = await http.get('/customers', { params: { keyword: q, pageNum: 1, pageSize: 200 } })
  custOpts.value = r.data.records
}

async function searchMold(q: string) {
  const r = await http.get('/knife-molds', { params: { keyword: q, pageNum: 1, pageSize: 200 } })
  moldOpts.value = r.data.records
}

function open(row?: Record<string, unknown>) {
  dlg.value = true
  Object.assign(
    form,
    row || {
      id: null,
      orderDate: new Date().toISOString().slice(0, 19),
      deliveryNo: '',
      printName: '',
      customerId: undefined,
      moldId: undefined,
      quantity: undefined,
      unitPrice: undefined,
      amount: undefined,
      material: '',
      scheduleNo: '',
      shipped: 0,
      remark: '',
    }
  )
  if (row) {
    if (row.customerId) {
      custOpts.value = [{ id: row.customerId as number, customerName: row.customerName as string }]
    }
    if (row.moldId) {
      moldOpts.value = [{ id: row.moldId as number, moldName: row.moldName as string, model: row.model as string }]
    }
  } else {
    custOpts.value = []
    moldOpts.value = []
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
  a.download = `订单导出_${new Date().toISOString().slice(0, 16).replace('T', '_').replace(/:/g, '')}.xlsx`
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
  qc.contactPerson = ''
  qcDlg.value = true
}

async function saveQuickCustomer() {
  if (!qc.customerName.trim()) {
    ElMessage.warning('请填写客户名称')
    return
  }
  const r = await http.post('/customers', { customerName: qc.customerName, phone: qc.phone, contactPerson: qc.contactPerson })
  ElMessage.success('客户已创建')
  qcDlg.value = false
  if (r.data) {
    form.customerId = r.data.id
    custOpts.value.push({ id: r.data.id, customerName: r.data.customerName })
  }
}

function quickMold() {
  Object.assign(qm, { moldName: '', shapeType: 'RECTANGLE', length: 0, width: 0, diameter: 0, areaCode: 'A', shelfNo: '1', layerNo: '01', positionNo: '', remark: '' })
  qmDlg.value = true
}

async function saveQuickMold() {
  if (!qm.moldName.trim()) {
    ElMessage.warning('请填写刀模名称')
    return
  }
  if (qm.shapeType === 'RECTANGLE' || qm.shapeType === 'SQUARE') {
    if (!qm.length || !qm.width) {
      ElMessage.warning('矩形/正方形刀模需填写长与宽')
      return
    }
  } else if (qm.shapeType === 'CIRCLE') {
    if (!qm.diameter) {
      ElMessage.warning('圆形刀模需填写直径')
      return
    }
  }
  const r = await http.post('/knife-molds', qm)
  ElMessage.success('刀模已创建')
  qmDlg.value = false
  if (r.data) {
    form.moldId = r.data.id
    moldOpts.value.push({ id: r.data.id, moldName: r.data.moldName, model: r.data.model as string })
  }
}

function calcAmount() {
  const q = Number(form.quantity)
  const up = Number(form.unitPrice)
  if (q > 0 && up > 0) {
    form.amount = Math.round(q * up * 100) / 100
  }
}

function calcUnitPrice() {
  const q = Number(form.quantity)
  const amt = Number(form.amount)
  if (q > 0 && amt > 0) {
    form.unitPrice = Math.round((amt / q) * 100) / 100
  }
}

async function batchShip() {
  if (!selection.value.length) return
  try {
    await ElMessageBox.confirm(`确认将 ${selection.value.length} 条订单标记为已出货？`)
  } catch {
    return
  }
  const ids = selection.value.map((x) => x.id as number)
  await http.put('/orders/batch-ship', { ids })
  ElMessage.success('已出货')
  load()
}

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
    border: 1px solid var(--border-color);
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
