package com.printims.module.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.printims.module.system.entity.SysRole;
import com.printims.module.system.mapper.SysRoleMapper;
import com.printims.module.system.service.SysRoleService;
import org.springframework.stereotype.Service;

/**
 * 角色服务实现。
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
}
