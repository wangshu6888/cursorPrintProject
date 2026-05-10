package com.printims.security;

import com.printims.module.system.entity.SysUser;
import com.printims.module.system.mapper.SysUserMapper;
import com.printims.module.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security 登录用户加载。
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserService sysUserService;
    private final SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser u = sysUserService.getByUsername(username);
        if (u == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        if (u.getStatus() == null || u.getStatus() != 1) {
            throw new UsernameNotFoundException("账号已禁用");
        }
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(u.getId());
        return new LoginUser(u.getId(), u.getUsername(), u.getPassword(), roles);
    }
}
