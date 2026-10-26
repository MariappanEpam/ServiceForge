package com.serviceforge.config;

import com.serviceforge.persistence.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(PersistenceProperties.class)
public class PersistenceConfig {

    @Bean
        @Primary
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

    // ETSTCC-13074: same mode logic as above; the feedback store follows the persistence mode.
    @Bean
    @Primary
    public FeedbackRepository feedbackRepository(
            PersistenceProperties props,
            SqliteFeedbackRepository sqlite,
            InMemoryFeedbackRepository inMemory
    ) {
        String mode = props.getPersistence() == null ? "memory" : props.getPersistence().trim().toLowerCase();
        if (mode.equals("sqlite")) {
            return sqlite;
        }
        return inMemory;
    }
}
