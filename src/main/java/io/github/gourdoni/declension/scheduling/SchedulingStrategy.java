package io.github.gourdoni.declension.scheduling;

import io.github.gourdoni.declension.domain.Revision;

import java.time.LocalDate;

/**
 * Rescheduling procedure for an inflection after a revision.
 */
public interface SchedulingStrategy {
    Revision schedule(Revision previousRevision, RecallQuality currentRecall, LocalDate lastSeen);
}
