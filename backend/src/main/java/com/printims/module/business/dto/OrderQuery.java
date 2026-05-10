package com.printims.module.business.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 订单列表查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderQuery extends PageQuery {

    private String keyword;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer shipped;
}
