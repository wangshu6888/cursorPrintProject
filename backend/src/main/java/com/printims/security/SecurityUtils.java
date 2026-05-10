package com.printims.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户上下文工具。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser lu)) {
            return null;
        }
        return lu;
    }

    public static Long currentUserIdOrNull() {
        LoginUser lu = principal();
        return lu == null ? null : lu.getUserId();
    }

    public static Long requireUserId() {
        LoginUser lu = principal();
        if (lu == null) {
            throw new IllegalStateException("未登录");
        }
        return lu.getUserId();
    }
}
