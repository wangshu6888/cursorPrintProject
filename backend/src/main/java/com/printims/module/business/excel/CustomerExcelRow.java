package com.printims.module.business.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 客户 Excel 行映射。
 */
@Data
public class CustomerExcelRow {

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("联系人")
    private String contactPerson;

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("地址")
    private String address;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("备注")
    private String remark;
}
