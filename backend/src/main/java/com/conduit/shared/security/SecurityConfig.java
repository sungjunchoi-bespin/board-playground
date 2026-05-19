package com.conduit.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final ObjectMapper objectMapper;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.objectMapper = objectMapper;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Public endpoints
                    .requestMatchers(HttpMethod.POST, "/api/users", "/api/users/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/articles", "/api/articles/feed")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/articles/{slug}")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/articles/{slug}/comments")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/tags")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/profiles/{username}")
                    .permitAll()
                    // Swagger / OpenAPI
                    .requestMatchers(
                        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs")
                    .permitAll()
                    // Actuator health
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    // All other endpoints require authentication
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(
                        (request, response, authException) -> {
                          response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                          response.setCharacterEncoding("UTF-8");
                          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                          String body =
                              objectMapper.writeValueAsString(
                                  Map.of(
                                      "errors",
                                      Map.of("token", new String[] {"is invalid or missing"})));
                          response.getWriter().write(body);
                        })
                    .accessDeniedHandler(
                        (request, response, accessDeniedException) -> {
                          response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                          response.setCharacterEncoding("UTF-8");
                          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                          String body =
                              objectMapper.writeValueAsString(
                                  Map.of(
                                      "errors",
                                      Map.of("authorization", new String[] {"forbidden"})));
                          response.getWriter().write(body);
                        }))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
