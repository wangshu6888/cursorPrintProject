package com.printims.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 用户创建/更新请求。
 */
@Data
public class SysUserRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 创建时必填，更新可空表示不修改密码 */
    private String password;

    private String realName;
    private String phone;
    private String email;
    private Integer status = 1;

    /** 角色 ID 列表，创建或更新时可选 */
    private List<Long> roleIds;
}
