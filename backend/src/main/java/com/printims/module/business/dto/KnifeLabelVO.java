package com.printims.module.business.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 刀模标签打印数据。
 */
@Data
public class KnifeLabelVO {

    private Long id;
    private String moldNo;
    private String model;
    private String locationCode;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal diameter;
    private String remark;
    private String qrContent;
}
