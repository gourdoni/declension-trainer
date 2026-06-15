package io.github.gourdoni.declension.service;

import io.github.gourdoni.declension.domain.Revision;
import io.github.gourdoni.declension.domain.RevisionRepository;
import io.github.gourdoni.declension.scheduling.RecallQuality;
import io.github.gourdoni.declension.scheduling.SchedulingStrategy;

import java.time.LocalDate;

/**
 * Records the result of revising an inflection.
 * Loads current revision state state (if seen), then applies the chosen scheduling strategy and saves the result.
 */
public final class RevisionService {

    private final RevisionRepository revisions;
    private final SchedulingStrategy strategy;

    public RevisionService(RevisionRepository revisions, SchedulingStrategy strategy) {
        this.revisions = revisions;
        this.strategy = strategy;
    }

    public Revision processRevision(long inflectionID, RecallQuality currentRecall, LocalDate lastSeen) {
        // Find the previous revision stored against this inflection, or create an unseen-before revision.
        Revision current = revisions.findByInflection(inflectionID)
                                    .orElseGet(() -> Revision.unseen(inflectionID));
        return revisions.save(strategy.schedule(current, currentRecall, lastSeen));
    }
}
