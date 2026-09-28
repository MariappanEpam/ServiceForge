package com.serviceforge.persistence;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "serviceforge")
public class PersistenceProperties {

    /**
     * Persistence mode.
     * - memory (default)
     * - sqlite
     */
    private String persistence = "memory";

    /**
     * SQLite DB file path.
     */
    private String sqlitePath = "backend/.data/serviceforge.db";

    public String getPersistence() {
        return persistence;
    }

    public void setPersistence(String persistence) {
        this.persistence = persistence;
    }

    public String getSqlitePath() {
        return sqlitePath;
    }

    public void setSqlitePath(String sqlitePath) {
        this.sqlitePath = sqlitePath;
    }
}
