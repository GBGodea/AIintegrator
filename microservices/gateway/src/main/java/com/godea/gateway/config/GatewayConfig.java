package com.godea.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "jwt.auth")
public class GatewayConfig {
    private List<String> requiredAuthorities;
}
