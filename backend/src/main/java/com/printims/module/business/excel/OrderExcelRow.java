package com.printims.module.business.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单 Excel 导入行（客户名称用于关联或提示）。
 */
@Data
public class OrderExcelRow {

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

    @ExcelProperty("排单号")
    private String scheduleNo;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("刀模型号")
    private String moldModel;

    @ExcelProperty("不干胶材料")
    private String material;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("是否出货")
    private String shippedText;
}
