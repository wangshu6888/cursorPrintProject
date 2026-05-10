-- 印刷行业综合管理系统 - 初始化数据库脚本 (MySQL 8.0)
-- 默认管理员账号由后端首次启动时创建（用户名 admin / 密码 admin123）

CREATE DATABASE IF NOT EXISTS print_ims DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE print_ims;

-- ========== 系统表 ==========

DROP TABLE IF EXISTS sys_operation_log;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_menu;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '加密密码',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
    UNIQUE KEY uk_user_username (username),
    KEY idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(50) NOT NULL,
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    component VARCHAR(200) DEFAULT NULL COMMENT '前端组件路径',
    icon VARCHAR(50) DEFAULT NULL,
    sort INT NOT NULL DEFAULT 0,
    menu_type TINYINT NOT NULL DEFAULT 0 COMMENT '0目录 1菜单 2按钮',
    permission VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_menu_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_ur_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色';

CREATE TABLE sys_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    KEY idx_rm_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单';

CREATE TABLE sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT DEFAULT NULL,
    username VARCHAR(50) DEFAULT NULL,
    operation VARCHAR(100) DEFAULT NULL,
    method VARCHAR(200) DEFAULT NULL,
    params TEXT,
    ip VARCHAR(50) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0失败 1成功',
    error_msg TEXT,
    cost_time BIGINT DEFAULT NULL COMMENT '耗时ms',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_log_create (create_time),
    KEY idx_log_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- ========== 业务表 ==========

DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_knife_mold;
DROP TABLE IF EXISTS t_customer;

CREATE TABLE t_customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_no VARCHAR(50) NOT NULL COMMENT '客户编号',
    customer_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(50) DEFAULT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(200) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    customer_level VARCHAR(20) DEFAULT NULL COMMENT 'A/B/C',
    remark TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_customer_no (customer_no),
    KEY idx_customer_name (customer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户';

CREATE TABLE t_knife_mold (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    mold_no VARCHAR(50) NOT NULL,
    mold_name VARCHAR(100) NOT NULL,
    shape_type VARCHAR(20) NOT NULL COMMENT 'RECTANGLE/CIRCLE/CUSTOM等',
    length DECIMAL(10,2) DEFAULT NULL,
    width DECIMAL(10,2) DEFAULT NULL,
    diameter DECIMAL(10,2) DEFAULT NULL,
    model VARCHAR(50) NOT NULL,
    area_code VARCHAR(10) NOT NULL,
    shelf_no VARCHAR(10) NOT NULL COMMENT '排/货架号',
    layer_no VARCHAR(10) NOT NULL DEFAULT '01' COMMENT '层，兼容简化为01',
    position_no VARCHAR(10) NOT NULL COMMENT '序号',
    location_code VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_STOCK' COMMENT 'IN_STOCK/OUT_STOCK',
    remark TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_mold_no (mold_no),
    KEY idx_mold_location (location_code),
    KEY idx_mold_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='刀模';

CREATE TABLE t_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL,
    order_date DATETIME NOT NULL,
    delivery_no VARCHAR(50) NOT NULL,
    print_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    schedule_no VARCHAR(50) DEFAULT NULL,
    material VARCHAR(100) DEFAULT NULL COMMENT '不干胶材料',
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    mold_id BIGINT DEFAULT NULL,
    mold_name VARCHAR(100) DEFAULT NULL,
    remark TEXT,
    shipped TINYINT NOT NULL DEFAULT 0 COMMENT '0未出货 1已出货',
    delivery_date DATETIME DEFAULT NULL,
    extra_info VARCHAR(200) DEFAULT NULL,
    reserve_field VARCHAR(200) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_customer (customer_id),
    KEY idx_order_date (order_date),
    KEY idx_order_shipped (shipped)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';
