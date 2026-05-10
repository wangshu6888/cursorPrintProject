package com.printims.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.printims.security.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自动填充创建时间、更新时间及操作人。
 */
@Component
public class MetaObjectHandlerConfig implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        Long uid = SecurityUtils.currentUserIdOrNull();
        if (uid != null) {
            strictInsertFill(metaObject, "createBy", Long.class, uid);
            strictInsertFill(metaObject, "updateBy", Long.class, uid);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        Long uid = SecurityUtils.currentUserIdOrNull();
        if (uid != null) {
            strictUpdateFill(metaObject, "updateBy", Long.class, uid);
        }
    }
}
