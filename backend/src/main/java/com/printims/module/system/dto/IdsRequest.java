package com.printims.module.system.dto;

import lombok.Data;

import java.util.List;

/**
 * ID 列表请求体。
 */
@Data
public class IdsRequest {

    private List<Long> ids;
}
