# 印刷行业综合管理系统 (Print IMS)

面向印刷行业的综合管理系统，覆盖订单、客户、刀模管理与数据统计，支持送货单打印、刀模标签打印、Excel 批量导入导出。

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 3.2.5 | 应用框架 |
| Spring Security | 6.x | 安全认证 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| JWT (jjwt) | 0.12.5 | 无状态认证 |
| Knife4j | 4.5.0 | API 文档 |
| EasyExcel | 3.3.4 | Excel 导入导出 |
| ZXing | 3.5.3 | 二维码生成 |
| MySQL Connector | 8.x | 数据库驱动 |
| Lombok | - | 代码简化 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3 | 前端框架 |
| TypeScript | 类型安全 |
| Vite | 构建工具 |
| Pinia | 状态管理 |
| Element Plus | UI 组件库 |
| ECharts | 数据可视化 |
| Axios | HTTP 请求 |

### 基础设施

| 技术 | 版本 |
|------|------|
| MySQL | 8.0 |
| Docker / Docker Compose | - |

## 项目结构

```
cursorPrintProject/
├── backend/                          # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/printims/
│       ├── PrintImsApplication.java  # 启动类
│       ├── common/                   # 公共模块
│       │   ├── api/                  # 统一返回 R、PageResult
│       │   ├── annotation/           # 自定义注解 (@Log)
│       │   └── exception/            # 全局异常处理
│       ├── config/                   # 配置类
│       │   ├── MybatisPlusConfig.java
│       │   ├── OpenApiConfig.java    # Knife4j 文档配置
│       │   ├── WebMvcConfig.java
│       │   └── DataInitializer.java  # 初始化角色、菜单、管理员
│       ├── security/                 # 安全模块
│       │   ├── JwtUtil.java         # JWT 工具
│       │   ├── JwtAuthenticationFilter.java
│       │   ├── SecurityConfig.java
│       │   ├── UserDetailsServiceImpl.java
│       │   └── LoginUser.java
│       ├── aspect/                   # 切面
│       │   └── LogAspect.java       # 操作日志切面
│       ├── module/
│       │   ├── auth/                # 认证模块
│       │   │   ├── controller/AuthController.java
│       │   │   ├── service/AuthService.java
│       │   │   └── dto/             # LoginRequest, RegisterRequest, LoginResponse
│       │   ├── system/              # 系统管理模块
│       │   │   ├── entity/          # SysUser, SysRole, SysMenu, SysOperationLog
│       │   │   ├── mapper/
│       │   │   ├── service/
│       │   │   ├── controller/      # 用户/角色/菜单/日志管理
│       │   │   └── constants/RoleCodes.java
│       │   ├── business/            # 业务模块
│       │   │   ├── entity/          # PrintOrder, Customer, KnifeMold
│       │   │   ├── mapper/
│       │   │   ├── service/
│       │   │   ├── controller/      # 订单/客户/刀模管理
│       │   │   ├── dto/             # 查询条件、视图对象
│       │   │   └── excel/           # Excel 导入导出模型
│       │   └── statistics/          # 统计模块
│       │       ├── mapper/StatisticsMapper.java
│       │       ├── service/StatisticsService.java
│       │       └── controller/StatisticsController.java
│       └── util/                    # 工具类
│           ├── BizNoUtil.java       # 业务单号生成
│           └── KnifeMoldRules.java  # 刀模型号/位置编码规则
├── frontend/                         # Vue 3 前端
│   ├── src/
│   ├── package.json
│   ├── vite.config.ts
│   └── tsconfig.json
├── docker-compose.yml                # MySQL 8.0 容器配置
├── issues.md                          # 问题记录（按批次，已修复标记删除线）
├── session-summary.md                 # 会话总结（技术决策、修改文件、下一步）
├── 印刷行业综合管理系统需求文档.md
└── README.md
```

## 核心模块

### 1. 用户与权限模块
- JWT 无状态认证，Token 有效期 1440 分钟（24小时）
- RBAC 模型：用户 → 角色 → 菜单权限
- 预设角色：超级管理员、财务人员、普通员工
- 操作日志记录（通过 `@Log` 注解 + AOP 切面）
- 接口权限控制：`@PreAuthorize` 注解
- 菜单权限：前端路由与菜单根据角色动态生成

### 2. 订单管理模块
- 订单 CRUD，支持多条件分页查询
- 自动计算金额（数量 × 单价）
- 订单号、送货单号自动生成（`BizNoUtil`）
- Excel 批量导入导出（`OrderExcelRow`, `OrderExportRow`）
- 送货单打印：勾选同一客户订单生成打印数据
- 智能关联：新增订单时支持快速创建客户和刀模并回填

### 3. 客户管理模块
- 客户 CRUD，支持分页查询
- 客户编号自动生成
- Excel 批量导入导出（`CustomerExcelRow`）
- 与订单模块联动，支持快速新增

### 4. 刀模管理模块
- 刀模 CRUD，支持分页查询
- 型号自动生成规则：
  - 矩形/正方形：`长×宽`（如 `50×10`）
  - 圆形：`D直径`（如 `D100`）
  - 异型：`异型-{名称/序号}`
- 位置编码：区域-排-序号（如 `A-5-1`），支持扩展为四层（含层位）
- 标签打印：50mm×30mm 标签，含二维码（PDA 扫描用）
- 支持单条和批量打印

### 5. 数据统计模块
- Dashboard 仪表盘，ECharts 可视化
- 订单统计：总量、销售额趋势、出货率、客户排行
- 刀模统计：总量、型号分布、区域占用情况
- 客户统计：总量、增长趋势、活跃度排行
- 支持按时间范围筛选

## 快速开始

### 前置条件
- JDK 17+
- Node.js 18+ / npm
- MySQL 8.0（或 Docker）
- Maven 3.6+

### 1. 启动数据库

```bash
docker-compose up -d
```

或手动创建 MySQL 数据库 `print_ims`，字符集 `utf8mb4_unicode_ci`。

### 2. 配置后端

编辑 `backend/src/main/resources/application.yml`：

```yaml
server:
  port: 8080

spring:
  application:
    name: print-ims
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/print_ims?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai

mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true

print:
  jwt:
    secret: your-jwt-secret-key-at-least-32-chars!!
    expire-minutes: 1440
```

### 3. 初始化数据库

执行建表 SQL（参考需求文档中的完整表结构）。系统首次启动时会通过 `DataInitializer` 自动创建：
- 预设角色（SUPER_ADMIN、FINANCE、EMPLOYEE）
- 菜单数据
- 超级管理员账号 `admin`

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`

### 5. API 文档

Knife4j 接口文档：`http://localhost:8080/doc.html`

Swagger UI：`http://localhost:8080/swagger-ui.html`

### 6. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:5173`（Vite 默认端口），已配置 `/api` 代理到后端。

## API 接口概览

| 模块 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 认证 | POST | `/api/auth/login` | 登录 | 公开 |
| 认证 | POST | `/api/auth/register` | 注册 | 公开 |
| 用户 | GET | `/api/system/users` | 用户列表 | SUPER_ADMIN |
| 用户 | POST | `/api/system/users` | 新增用户 | SUPER_ADMIN |
| 角色 | GET | `/api/system/roles` | 角色列表 | SUPER_ADMIN |
| 菜单 | GET | `/api/system/menus` | 菜单树 | SUPER_ADMIN |
| 日志 | GET | `/api/system/logs` | 操作日志 | SUPER_ADMIN |
| 订单 | GET | `/api/orders` | 分页查询 | 所有角色 |
| 订单 | POST | `/api/orders` | 新增订单 | EMPLOYEE |
| 订单 | PUT | `/api/orders/{id}` | 更新订单 | EMPLOYEE |
| 订单 | DELETE | `/api/orders/{id}` | 删除订单 | SUPER_ADMIN |
| 订单 | POST | `/api/orders/export` | 导出 Excel | 所有角色 |
| 订单 | POST | `/api/orders/import` | 导入 Excel | EMPLOYEE |
| 订单 | POST | `/api/orders/print` | 打印送货单 | EMPLOYEE |
| 客户 | GET | `/api/customers` | 分页查询 | EMPLOYEE |
| 客户 | POST | `/api/customers` | 新增客户 | EMPLOYEE |
| 客户 | POST | `/api/customers/export` | 导出 Excel | EMPLOYEE |
| 客户 | POST | `/api/customers/import` | 导入 Excel | EMPLOYEE |
| 刀模 | GET | `/api/knife-molds` | 分页查询 | EMPLOYEE |
| 刀模 | POST | `/api/knife-molds` | 新增刀模 | EMPLOYEE |
| 刀模 | POST | `/api/knife-molds/print-label` | 打印标签 | EMPLOYEE |
| 统计 | GET | `/api/statistics/**` | 各类统计接口 | FINANCE/SUPER_ADMIN |

### 统一返回格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {},
  "timestamp": 1715241600000
}
```

状态码：200=成功，400=参数错误，401=未授权，403=无权限，404=不存在，500=服务器错误

### 分页请求格式

```json
{
  "pageNum": 1,
  "pageSize": 20,
  "keyword": "搜索关键词",
  "startDate": "2026-01-01",
  "endDate": "2026-05-09"
}
```

### 分页响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 5
  },
  "timestamp": 1715241600000
}
```

## 开发规范

### 后端规范
- RESTful API 设计，URL 使用小写+下划线
- 统一返回格式：`R<T>` 包装 `{ code, msg, data, timestamp }`
- 全局异常处理：`BusinessException` + `GlobalExceptionHandler`
- 分页请求统一封装：`PageQuery`，分页响应：`PageResult`
- 操作日志：自定义 `@Log` 注解 + `LogAspect` AOP 切面
- 接口权限：`@PreAuthorize("hasAnyRole('SUPER_ADMIN',...)")`
- 代码分层：controller → service → mapper，不在 controller 写业务逻辑
- DTO/VO 分离，不直接返回实体类
- SQL 关键词大写，字段名小写+下划线

### 前端规范
- 组件化开发：页面 = 布局组件 + 业务组件
- 统一请求封装：Axios 拦截器 + Token 自动携带
- 统一错误处理：网络错误、业务错误提示
- 表单统一校验规则
- 删除操作二次确认
- 页面加载状态统一处理

### 数据库规范
- 表名：系统表 `sys_` 前缀，业务表 `t_` 前缀
- 字段名：小写+下划线
- 每表必有：`id, create_time, update_time, create_by, update_by, is_deleted`
- 索引命名：`idx_表名_字段名`
- 所有字段必须有注释
- 软删除用 `is_deleted` 字段，禁止物理删除

## 预设角色

| 角色编码 | 权限范围 | 说明 |
|----------|----------|------|
| `SUPER_ADMIN` | 全部权限 | 系统配置、用户管理、所有业务模块 |
| `FINANCE` | 订单查看、数据统计 | 查看订单及金额，操作统计报表 |
| `EMPLOYEE` | 订单、刀模、客户 | 日常录单、刀模管理、客户维护 |

默认注册用户分配 `EMPLOYEE` 角色。

## 默认管理员账号

系统首次启动自动创建：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | SUPER_ADMIN |

可在 `DataInitializer` 中修改默认密码。

## 打印功能

### 送货单打印
- 格式：前端 `window.print()` 排版打印
- 规则：后端校验「仅同一客户」订单可合并打印
- 包含：客户名称、送货单号、产品明细表格、日期
- 校验：勾选不同客户订单时返回错误提示

### 刀模标签打印
- 标签尺寸：50mm × 30mm（适配热敏标签打印机）
- 内容：刀模编号、型号、位置编码、二维码、创建日期
- 二维码编码：`MOLD:{id}`，供 PDA 扫描
- 支持单个打印和批量勾选打印
- 前端用 `qrcode` 生成画布后打印

## 部署

### Docker Compose（仅 MySQL）

```bash
docker-compose up -d
```

### 后端打包

```bash
cd backend
mvn clean package -DskipTests
java -jar target/print-ims-backend-1.0.0.jar
```

### 前端打包

```bash
cd frontend
npm run build
# 产物在 dist/ 目录，可部署到 Nginx
```

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## UI 风格

采用苹果风设计：
- 大面积留白，布局简洁
- 圆角卡片式容器
- 轻盈的弥散阴影
- 清爽的浅色背景为主
- 字体清晰，层级分明
- 图标简洁统一
- 支持标签页模式（多页面同时打开）

## 数据库核心表

### 系统表
- `sys_user` - 用户表
- `sys_role` - 角色表
- `sys_menu` - 菜单表
- `sys_user_role` - 用户角色关联表
- `sys_role_menu` - 角色菜单关联表
- `sys_operation_log` - 操作日志表

### 业务表
- `t_order` - 订单表
- `t_customer` - 客户表
- `t_knife_mold` - 刀模表

完整建表 SQL 参考《印刷行业综合管理系统需求文档.md》

## License

内部项目
