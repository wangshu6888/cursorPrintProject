package com.printims.module.auth.service;

import com.printims.common.exception.BusinessException;
import com.printims.module.auth.dto.LoginRequest;
import com.printims.module.auth.dto.LoginResponse;
import com.printims.module.auth.dto.RegisterRequest;
import com.printims.module.system.constants.RoleCodes;
import com.printims.module.system.entity.SysRole;
import com.printims.module.system.entity.SysUser;
import com.printims.module.system.entity.SysUserRole;
import com.printims.module.system.mapper.SysUserRoleMapper;
import com.printims.module.system.service.SysRoleService;
import com.printims.module.system.service.SysUserService;
import com.printims.security.JwtUtil;
import com.printims.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证相关业务。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        LoginUser lu = (LoginUser) auth.getPrincipal();
        SysUser u = sysUserService.getById(lu.getUserId());
        String token = jwtUtil.generateToken(lu.getUserId(), lu.getUsername());
        return new LoginResponse(token, u.getId(), u.getUsername(), u.getRealName());
    }

    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest req) {
        if (sysUserService.getByUsername(req.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        SysUser u = new SysUser();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRealName(req.getRealName());
        u.setPhone(req.getPhone());
        u.setEmail(req.getEmail());
        u.setStatus(1);
        sysUserService.save(u);

        SysRole emp = sysRoleService.lambdaQuery().eq(SysRole::getRoleCode, RoleCodes.EMPLOYEE).one();
        if (emp == null) {
            throw new BusinessException("系统未初始化角色，请联系管理员");
        }
        SysUserRole ur = new SysUserRole();
        ur.setUserId(u.getId());
        ur.setRoleId(emp.getId());
        sysUserRoleMapper.insert(ur);
    }
}
