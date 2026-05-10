package com.printims.module.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.printims.module.system.entity.SysOperationLog;
import com.printims.module.system.mapper.SysOperationLogMapper;
import com.printims.module.system.service.SysOperationLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现。
 */
@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements SysOperationLogService {
}
