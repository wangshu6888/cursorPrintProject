package com.printims.config;

import com.printims.module.system.constants.RoleCodes;
import com.printims.module.system.entity.SysMenu;
import com.printims.module.system.entity.SysRole;
import com.printims.module.system.entity.SysRoleMenu;
import com.printims.module.system.entity.SysUser;
import com.printims.module.system.entity.SysUserRole;
import com.printims.module.system.mapper.SysRoleMenuMapper;
import com.printims.module.system.mapper.SysUserRoleMapper;
import com.printims.module.system.service.SysMenuService;
import com.printims.module.system.service.SysRoleService;
import com.printims.module.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 首次启动时初始化角色、菜单、管理员账号。
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysMenuService sysMenuService;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        if (sysUserService.count() > 0) {
            return;
        }

        SysRole adminRole = new SysRole();
        adminRole.setRoleName("超级管理员");
        adminRole.setRoleCode(RoleCodes.SUPER_ADMIN);
        adminRole.setDescription("全部权限");
        adminRole.setStatus(1);
        sysRoleService.save(adminRole);

        SysRole financeRole = new SysRole();
        financeRole.setRoleName("财务人员");
        financeRole.setRoleCode(RoleCodes.FINANCE);
        financeRole.setDescription("订单查看与统计");
        financeRole.setStatus(1);
        sysRoleService.save(financeRole);

        SysRole empRole = new SysRole();
        empRole.setRoleName("普通员工");
        empRole.setRoleCode(RoleCodes.EMPLOYEE);
        empRole.setDescription("订单、客户、刀模");
        empRole.setStatus(1);
        sysRoleService.save(empRole);

        Map<String, Long> menuPathToId = new HashMap<>();
        menuPathToId.put("/dashboard", saveMenu("工作台", 0L, "/dashboard", "dashboard/index", "Odometer", 1, 1, null));
        menuPathToId.put("/order", saveMenu("订单管理", 0L, "/order", "order/index", "Document", 2, 1, "order:list"));
        menuPathToId.put("/customer", saveMenu("客户管理", 0L, "/customer", "customer/index", "User", 3, 1, "customer:list"));
        menuPathToId.put("/knife-mold", saveMenu("刀模管理", 0L, "/knife-mold", "knife-mold/index", "Box", 4, 1, "knife:list"));
        menuPathToId.put("/statistics", saveMenu("数据统计", 0L, "/statistics", "statistics/index", "DataAnalysis", 5, 1, "stats:view"));
        menuPathToId.put("/system/user", saveMenu("用户管理", 0L, "/system/user", "system/user/index", "Avatar", 6, 1, "sys:user:list"));
        menuPathToId.put("/system/role", saveMenu("角色管理", 0L, "/system/role", "system/role/index", "Key", 7, 1, "sys:role:list"));
        menuPathToId.put("/system/menu", saveMenu("菜单管理", 0L, "/system/menu", "system/menu/index", "Menu", 8, 1, "sys:menu:list"));
        menuPathToId.put("/system/log", saveMenu("操作日志", 0L, "/system/log", "system/log/index", "Notebook", 9, 1, "sys:log:list"));

        bindAllMenus(adminRole.getId(), menuPathToId);
        bindMenus(financeRole.getId(), menuPathToId, "/dashboard", "/order", "/statistics");
        bindMenus(empRole.getId(), menuPathToId, "/dashboard", "/order", "/customer", "/knife-mold");

        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRealName("系统管理员");
        admin.setStatus(1);
        sysUserService.save(admin);

        SysUserRole ur = new SysUserRole();
        ur.setUserId(admin.getId());
        ur.setRoleId(adminRole.getId());
        sysUserRoleMapper.insert(ur);
    }

    private Long saveMenu(String name, Long parentId, String path, String component, String icon, int sort, int menuType,
                          String permission) {
        SysMenu m = new SysMenu();
        m.setParentId(parentId);
        m.setMenuName(name);
        m.setPath(path);
        m.setComponent(component);
        m.setIcon(icon);
        m.setSort(sort);
        m.setMenuType(menuType);
        m.setPermission(permission);
        m.setStatus(1);
        sysMenuService.save(m);
        return m.getId();
    }

    private void bindAllMenus(Long roleId, Map<String, Long> pathToId) {
        for (Long mid : pathToId.values()) {
            insertRm(roleId, mid);
        }
    }

    private void bindMenus(Long roleId, Map<String, Long> pathToId, String... paths) {
        for (String p : paths) {
            Long mid = pathToId.get(p);
            if (mid != null) {
                insertRm(roleId, mid);
            }
        }
    }

    private void insertRm(Long roleId, Long menuId) {
        SysRoleMenu rm = new SysRoleMenu();
        rm.setRoleId(roleId);
        rm.setMenuId(menuId);
        sysRoleMenuMapper.insert(rm);
    }
}
