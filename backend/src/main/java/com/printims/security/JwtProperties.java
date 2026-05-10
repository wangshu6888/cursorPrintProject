package com.printims.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置项。
 */
@Data
@Component
@ConfigurationProperties(prefix = "print.jwt")
public class JwtProperties {

    private String secret = "change-me";
    private long expireMinutes = 1440;
}
