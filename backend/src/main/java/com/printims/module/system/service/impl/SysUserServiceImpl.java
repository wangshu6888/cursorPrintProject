package com.printims.module.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.printims.module.system.entity.SysUser;
import com.printims.module.system.mapper.SysUserMapper;
import com.printims.module.system.service.SysUserService;
import com.printims.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现。
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public LoginUser loadLoginUser(Long userId) {
        SysUser u = getById(userId);
        if (u == null || u.getStatus() == null || u.getStatus() != 1) {
            return null;
        }
        List<String> roles = baseMapper.selectRoleCodesByUserId(userId);
        return new LoginUser(u.getId(), u.getUsername(), "", roles);
    }

    @Override
    public SysUser getByUsername(String username) {
        return lambdaQuery().eq(SysUser::getUsername, username).one();
    }
}
