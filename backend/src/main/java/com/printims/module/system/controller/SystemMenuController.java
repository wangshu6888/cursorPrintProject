package com.printims.module.system.controller;

import com.printims.common.annotation.Log;
import com.printims.common.api.R;
import com.printims.module.system.dto.MenuTreeVO;
import com.printims.module.system.entity.SysMenu;
import com.printims.module.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单管理。
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/system/menus")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SystemMenuController {

    private final SysMenuService sysMenuService;

    @Operation(summary = "菜单树")
    @GetMapping("/tree")
    public R<List<MenuTreeVO>> tree() {
        return R.ok(sysMenuService.listFullMenuTree());
    }

    @Operation(summary = "全部平铺")
    @GetMapping
    public R<List<SysMenu>> list() {
        return R.ok(sysMenuService.lambdaQuery().orderByAsc(SysMenu::getSort).list());
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public R<SysMenu> get(@PathVariable Long id) {
        return R.ok(sysMenuService.getById(id));
    }

    @Log("保存菜单")
    @Operation(summary = "保存")
    @PostMapping
    public R<Void> save(@Valid @RequestBody SysMenu body) {
        sysMenuService.save(body);
        return R.ok(null);
    }

    @Log("更新菜单")
    @Operation(summary = "更新")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody SysMenu body) {
        body.setId(id);
        sysMenuService.updateById(body);
        return R.ok(null);
    }

    @Log("删除菜单")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysMenuService.removeById(id);
        return R.ok(null);
    }
}
