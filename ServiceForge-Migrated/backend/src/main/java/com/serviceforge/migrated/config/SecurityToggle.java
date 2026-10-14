package com.serviceforge.migrated.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Simple toggle used by SpEL in @PreAuthorize expressions.
 *
 * When disabled (local dev), controllers allow requests without Entra roles.
 */
@Component("securityToggle")
public class SecurityToggle {

  private final boolean enabled;

  public SecurityToggle(@Value("${serviceforge.security.enabled:true}") boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
