package com.printims.module.business.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 送货单打印数据（前端排版打印）。
 */
@Data
public class DeliveryPrintVO {

    private Long customerId;
    private String customerName;
    private String deliveryNo;
    private LocalDateTime printTime;
    private List<Line> lines;

    @Data
    public static class Line {
        private String orderNo;
        private String printName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private String remark;
    }
}
