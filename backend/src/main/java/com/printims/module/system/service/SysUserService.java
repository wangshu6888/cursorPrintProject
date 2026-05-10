package com.printims.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.printims.module.system.entity.SysUser;
import com.printims.security.LoginUser;

/**
 * 用户服务。
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 根据主键构造登录用户（含角色），用于 JWT 鉴权。
     */
    LoginUser loadLoginUser(Long userId);

    SysUser getByUsername(String username);
}
