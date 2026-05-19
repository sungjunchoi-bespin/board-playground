package com.conduit.shared.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "conduit")
public record ConduitProperties(Jwt jwt, Cors cors) {

  public record Jwt(String secret, Duration expiration) {}

  public record Cors(String allowedOrigins) {}
}
