package com.serviceforge.persistence;

import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Component;

/**
 * Applies versioned Flyway migrations (classpath:db/migration) to the local SQLite file.
 * ETSTCC-13074: introduces the single new table {@code feedback}.
 *
 * Legacy tables are still created by {@link SqliteSchemaInitializer}; an existing database without
 * Flyway history is baselined at version 0 so that V1 is applied exactly once.
 * No network, no credentials, no Spring DataSource. Failures propagate (startup fails fast).
 */
@Component
public class SqliteMigrationRunner {

    public void migrate(String sqlitePath) {
        Flyway.configure()
                .dataSource("jdbc:sqlite:" + sqlitePath, null, null)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .load()
                .migrate();
    }
}
