package com.printims.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.printims.common.annotation.Log;
import com.printims.module.system.entity.SysOperationLog;
import com.printims.module.system.service.SysOperationLogService;
import com.printims.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志切面：记录方法调用结果与耗时。
 */
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final SysOperationLogService logService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(logAnn)")
    public Object around(ProceedingJoinPoint pjp, Log logAnn) throws Throwable {
        long start = System.currentTimeMillis();
        SysOperationLog entity = new SysOperationLog();
        entity.setOperation(logAnn.value());
        entity.setMethod(pjp.getSignature().toShortString());
        entity.setCreateTime(LocalDateTime.now());
        try {
            entity.setParams(objectMapper.writeValueAsString(pjp.getArgs()));
        } catch (Exception e) {
            entity.setParams("[]");
        }
        var user = SecurityUtils.principal();
        if (user != null) {
            entity.setUserId(user.getUserId());
            entity.setUsername(user.getUsername());
        }
        HttpServletRequest req = currentRequest();
        if (req != null) {
            entity.setIp(clientIp(req));
        }
        try {
            Object result = pjp.proceed();
            entity.setStatus(1);
            return result;
        } catch (Throwable ex) {
            entity.setStatus(0);
            entity.setErrorMsg(ex.getMessage());
            throw ex;
        } finally {
            entity.setCostTime(System.currentTimeMillis() - start);
            logService.save(entity);
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }

    private String clientIp(HttpServletRequest req) {
        String h = req.getHeader("X-Forwarded-For");
        if (h != null && !h.isBlank()) {
            return h.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
