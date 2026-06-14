package io.github.gourdoni.declension.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {

    private final String databaseFileURL;

    public Database(Path databaseFile) throws IOException {
        // Create database file parent directories if not already present.
        Path databaseFileParent = databaseFile.toAbsolutePath().getParent();
        if (databaseFileParent != null) {
            Files.createDirectories(databaseFileParent);
        }
        this.databaseFileURL = "jdbc:sqlite:" + databaseFile;
    }

    public Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(this.databaseFileURL);
        // Observe foreign key constraints.
        // SQLite has foreign key constraints off by default; setting does not persist across connections.
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException exception) {
            // Clean up the connection if an `SQLException` occurs.
            connection.close();
            throw exception;
        }
        return connection;
    }
}