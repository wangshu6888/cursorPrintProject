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

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3 + TypeScript | 前端框架 |
| Vite | 构建工具 |
| Pinia | 状态管理 |
| Element Plus | UI 组件库 |
| ECharts | 数据可视化 |

### 基础设施

| 技术 | 版本 |
|------|------|
| MySQL | 8.0 |
| Docker / Docker Compose | - |


## 核心模块

### 1. 用户与权限模块
- JWT 无状态认证（Token 有效期 24 小时）
- RBAC 模型：用户 → 角色 → 菜单权限
- 操作日志：`@Log` 注解 + AOP 切面
- 接口权限控制：`@PreAuthorize` 注解

### 2. 订单管理模块
- 订单 CRUD，支持多条件分页查询
- 自动计算金额（数量 × 单价），单号自动生成
- Excel 批量导入导出
- 送货单打印：同一客户订单可合并打印

### 3. 客户管理模块
- 客户 CRUD，编号自动生成，Excel 批量导入导出
- 与订单模块联动，支持快速新增

### 4. 刀模管理模块
- 型号自动生成（矩形：`长×宽`，圆形：`D直径`，异型：`异型-{名称}`）
- 位置编码：区域-排-序号（如 `A-5-1`）
- 标签打印：50mm×30mm，含二维码（PDA 扫描用）

### 5. 数据统计模块
- Dashboard 仪表盘，ECharts 可视化
- 订单/刀模/客户多维度统计，支持按时间范围筛选

## 快速开始

### 前置条件
- JDK 17+、Maven 3.6+
- Node.js 18+ / npm
- MySQL 8.0（或 Docker）

### 1. 启动数据库

```bash
docker-compose up -d
```

或手动创建 MySQL 数据库 `print_ims`，字符集 `utf8mb4_unicode_ci`。

### 2. 配置并启动后端

编辑 `backend/src/main/resources/application.yml` 中的数据库连接信息，然后：

```bash
cd backend
mvn spring-boot:run
```

后端默认端口 `8080`，启动后自动初始化角色、菜单和管理员账号。API 文档地址：`http://localhost:8080/doc.html`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认端口 `5173`，访问 http://localhost:5173 即可。

### 4. 默认管理员账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | SUPER_ADMIN |

## 角色权限

| 角色编码 | 权限范围 | 说明 |
|----------|----------|------|
| `SUPER_ADMIN` | 全部权限 | 系统配置、用户管理、所有业务模块 |
| `FINANCE` | 订单查看、数据统计 | 查看订单及金额，操作统计报表 |
| `EMPLOYEE` | 订单、刀模、客户 | 日常录单、刀模管理、客户维护 |

默认注册用户分配 `EMPLOYEE` 角色。

## 部署

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

## License

内部项目
