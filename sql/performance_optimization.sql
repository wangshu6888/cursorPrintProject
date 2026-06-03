-- 优化大数据量查询性能的索引

-- t_order 复合索引，用于统计过滤
ALTER TABLE t_order ADD INDEX idx_is_deleted_order_date(is_deleted, order_date);

-- t_order 复合索引，用于通过客户查询关联
ALTER TABLE t_order ADD INDEX idx_customer_is_deleted(customer_id, is_deleted);

-- t_knife_mold 复合索引，用于按刀模模型分组统计
ALTER TABLE t_knife_mold ADD INDEX idx_is_deleted_model(is_deleted, model);
