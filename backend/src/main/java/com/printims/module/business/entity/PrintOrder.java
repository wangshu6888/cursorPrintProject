package com.printims.module.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.printims.module.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 印刷订单（实体名避免与 order 关键字冲突）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class PrintOrder extends BaseEntity {

    private String orderNo;
    private LocalDateTime orderDate;
    private String deliveryNo;
    private String printName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String scheduleNo;
    private String material;
    private Long customerId;
    private String customerName;
    private Long moldId;
    private String moldName;
    private String remark;
    private Integer shipped;
    private LocalDateTime deliveryDate;
    private String extraInfo;
    private String reserveField;
}
