package com.printims.module.auth.controller;

import com.printims.common.annotation.Log;
import com.printims.common.api.R;
import com.printims.module.auth.dto.LoginRequest;
import com.printims.module.auth.dto.LoginResponse;
import com.printims.module.auth.dto.RegisterRequest;
import com.printims.module.auth.service.AuthService;
import com.printims.module.system.dto.MenuTreeVO;
import com.printims.module.system.entity.SysUser;
import com.printims.module.system.mapper.SysUserMapper;
import com.printims.module.system.service.SysMenuService;
import com.printims.module.system.service.SysUserService;
import com.printims.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录注册与当前用户菜单。
 */
@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SysUserService sysUserService;
    private final SysMenuService sysMenuService;
    private final SysUserMapper sysUserMapper;

    @Log("用户登录")
    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return R.ok(authService.login(req));
    }

    @Log("用户注册")
    @Operation(summary = "注册")
    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return R.ok(null);
    }

    @Operation(summary = "当前用户与菜单")
    @GetMapping("/me")
    public R<Map<String, Object>> me() {
        Long uid = SecurityUtils.requireUserId();
        SysUser u = sysUserService.getById(uid);
        List<MenuTreeVO> menus = sysMenuService.listMenuTreeForUser(uid);
        Map<String, Object> m = new HashMap<>();
        m.put("userId", u.getId());
        m.put("username", u.getUsername());
        m.put("realName", u.getRealName());
        m.put("menus", menus);
        m.put("roleCodes", sysUserMapper.selectRoleCodesByUserId(uid));
        return R.ok(m);
    }
}
