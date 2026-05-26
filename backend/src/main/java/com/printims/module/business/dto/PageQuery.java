package com.printims.module.business.dto;

import lombok.Data;

/**
 * 通用分页查询参数。
 */
@Data
public class PageQuery {

    @jakarta.validation.constraints.Min(value = 1, message = "页码不能小于1")
    private long pageNum = 1;

    @jakarta.validation.constraints.Min(value = 1, message = "每页大小不能小于1")
    @jakarta.validation.constraints.Max(value = 1000, message = "每页大小不能超过1000")
    private long pageSize = 20;
}
