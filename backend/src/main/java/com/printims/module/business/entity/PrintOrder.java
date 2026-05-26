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

    @jakarta.validation.constraints.NotNull(message = "数量不能为空")
    @jakarta.validation.constraints.Min(value = 1, message = "数量必须大于0")
    private Integer quantity;

    @jakarta.validation.constraints.NotNull(message = "单价不能为空")
    @jakarta.validation.constraints.DecimalMin(value = "0.0", inclusive = true, message = "单价不能为负数")
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
