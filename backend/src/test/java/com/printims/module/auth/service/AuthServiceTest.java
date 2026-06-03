package com.printims.module.auth.service;

import com.printims.common.exception.BusinessException;
import com.printims.module.auth.dto.LoginRequest;
import com.printims.module.auth.dto.LoginResponse;
import com.printims.module.auth.dto.RegisterRequest;
import com.printims.module.system.constants.RoleCodes;
import com.printims.module.system.entity.SysRole;
import com.printims.module.system.entity.SysUser;
import com.printims.module.system.mapper.SysUserRoleMapper;
import com.printims.module.system.service.SysRoleService;
import com.printims.module.system.service.SysUserService;
import com.printims.security.JwtUtil;
import com.printims.security.LoginUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private SysUserService sysUserService;
    @Mock
    private SysRoleService sysRoleService;
    @Mock
    private SysUserRoleMapper sysUserRoleMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("123456");

        LoginUser loginUser = new LoginUser(1L, "admin", "123456", null);
        Authentication auth = new UsernamePasswordAuthenticationToken(loginUser, "123456");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        SysUser sysUser = new SysUser();
        sysUser.setId(1L);
        sysUser.setUsername("admin");
        sysUser.setRealName("Admin User");
        when(sysUserService.getById(1L)).thenReturn(sysUser);

        when(jwtUtil.generateToken(1L, "admin")).thenReturn("mocked-jwt-token");

        LoginResponse resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("mocked-jwt-token", resp.getToken());
        assertEquals(1L, resp.getUserId());
        assertEquals("admin", resp.getUsername());
        assertEquals("Admin User", resp.getRealName());
    }

    @Test
    void login_Failure_BadCredentials() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(req));
    }

    @Test
    void register_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("pass");
        req.setRealName("New User");

        when(sysUserService.getByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");

        // Mocking mybatis-plus lambda query is tricky, let's mock the chain
        LambdaQueryChainWrapper<SysRole> wrapper = mock(LambdaQueryChainWrapper.class);
        when(sysRoleService.lambdaQuery()).thenReturn(wrapper);
        when(wrapper.eq(any(), eq(RoleCodes.EMPLOYEE))).thenReturn(wrapper);

        SysRole empRole = new SysRole();
        empRole.setId(2L);
        when(wrapper.one()).thenReturn(empRole);

        doAnswer(invocation -> {
            SysUser u = invocation.getArgument(0);
            u.setId(10L); // simulate auto increment
            return true;
        }).when(sysUserService).save(any(SysUser.class));

        authService.register(req);

        verify(sysUserService).save(any(SysUser.class));
        verify(sysUserRoleMapper).insert(any());
    }

    @Test
    void register_Failure_DuplicateUsername() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("admin");
        req.setPassword("pass");

        when(sysUserService.getByUsername("admin")).thenReturn(new SysUser());

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(req));
        assertEquals("用户名已存在", ex.getMessage());
    }

    @Test
    void register_Failure_RoleNotFound() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("pass");

        when(sysUserService.getByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");

        LambdaQueryChainWrapper<SysRole> wrapper = mock(LambdaQueryChainWrapper.class);
        when(sysRoleService.lambdaQuery()).thenReturn(wrapper);
        when(wrapper.eq(any(), eq(RoleCodes.EMPLOYEE))).thenReturn(wrapper);
        when(wrapper.one()).thenReturn(null); // Role not found

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(req));
        assertEquals("系统未初始化角色，请联系管理员", ex.getMessage());
    }
}
