package com.printims.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printims.common.annotation.Log;
import com.printims.common.api.PageResult;
import com.printims.common.api.R;
import com.printims.common.exception.BusinessException;
import com.printims.module.system.dto.IdsRequest;
import com.printims.module.system.dto.SysUserRequest;
import com.printims.module.system.entity.SysUser;
import com.printims.module.system.entity.SysUserRole;
import com.printims.module.system.mapper.SysUserRoleMapper;
import com.printims.module.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统用户管理。
 */
@Tag(name = "系统用户")
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Validated
public class SystemUserController {

    private final SysUserService sysUserService;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "分页")
    @GetMapping
    public R<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") @Min(1) long pageNum,
            @RequestParam(defaultValue = "20") @Min(1) @Max(1000) long pageSize,
            @RequestParam(required = false) String keyword) {
        Page<SysUser> p = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String k = keyword.trim();
            w.and(x -> x.like(SysUser::getUsername, k).or().like(SysUser::getRealName, k));
        }
        w.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> r = sysUserService.page(p, w);
        return R.ok(PageResult.of(r.getRecords(), r.getTotal(), r.getCurrent(), r.getSize()));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public R<SysUser> get(@PathVariable Long id) {
        return R.ok(sysUserService.getById(id));
    }

    @Log("新增用户")
    @Operation(summary = "新增")
    @PostMapping
    public R<Void> create(@Valid @RequestBody SysUserRequest req) {
        if (sysUserService.getByUsername(req.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        if (!StringUtils.hasText(req.getPassword())) {
            throw new BusinessException("请设置初始密码");
        }
        SysUser u = toUser(req, true);
        sysUserService.save(u);
        bindRoles(u.getId(), req.getRoleIds());
        return R.ok(null);
    }

    @Log("更新用户")
    @Operation(summary = "更新")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody SysUserRequest req) {
        SysUser exist = sysUserService.getById(id);
        if (exist == null) {
            throw new BusinessException("用户不存在");
        }
        if (!exist.getUsername().equals(req.getUsername())
                && sysUserService.getByUsername(req.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        SysUser u = toUser(req, false);
        u.setId(id);
        if (!StringUtils.hasText(req.getPassword())) {
            u.setPassword(exist.getPassword());
        }
        sysUserService.updateById(u);
        if (req.getRoleIds() != null) {
            sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
            bindRoles(id, req.getRoleIds());
        }
        return R.ok(null);
    }

    @Log("删除用户")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        return R.ok(null);
    }

    @Operation(summary = "用户已分配角色ID")
    @GetMapping("/{id}/roles")
    public R<List<Long>> roles(@PathVariable Long id) {
        List<SysUserRole> list = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        return R.ok(list.stream().map(SysUserRole::getRoleId).toList());
    }

    @Log("分配用户角色")
    @Operation(summary = "分配角色")
    @PutMapping("/{id}/roles")
    public R<Void> assignRoles(@PathVariable Long id, @RequestBody IdsRequest body) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        bindRoles(id, body.getIds());
        return R.ok(null);
    }

    private SysUser toUser(SysUserRequest req, boolean isNew) {
        SysUser u = new SysUser();
        u.setUsername(req.getUsername());
        u.setRealName(req.getRealName());
        u.setPhone(req.getPhone());
        u.setEmail(req.getEmail());
        u.setStatus(req.getStatus());
        if (isNew || StringUtils.hasText(req.getPassword())) {
            u.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        return u;
    }

    private void bindRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null) {
            return;
        }
        for (Long rid : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(rid);
            sysUserRoleMapper.insert(ur);
        }
    }
}
