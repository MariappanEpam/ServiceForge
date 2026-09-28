package com.serviceforge.config;

import com.serviceforge.persistence.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PersistenceProperties.class)
public class PersistenceConfig {

    @Bean
    public ServiceForgeDataStore serviceForgeDataStore(
            PersistenceProperties props,
            InMemoryDataStoreAdapter inMemory,
            SqliteDataStoreAdapter sqlite
    ) {
        String mode = props.getPersistence() == null ? "memory" : props.getPersistence().trim().toLowerCase();
        if (mode.equals("sqlite")) {
            return sqlite;
        }
        return inMemory;
    }
}
