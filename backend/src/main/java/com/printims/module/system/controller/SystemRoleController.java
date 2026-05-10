package com.printims.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printims.common.annotation.Log;
import com.printims.common.api.PageResult;
import com.printims.common.api.R;
import com.printims.common.exception.BusinessException;
import com.printims.module.system.dto.IdsRequest;
import com.printims.module.system.entity.SysRole;
import com.printims.module.system.entity.SysRoleMenu;
import com.printims.module.system.mapper.SysRoleMenuMapper;
import com.printims.module.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * 角色管理。
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SystemRoleController {

    private final SysRoleService sysRoleService;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    @Operation(summary = "分页")
    @GetMapping
    public R<PageResult<SysRole>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "50") long pageSize,
            @RequestParam(required = false) String keyword) {
        Page<SysRole> p = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String k = keyword.trim();
            w.and(x -> x.like(SysRole::getRoleName, k).or().like(SysRole::getRoleCode, k));
        }
        w.orderByAsc(SysRole::getId);
        Page<SysRole> r = sysRoleService.page(p, w);
        return R.ok(PageResult.of(r.getRecords(), r.getTotal(), r.getCurrent(), r.getSize()));
    }

    @Operation(summary = "全部角色")
    @GetMapping("/all")
    public R<List<SysRole>> all() {
        return R.ok(sysRoleService.list(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId)));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public R<SysRole> get(@PathVariable Long id) {
        return R.ok(sysRoleService.getById(id));
    }

    @Log("保存角色")
    @Operation(summary = "保存")
    @PostMapping
    public R<Void> save(@Valid @RequestBody SysRole body) {
        if (sysRoleService.lambdaQuery().eq(SysRole::getRoleCode, body.getRoleCode()).exists()) {
            throw new BusinessException("角色编码已存在");
        }
        sysRoleService.save(body);
        return R.ok(null);
    }

    @Log("更新角色")
    @Operation(summary = "更新")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody SysRole body) {
        SysRole exist = sysRoleService.getById(id);
        if (exist == null) {
            throw new BusinessException("角色不存在");
        }
        if (!exist.getRoleCode().equals(body.getRoleCode())
                && sysRoleService.lambdaQuery().eq(SysRole::getRoleCode, body.getRoleCode()).exists()) {
            throw new BusinessException("角色编码已存在");
        }
        body.setId(id);
        sysRoleService.updateById(body);
        return R.ok(null);
    }

    @Log("删除角色")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysRoleService.removeById(id);
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        return R.ok(null);
    }

    @Operation(summary = "角色已分配菜单ID")
    @GetMapping("/{id}/menus")
    public R<List<Long>> menus(@PathVariable Long id) {
        List<SysRoleMenu> list = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        return R.ok(list.stream().map(SysRoleMenu::getMenuId).toList());
    }

    @Log("分配角色菜单")
    @Operation(summary = "分配菜单")
    @PutMapping("/{id}/menus")
    public R<Void> assignMenus(@PathVariable Long id, @RequestBody IdsRequest body) {
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        if (body.getIds() == null) {
            return R.ok(null);
        }
        for (Long mid : body.getIds()) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(id);
            rm.setMenuId(mid);
            sysRoleMenuMapper.insert(rm);
        }
        return R.ok(null);
    }
}
