package com.printims.module.business.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户列表查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerQuery extends PageQuery {

    private String keyword;
}
