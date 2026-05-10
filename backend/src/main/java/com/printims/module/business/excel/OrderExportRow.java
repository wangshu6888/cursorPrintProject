package com.printims.module.business.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单导出 Excel 行。
 */
@Data
public class OrderExportRow {

    @ExcelProperty("订单号")
    private String orderNo;

    @ExcelProperty("下单日期")
    private LocalDateTime orderDate;

    @ExcelProperty("送货单号")
    private String deliveryNo;

    @ExcelProperty("印刷名称")
    private String printName;

    @ExcelProperty("数量")
    private Integer quantity;

    @ExcelProperty("单价")
    private BigDecimal unitPrice;

    @ExcelProperty("金额")
    private BigDecimal amount;

    @ExcelProperty("排单号")
    private String scheduleNo;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("刀模名称")
    private String moldName;

    @ExcelProperty("不干胶材料")
    private String material;

    @ExcelProperty("是否出货")
    private String shipped;

    @ExcelProperty("送货日期")
    private LocalDateTime deliveryDate;

    @ExcelProperty("备注")
    private String remark;
}
