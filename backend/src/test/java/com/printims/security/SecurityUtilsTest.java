package com.printims.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void principal_WithValidAuth() {
        LoginUser loginUser = new LoginUser(1L, "admin", "123456", null);
        Authentication auth = new UsernamePasswordAuthenticationToken(loginUser, "123456");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        LoginUser result = SecurityUtils.principal();
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("admin", result.getUsername());
    }

    @Test
    void principal_WithoutAuth() {
        assertNull(SecurityUtils.principal());
    }

    @Test
    void principal_WithInvalidAuth() {
        Authentication auth = new UsernamePasswordAuthenticationToken("string-user", "123456");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        assertNull(SecurityUtils.principal());
    }

    @Test
    void currentUserIdOrNull_WithValidAuth() {
        LoginUser loginUser = new LoginUser(2L, "user", "123456", null);
        Authentication auth = new UsernamePasswordAuthenticationToken(loginUser, "123456");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        assertEquals(2L, SecurityUtils.currentUserIdOrNull());
    }

    @Test
    void currentUserIdOrNull_WithoutAuth() {
        assertNull(SecurityUtils.currentUserIdOrNull());
    }

    @Test
    void requireUserId_WithValidAuth() {
        LoginUser loginUser = new LoginUser(3L, "user3", "123456", null);
        Authentication auth = new UsernamePasswordAuthenticationToken(loginUser, "123456");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        assertEquals(3L, SecurityUtils.requireUserId());
    }

    @Test
    void requireUserId_WithoutAuth() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, SecurityUtils::requireUserId);
        assertEquals("未登录", ex.getMessage());
    }
}
