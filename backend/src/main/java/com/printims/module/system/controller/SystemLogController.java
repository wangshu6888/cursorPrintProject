package com.printims.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printims.common.api.PageResult;
import com.printims.common.api.R;
import com.printims.module.system.entity.SysOperationLog;
import com.printims.module.system.service.SysOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 操作日志查询。
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/system/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@org.springframework.validation.annotation.Validated
public class SystemLogController {

    private final SysOperationLogService logService;

    @Operation(summary = "分页查询")
    @GetMapping
    public R<PageResult<SysOperationLog>> page(
            @RequestParam(defaultValue = "1") @jakarta.validation.constraints.Min(1) long pageNum,
            @RequestParam(defaultValue = "20") @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(1000) long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Page<SysOperationLog> p = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysOperationLog> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String k = keyword.trim();
            w.and(x -> x.like(SysOperationLog::getUsername, k).or().like(SysOperationLog::getOperation, k));
        }
        if (start != null) {
            w.ge(SysOperationLog::getCreateTime, start);
        }
        if (end != null) {
            w.le(SysOperationLog::getCreateTime, end);
        }
        w.orderByDesc(SysOperationLog::getCreateTime);
        Page<SysOperationLog> r = logService.page(p, w);
        return R.ok(PageResult.of(r.getRecords(), r.getTotal(), r.getCurrent(), r.getSize()));
    }
}
