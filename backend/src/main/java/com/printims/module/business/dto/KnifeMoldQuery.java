package com.printims.module.business.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 刀模列表查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnifeMoldQuery extends PageQuery {

    private String keyword;
    private String status;
}
