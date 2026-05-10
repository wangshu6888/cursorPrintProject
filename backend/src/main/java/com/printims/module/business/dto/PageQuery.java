package com.printims.module.business.dto;

import lombok.Data;

/**
 * 通用分页查询参数。
 */
@Data
public class PageQuery {

    private long pageNum = 1;
    private long pageSize = 20;
}
