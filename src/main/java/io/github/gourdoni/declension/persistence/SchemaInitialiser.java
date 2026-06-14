package io.github.gourdoni.declension.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaInitialiser {

    // Resources are bundled into the JAR.
    private static final String SCHEMA_RESOURCE_FILE = "/schema.sql";

    private final Database database;

    public SchemaInitialiser(Database database) {
        this.database = database;
    }

    public void createEntities() throws SQLException {
        String script = readInitScript();
        try (Connection connection = this.database.openConnection()) {
            // Apply everything at once, in one transaction; not one-by-one.
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                // This is naive SQL parsing; e.g. breaks for `;` inside strings.
                // Fixing probably pulls in an SQL parsing dependency; look into this later?
                for (String sql : script.split(";")) {
                    if (!sql.isBlank()) {
                        statement.execute(sql);
                    }
                }
                connection.commit();
            } catch (SQLException exception) {
                // Undo partially-constructed transaction if anything fails.
                connection.rollback();
                throw exception;
            }
        }
    }

    private String readInitScript() {
        try (InputStream input = SchemaInitialiser.class.getResourceAsStream(SCHEMA_RESOURCE_FILE)) {
            if (input == null) {
                // This is a build or packaging error, not found on classpath; not recoverable.
                throw new IllegalStateException("Resource not found: " + SCHEMA_RESOURCE_FILE);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            // Corrupt; also not recoverable.
            throw new UncheckedIOException("Could not read: " + SCHEMA_RESOURCE_FILE, exception);
        }
    }
}