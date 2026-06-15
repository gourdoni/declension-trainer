package io.github.gourdoni.declension.scheduling;

import io.github.gourdoni.declension.domain.Revision;

import java.time.LocalDate;

/**
 * Successful revisions double the interval; lapses reset it to one day.
 * No ease factor.
 */
public final class DoublingSchedulingStrategy implements SchedulingStrategy {

    @Override
    public Revision schedule(Revision previousRevision, RecallQuality currentRecall, LocalDate lastSeen) {
        int intervalDays = 1;
        // If recalled (i.e. no lapse), double the interval before next revision.
        if (currentRecall.quality() >= 3) {
            intervalDays = previousRevision.intervalDays() == 0 ? 1 : previousRevision.intervalDays() * 2;
        }
        return new Revision(previousRevision.id(),
                            previousRevision.inflectionID(),
                            intervalDays,
                            previousRevision.easeFactor(),
                            (currentRecall.quality() < 3) ? 0 : previousRevision.repetitions() + 1,
                            lastSeen.plusDays(intervalDays));
    }
}
