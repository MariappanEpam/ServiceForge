package com.serviceforge.persistence;

import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

@Component
public class SqliteConnectionFactory {

    public Connection open(String sqlitePath) {
        try {
            Path p = Path.of(sqlitePath);
            Path parent = p.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            // sqlite JDBC URL
            String url = "jdbc:sqlite:" + sqlitePath;
            return DriverManager.getConnection(url);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to open SQLite DB at " + sqlitePath, e);
        }
    }
}
