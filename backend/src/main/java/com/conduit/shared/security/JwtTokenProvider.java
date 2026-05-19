package com.conduit.shared.security;

import com.conduit.shared.config.ConduitProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private final SecretKey signingKey;
  private final long expirationMillis;

  public JwtTokenProvider(ConduitProperties properties) {
    this.signingKey =
        Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8));
    this.expirationMillis = properties.jwt().expiration().toMillis();
  }

  public String generateToken(Long userId) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMillis);

    return Jwts.builder()
        .claim("userId", userId)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(signingKey)
        .compact();
  }

  public Long extractUserId(String token) {
    Claims claims =
        Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    return claims.get("userId", Long.class);
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}
