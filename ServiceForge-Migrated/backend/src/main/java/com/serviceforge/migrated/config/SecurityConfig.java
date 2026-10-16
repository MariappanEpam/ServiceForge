package com.serviceforge.migrated.config;

import com.serviceforge.migrated.api.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Bean
  @ConditionalOnProperty(name = "serviceforge.security.enabled", havingValue = "true", matchIfMissing = true)
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
        .authenticationEntryPoint(this::writeUnauthorized)
        .accessDeniedHandler(this::writeForbidden));

    return http.build();
  }

  @Bean
  @ConditionalOnProperty(name = "serviceforge.security.enabled", havingValue = "false")
  public SecurityFilterChain insecureSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

    return http.build();
  }

  @Bean
  @ConditionalOnProperty(name = "serviceforge.security.enabled", havingValue = "true", matchIfMissing = true)
  JwtDecoder jwtDecoder(
      @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
      @Value("${serviceforge.security.audience}") String audience) {

    NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();

    OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
    OAuth2TokenValidator<Jwt> withAudience = token -> {
      List<String> audiences = token.getAudience();
      if (audiences != null && audiences.contains(audience)) {
        return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success();
      }
      return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
          new OAuth2Error("invalid_token", "Required audience is missing", null));
    };

    decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
    return decoder;
  }

  private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
    scopes.setAuthorityPrefix("SCOPE_");
    scopes.setAuthoritiesClaimName("scp");

    return jwt -> {
      Collection<GrantedAuthority> base = scopes.convert(jwt);
      List<GrantedAuthority> authorities = new ArrayList<>(base == null ? List.of() : base);

      // Entra app roles are typically in "roles" claim.
      Object rolesClaim = jwt.getClaims().get("roles");
      if (rolesClaim instanceof Collection<?> roles) {
        for (Object r : roles) {
          if (r != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + r.toString()));
          }
        }
      }

      String principal = jwt.getClaimAsString(JwtClaimNames.SUB);
      if (principal == null) {
        principal = jwt.getClaimAsString("oid");
      }
      if (principal == null) {
        principal = "unknown";
      }

      return new JwtAuthenticationToken(jwt, authorities, principal);
    };
  }

  private void writeUnauthorized(HttpServletRequest req, HttpServletResponse res, Exception ex) throws java.io.IOException {
    res.setStatus(401);
    res.setContentType("application/json");
    ApiError body = new ApiError(Instant.now(), 401, "Unauthorized", "Unauthorized", req.getRequestURI(), "UNAUTHORIZED");
    objectMapper.writeValue(res.getOutputStream(), body);
  }

  private void writeForbidden(HttpServletRequest req, HttpServletResponse res, org.springframework.security.access.AccessDeniedException ex) throws java.io.IOException {
    res.setStatus(403);
    res.setContentType("application/json");
    ApiError body = new ApiError(Instant.now(), 403, "Forbidden", "Forbidden", req.getRequestURI(), "FORBIDDEN");
    objectMapper.writeValue(res.getOutputStream(), body);
  }
}
