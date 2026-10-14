package com.serviceforge.migrated.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    // Dev UI origins (Vite may auto-increment ports if the default is busy)
    config.addAllowedOriginPattern("http://localhost:5173");
    config.addAllowedOriginPattern("http://127.0.0.1:5173");
    config.addAllowedOriginPattern("http://localhost:5174");
    config.addAllowedOriginPattern("http://127.0.0.1:5174");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    config.addExposedHeader("Location");
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
