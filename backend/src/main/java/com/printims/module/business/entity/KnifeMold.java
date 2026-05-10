package com.printims.module.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.printims.module.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 刀模。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_knife_mold")
public class KnifeMold extends BaseEntity {

    private String moldNo;
    private String moldName;
    private String shapeType;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal diameter;
    private String model;
    private String areaCode;
    private String shelfNo;
    private String layerNo;
    private String positionNo;
    private String locationCode;
    private String status;
    private String remark;

    /** 异型型号自定义后缀，不落库 */
    @TableField(exist = false)
    private String customModelSuffix;
}
