package io.github.gourdoni.declension.service;

import io.github.gourdoni.declension.scheduling.RecallQuality;
import java.util.List;

public record RevisionResponse(List<Response> responses) {
    public record Response(long inflectionID, RecallQuality recall) {}
}
