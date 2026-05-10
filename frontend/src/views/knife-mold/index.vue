<template>
  <div class="apple-card">
    <h2 class="apple-page-title">刀模管理</h2>
    <el-form inline @submit.prevent="load">
      <el-form-item>
        <el-input v-model="query.keyword" placeholder="名称/编号/型号/位置" clearable />
      </el-form-item>
      <el-form-item>
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
          <el-option value="IN_STOCK" label="在库" />
          <el-option value="OUT_STOCK" label="已出库" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="open()">新增</el-button>
        <el-button :disabled="!selection.length" @click="openLabels">打印标签</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="rows" v-loading="loading" stripe @selection-change="(s: any[]) => (selection = s)">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="moldNo" label="编号" width="150" />
      <el-table-column prop="moldName" label="名称" />
      <el-table-column prop="shapeType" label="形状" width="100" />
      <el-table-column prop="model" label="型号" width="100" />
      <el-table-column prop="locationCode" label="位置" width="120" />
      <el-table-column prop="status" label="状态" width="90" />
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

    <el-dialog v-model="dlg" :title="form.id ? '编辑刀模' : '新增刀模'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="form.moldName" />
        </el-form-item>
        <el-form-item label="形状" required>
          <el-select v-model="form.shapeType" style="width: 100%">
            <el-option value="RECTANGLE" label="矩形" />
            <el-option value="SQUARE" label="正方形" />
            <el-option value="CIRCLE" label="圆形" />
            <el-option value="CUSTOM" label="异型" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.shapeType === 'RECTANGLE' || form.shapeType === 'SQUARE'" label="长×宽 mm">
          <el-input-number v-model="form.length" :min="0" /> ×
          <el-input-number v-model="form.width" :min="0" />
        </el-form-item>
        <el-form-item v-if="form.shapeType === 'CIRCLE'" label="直径 mm">
          <el-input-number v-model="form.diameter" :min="0" />
        </el-form-item>
        <el-form-item v-if="form.shapeType === 'CUSTOM'" label="型号后缀">
          <el-input v-model="form.customModelSuffix" placeholder="如 001" />
        </el-form-item>
        <el-form-item label="区域" required>
          <el-input v-model="form.areaCode" placeholder="如 A" style="width: 120px" />
        </el-form-item>
        <el-form-item label="排号" required>
          <el-input v-model="form.shelfNo" placeholder="货架/排" />
        </el-form-item>
        <el-form-item label="层号" required>
          <el-input v-model="form.layerNo" placeholder="如 01" />
        </el-form-item>
        <el-form-item label="序号" required>
          <el-input v-model="form.positionNo" placeholder="位置序号" />
        </el-form-item>
        <el-form-item label="型号(可空)">
          <el-input v-model="form.model" placeholder="留空则自动生成" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option value="IN_STOCK" label="在库" />
            <el-option value="OUT_STOCK" label="已出库" />
          </el-select>
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

    <el-dialog v-model="labDlg" title="刀模标签" width="520px" class="lab-dlg">
      <div id="lab-print">
        <div v-for="(l, i) in labels" :key="i" class="lab-item">
          <canvas :id="'qr' + i" width="96" height="96" />
          <div class="lab-meta">
            <div><b>{{ l.moldNo }}</b> {{ l.model }}</div>
            <div>{{ l.locationCode }}</div>
            <div class="small">{{ l.remark }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="labDlg = false">关闭</el-button>
        <el-button type="primary" @click="printLab">打印</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { nextTick, reactive, ref, onMounted } from 'vue'
import http from '@/api/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import QRCode from 'qrcode'

const loading = ref(false)
const rows = ref<Record<string, unknown>[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 20, keyword: '', status: '' })
const selection = ref<Record<string, unknown>[]>([])
const dlg = ref(false)
const form = reactive<Record<string, unknown>>({ shapeType: 'RECTANGLE', status: 'IN_STOCK', layerNo: '01' })
const labDlg = ref(false)
const labels = ref<Record<string, unknown>[]>([])

async function load() {
  loading.value = true
  try {
    const r = await http.get('/knife-molds', { params: query })
    rows.value = r.data.records
    total.value = r.data.total
  } finally {
    loading.value = false
  }
}

function open(row?: Record<string, unknown>) {
  dlg.value = true
  Object.assign(
    form,
    row || {
      id: null,
      moldName: '',
      shapeType: 'RECTANGLE',
      length: 0,
      width: 0,
      diameter: 0,
      areaCode: 'A',
      shelfNo: '1',
      layerNo: '01',
      positionNo: '1',
      model: '',
      status: 'IN_STOCK',
      remark: '',
      customModelSuffix: '',
    }
  )
}

async function save() {
  if (form.id) {
    await http.put(`/knife-molds/${form.id}`, form)
  } else {
    await http.post('/knife-molds', form)
  }
  ElMessage.success('保存成功')
  dlg.value = false
  load()
}

async function remove(row: Record<string, unknown>) {
  await ElMessageBox.confirm('确认删除？')
  await http.delete(`/knife-molds/${row.id}`)
  ElMessage.success('已删除')
  load()
}

async function openLabels() {
  const ids = selection.value.map((x) => x.id as number)
  const r = await http.post('/knife-molds/labels', { ids })
  labels.value = r.data
  labDlg.value = true
  await nextTick()
  labels.value.forEach((l, i) => {
    const c = document.getElementById('qr' + i) as HTMLCanvasElement
    if (c && l.qrContent) QRCode.toCanvas(c, String(l.qrContent), { width: 96, margin: 1 })
  })
}

function printLab() {
  window.print()
}

onMounted(load)
</script>

<style scoped lang="scss">
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.lab-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
}
.lab-meta .small {
  font-size: 12px;
  color: var(--apple-muted);
}
@media print {
  .lab-dlg :deep(.el-dialog__header),
  .lab-dlg :deep(.el-dialog__footer) {
    display: none !important;
  }
}
</style>
