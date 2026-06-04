# 问题记录

> 按提交批次记录，每次修复为一组。已修复用 ~~删除线~~ 标记。

---

## 2026-05-11 第 1 次

### 订单管理

- [x] ~~**新增订单下单日期未自动填写当天日期**~~
  - 新增订单时下单日期字段为空，需手动选择。
  - > 前端 `order/index.vue` — `orderDate` 默认值改为当天；后端 `PrintOrderServiceImpl` — 增加 null 守卫。

- [x] ~~**送货单号标记为必填**~~
  - 送货单号后期对接打印后自动生成，不应在新增时强制必填。
  - > 前端 `order/index.vue` — 去掉 `el-form-item` 的 `required` 属性。

- [x] ~~**快速新增客户缺少空校验、创建后无法选中**~~
  - 名称和电话为空也能提交；创建后靠搜索回填不稳定。
  - > 后端 `CustomerServiceImpl` — 增加空值校验；`CustomerController` — 返回已保存实体（含 ID）。前端 `saveQuickCustomer()` — 客户端校验 + 直接用响应数据回填。

- [x] ~~**订单页面无法快速新增刀模**~~
  - "快速新增"按钮只是一个 toast 提示，未对接刀模创建。
  - > 后端 `KnifeMoldController` — 返回已保存实体。前端 `order/index.vue` — 新增 `qmDlg` 对话框（名称/形状/尺寸/位置/备注）、`saveQuickMold()` 含校验 + 自动选中。

### 刀模管理

- [x] ~~**位置序号需手动填写，无占用校验**~~
  - 序号无自动递增；同一区域/排/层下可创建重复位置。
  - > 后端 `KnifeMoldServiceImpl` — 序号为空时查同组最大序号 +1；校验 `locationCode` 唯一性，冲突报错。前端 `knife-mold/index.vue` — 序号去必填，placeholder 提示"留空则自动生成"。

---

## 2026-05-11 第 2 次

### 订单管理

- [x] ~~**下单日期改为中文日期选择**~~
  - 日期选择器显示英文格式，需改为中文年月日。
  - > 前端 `order/index.vue` — date-picker 增加 `format="YYYY年MM月DD日 HH:mm"`。

- [x] ~~**快速新增客户取消电话必填，增加联系人**~~
  - 电话非必填，需增加联系人字段。
  - > 后端 `CustomerServiceImpl` — 仅校验名称非空。前端 `order/index.vue` — 对话框增加 `contactPerson` 字段，去 phone required。

- [x] ~~**客户和刀魔非必填**~~
  - 订单表单客户和刀模应为可选。
  - > 后端 `PrintOrderServiceImpl` — customerId 为空时跳过客户查询。前端 `order/index.vue` — 去客户/数量/单价 `required` 标记。

- [x] ~~**客户/刀模搜索无法选择全部**~~
  - 下拉搜索只取 20 条，超出部分选不到。
  - > 前端 `order/index.vue` — `searchCust`/`searchMold` 的 pageSize 从 20 改为 200。

- [x] ~~**快速新增第二次替换第一次**~~
  - 新增客户B后客户A从下拉消失，刀模同理。
  - > 前端 `order/index.vue` — `saveQuickCustomer`/`saveQuickMold` 改为 push 追加，`open()` 新增时重置选项数组。

- [x] ~~**数量/单价/总价三字段联动**~~
  - 缺少总价字段，需填数量+总价算单价，填数量+单价算总价。
  - > 前端 `order/index.vue` — 新增总价字段，`calcAmount`/`calcUnitPrice` 联动函数。后端 `PrintOrderServiceImpl` — amount→unitPrice 反向计算。

### 刀模管理

- [x] ~~**新增刀模名称自动追加型号**~~
  - 保存后名称应追加形状和型号，如"测试1-矩形-10x20"。
  - > 后端 `KnifeMoldServiceImpl` — 新增时追加 `-{形状中文}-{model}` 后缀。

- [x] ~~**表格形状和状态展示英文**~~
  - shapeType 显示 RECTANGLE 而非矩形，status 显示 IN_STOCK 而非在库。
  - > 前端 `knife-mold/index.vue` — shapeType/status 列用模板映射中文标签。

- [x] ~~**打印标签格式调整**~~
  - 标签不需要编号，需展示形状+型号+位置。
  - > 后端 `KnifeLabelVO` — 新增 shapeType 字段。前端 `knife-mold/index.vue` — 标签替换 moldNo 为形状中文。

## 2026-05-11 第 3 次

### 订单管理

- [x] ~~**日期选择界面为英文、时间格式多余日字**~~
  - 日期面板显示英文月份/星期，格式中的"日"字多余。
  - > 前端 `main.ts` — 配置 Element Plus 中文语言环境。`order/index.vue` — date-picker format 改为 `YYYY-MM-DD HH:mm`。

- [x] ~~**客户/刀模下拉无法直接选择已有数据**~~
  - 下拉使用 remote 模式仅搜索时加载，聚焦时无数据。
  - > 前端 `order/index.vue` — 增加 `@focus` 触发 `loadCustList`/`loadMoldList` 预加载前200条；`searchCust`/`searchMold` 去除空查询拦截。

- [x] ~~**缺少一键出货按钮**~~
  - 订单页面无法批量标记出货，需逐条编辑。
  - > 后端 `OrderController` — 新增 `PUT /api/orders/batch-ship` 端点。前端 `order/index.vue` — 新增"一键出货"按钮含确认弹窗。

- [x] ~~**打印送货单时间显示错误**~~
  - 后端 `LocalDateTime` 序列化为数组格式，前端直接显示乱码。
  - > 前端 `order/index.vue` — 新增 `formatTime()` 函数处理数组/ISO 格式转换显示。

  
## 2026-05-11 第 4 次

- [x] ~~**检查所有时间展示格式**~~
  - 订单列表 `orderDate` 和操作日志 `createTime` 以原始 ISO 格式显示。
  - > 前端 `order/index.vue` — `orderDate` 列用 `formatTime` 模板；`system/log/index.vue` — `createTime` 列加格式化。

### 订单管理

- [x] ~~**顶部按钮间距调整**~~
  - 导入和打印送货单按钮间距不一致。
  - > 前端 `order/index.vue` — 打印送货单按钮加 `margin-left:8px`。

- [x] ~~**导出 Excel 文件名加时间**~~
  - 文件名硬编码为 `orders.xlsx`，第二次导出会覆盖。
  - > 前端 `order/index.vue` `customer/index.vue` — 文件名改为 `订单导出_20260511_1430.xlsx` 格式（含时间戳）。

### 刀模管理

- [x] ~~**型号 × 改为 \***~~
  - 刀模型号使用 Unicode × 符号，应改为 * 便于打印显示。
  - > 后端 `KnifeMoldRules.java` — `"×"` → `"*"`。前端 `knife-mold/index.vue` `order/index.vue` — 表单标签 `×` → `*`。
