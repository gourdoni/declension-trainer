package io.github.gourdoni.declension.service;

import io.github.gourdoni.declension.domain.Inflection;
import io.github.gourdoni.declension.domain.InflectionRepository;
import io.github.gourdoni.declension.domain.Revision;
import io.github.gourdoni.declension.domain.RevisionRepository;
import io.github.gourdoni.declension.scheduling.RecallQuality;
import io.github.gourdoni.declension.scheduling.SchedulingStrategy;

import java.time.LocalDate;
import java.util.List;

/**
 * Records the result of revising an inflection.
 * Loads current revision state state (if seen), then applies the chosen scheduling strategy and saves the result.
 */
public final class RevisionService {

    private final RevisionRepository revisions;
    private final InflectionRepository inflections;
    private final SchedulingStrategy strategy;

    public RevisionService(RevisionRepository revisions, InflectionRepository inflections, SchedulingStrategy strategy) {
        this.revisions = revisions;
        this.inflections = inflections;
        this.strategy = strategy;
    }

    public Revision processRevision(long inflectionID, RecallQuality currentRecall, LocalDate lastSeen) {
        // Find the previous revision stored against this inflection, or create an unseen-before revision.
        Revision current = revisions.findByInflection(inflectionID).orElseGet(() -> Revision.unseen(inflectionID));
        return revisions.save(strategy.schedule(current, currentRecall, lastSeen));
    }

    public List<RevisionResult> gradeResponses(List<RevisionResponse.Response> responses, LocalDate revisedOn) {
        return responses.stream().map(response -> gradeResponse(response, revisedOn)).toList();
    }

    private RevisionResult gradeResponse(RevisionResponse.Response response, LocalDate lastRevised) {
        Inflection inflection = inflections.findByID(response.inflectionID())
                                           .orElseThrow(() -> new IllegalArgumentException("Invalid inflection: " + response.inflectionID()));
        String userInput = response.response() == null ? "" : response.response().strip();
        RecallQuality recall = inflection.spelling().equals(userInput) ? RecallQuality.GOOD : RecallQuality.AGAIN;
        Revision updated = processRevision(response.inflectionID(), recall, lastRevised);
        return new RevisionResult(response.inflectionID(), recall == RecallQuality.GOOD, updated.dueDate());
    }
}
