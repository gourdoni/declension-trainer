package io.github.gourdoni.declension;

import com.sun.net.httpserver.HttpServer;
import io.github.gourdoni.declension.persistence.Database;
import io.github.gourdoni.declension.persistence.SchemaInitialiser;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) throws IOException, SQLException {
        Path databaseFile = Path.of("data", "declensions.db");
        // Create database and entities if not already configured on prior invocation.
        Database database = new Database(databaseFile);
        SchemaInitialiser initialiser = new SchemaInitialiser(database);
        initialiser.createEntities();
        System.out.println("Connected to database and created tables: " + databaseFile);
        // Configure and execute HTTP server.
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", exchange -> {
            byte[] body = "Salvete! Valete!".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        System.out.println("Server running: http://localhost:8080");
    }
}
