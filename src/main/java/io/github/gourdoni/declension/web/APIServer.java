package io.github.gourdoni.declension.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.gourdoni.declension.domain.DataAccessException;
import io.github.gourdoni.declension.domain.LanguageRepository;
import io.github.gourdoni.declension.domain.NounSortOrder;
import io.github.gourdoni.declension.domain.NounListEntryQuery;
import io.github.gourdoni.declension.domain.RevisionQueue;
import io.github.gourdoni.declension.service.NounDraft;
import io.github.gourdoni.declension.service.NounService;
import io.github.gourdoni.declension.service.RevisionResponse;
import io.github.gourdoni.declension.service.RevisionService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;

public final class APIServer {

    private final HttpServer server;

    public APIServer(int port,
                     LanguageRepository languages,
                     NounListEntryQuery nounListEntryQuery,
                     NounService nounService,
                     RevisionQueue revisionQueue,
                     RevisionService revisionService) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/languages", exchange -> handle(exchange, () -> {
            requireMethod(exchange, "GET");
            return languages.findAll();
        }));
        server.createContext("/api/nouns", exchange -> handle(exchange, () -> switch (extractMethod(exchange)) {
            case "GET" -> nounListEntryQuery.forLanguage(getRequiredLong(exchange, "language"), sortFrom(exchange));
            case "POST" -> nounService.create(readBody(exchange, NounDraft.class));
            default -> throw new MethodNotAllowed("GET or POST required");
        }));
        server.createContext("/api/review/submit", exchange -> handle(exchange, () -> {
            requireMethod(exchange, "POST");
            RevisionResponse response = readBody(exchange, RevisionResponse.class);
            return revisionService.gradeResponses(response.responses(), LocalDate.now());
        }));
        server.createContext("/api/review", exchange -> handle(exchange, () -> {
            requireMethod(exchange, "GET");
            return revisionQueue.dueForLanguage(getRequiredLong(exchange, "language"), LocalDate.now());
        }));
    }

    public void start() {
        server.start();
    }

    private void handle(HttpExchange exchange, APIAction action) throws IOException {
        try {
            outputJson(exchange, 200, action.run());
        } catch (MethodNotAllowed exception) {
            outputJson(exchange, 405, new APIError(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            outputJson(exchange, 400, new APIError(exception.getMessage()));
        } catch (DataAccessException exception) {
            outputJson(exchange, 500, new APIError("Internal server error"));
        } finally {
            exchange.close();
        }
    }

    private void outputJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = JsonHandler.instance().toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream stream = exchange.getResponseBody()) {
            stream.write(bytes);
        }
    }

    private <T> T readBody(HttpExchange exchange, Class<T> type) throws IOException {
        try (Reader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            T body = JsonHandler.instance().fromJson(reader, type);
            if (body == null) {
                throw new IllegalArgumentException("Requests need a body");
            }
            return body;
        }
    }

    private String extractMethod(HttpExchange exchange) {
        return exchange.getRequestMethod().toUpperCase(Locale.ROOT);
    }

    private void requireMethod(HttpExchange exchange, String expected) {
        if (!extractMethod(exchange).equals(expected)) {
            throw new MethodNotAllowed(expected + " required");
        }
    }

    private NounSortOrder sortFrom(HttpExchange exchange) {
        String sort = getQueryPara(exchange, "sort");
        if (sort == null || sort.isBlank()) {
            return NounSortOrder.DUE;
        }
        try {
            return NounSortOrder.valueOf(sort.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sort order: " + sort);
        }
    }

    private long getRequiredLong(HttpExchange exchange, String para) {
        String value = getQueryPara(exchange, para);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing parameter: " + para);
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Parameter " + para + " must be a number");
        }
    }

    private String getQueryPara(HttpExchange exchange, String para) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            int equalsIndex = pair.indexOf('=');
            String key = equalsIndex < 0 ? pair : pair.substring(0, equalsIndex);
            if (key.equals(para)) {
                return equalsIndex < 0 ? "" : URLDecoder.decode(pair.substring(equalsIndex + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface APIAction {
        Object run() throws IOException;
    }

    private static final class MethodNotAllowed extends RuntimeException {
        MethodNotAllowed(String message) {
            super(message);
        }
    }

    public record APIError(String error) {
    }
}
