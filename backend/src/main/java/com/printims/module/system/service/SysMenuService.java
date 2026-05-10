package com.printims.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.printims.module.system.dto.MenuTreeVO;
import com.printims.module.system.entity.SysMenu;

import java.util.List;

/**
 * 菜单服务。
 */
public interface SysMenuService extends IService<SysMenu> {

    List<MenuTreeVO> listMenuTreeForUser(Long userId);

    List<MenuTreeVO> listFullMenuTree();
}
