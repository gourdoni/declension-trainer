package io.github.gourdoni.declension.service;

import java.util.List;

public record RevisionResponse(List<Response> responses) {
    public record Response(long inflectionID, String response) {}
}
