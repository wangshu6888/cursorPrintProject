package com.printims.module.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.printims.module.system.dto.MenuTreeVO;
import com.printims.module.system.entity.SysMenu;
import com.printims.module.system.mapper.SysMenuMapper;
import com.printims.module.system.service.SysMenuService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现。
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public List<MenuTreeVO> listMenuTreeForUser(Long userId) {
        List<SysMenu> menus = baseMapper.selectMenusByUserId(userId);
        return buildTree(filterRouteMenus(menus));
    }

    @Override
    public List<MenuTreeVO> listFullMenuTree() {
        List<SysMenu> menus = lambdaQuery().orderByAsc(SysMenu::getSort).list();
        return buildTree(filterRouteMenus(menus));
    }

    private List<SysMenu> filterRouteMenus(List<SysMenu> menus) {
        return menus.stream()
                .filter(m -> m.getMenuType() != null && m.getMenuType() <= 1)
                .collect(Collectors.toList());
    }

    private List<MenuTreeVO> buildTree(List<SysMenu> menus) {
        Map<Long, MenuTreeVO> map = menus.stream().map(m -> {
            MenuTreeVO vo = new MenuTreeVO();
            BeanUtils.copyProperties(m, vo);
            return vo;
        }).collect(Collectors.toMap(MenuTreeVO::getId, v -> v, (a, b) -> a));

        List<MenuTreeVO> roots = new ArrayList<>();
        for (MenuTreeVO vo : map.values()) {
            Long pid = vo.getParentId() == null ? 0L : vo.getParentId();
            if (pid == 0L) {
                roots.add(vo);
            } else {
                MenuTreeVO parent = map.get(pid);
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        sortTree(roots);
        return roots;
    }

    private void sortTree(List<MenuTreeVO> nodes) {
        nodes.sort(Comparator.comparingInt(a -> a.getSort() == null ? 0 : a.getSort()));
        for (MenuTreeVO n : nodes) {
            if (!n.getChildren().isEmpty()) {
                sortTree(n.getChildren());
            }
        }
    }
}
