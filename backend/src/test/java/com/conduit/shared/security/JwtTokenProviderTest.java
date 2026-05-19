package com.conduit.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.conduit.shared.config.ConduitProperties;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

  private JwtTokenProvider provider;

  @BeforeEach
  void setUp() {
    ConduitProperties.Jwt jwt =
        new ConduitProperties.Jwt("test-jwt-secret-32chars-minimum!!", Duration.ofDays(1));
    ConduitProperties.Cors cors = new ConduitProperties.Cors("http://localhost:5173");
    ConduitProperties props = new ConduitProperties(jwt, cors);
    provider = new JwtTokenProvider(props);
  }

  @Test
  void generateToken_returnsNonBlankToken() {
    String token = provider.generateToken(1L);
    assertThat(token).isNotBlank();
  }

  @Test
  void extractUserId_returnsCorrectUserId() {
    String token = provider.generateToken(42L);
    Long userId = provider.extractUserId(token);
    assertThat(userId).isEqualTo(42L);
  }

  @Test
  void validateToken_returnsTrueForValidToken() {
    String token = provider.generateToken(1L);
    assertThat(provider.validateToken(token)).isTrue();
  }

  @Test
  void validateToken_returnsFalseForTamperedToken() {
    String token = provider.generateToken(1L);
    String tampered = token.substring(0, token.length() - 3) + "abc";
    assertThat(provider.validateToken(tampered)).isFalse();
  }

  @Test
  void validateToken_returnsFalseForGarbageString() {
    assertThat(provider.validateToken("not-a-jwt")).isFalse();
  }

  @Test
  void validateToken_returnsFalseForExpiredToken() {
    ConduitProperties.Jwt jwt =
        new ConduitProperties.Jwt("test-jwt-secret-32chars-minimum!!", Duration.ZERO);
    ConduitProperties.Cors cors = new ConduitProperties.Cors("http://localhost:5173");
    ConduitProperties props = new ConduitProperties(jwt, cors);
    JwtTokenProvider expiredProvider = new JwtTokenProvider(props);

    String token = expiredProvider.generateToken(1L);
    assertThat(expiredProvider.validateToken(token)).isFalse();
  }
}
