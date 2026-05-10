package com.printims.module.system.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 前端路由菜单树节点。
 */
@Data
public class MenuTreeVO {

    private Long id;
    private Long parentId;
    private String menuName;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private Integer menuType;
    private String permission;
    private List<MenuTreeVO> children = new ArrayList<>();
}
