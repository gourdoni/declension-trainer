package io.github.gourdoni.declension.scheduling;

import io.github.gourdoni.declension.domain.Revision;

import java.time.LocalDate;

/**
 * SM-2 spaced-repetition.
 * The ease factor (floor 1.3) is adjusted on every revision.
 * Any lapses (recall quality less than 3) reset the inflection back to a one-day interval.
 */
public final class SM2SchedulingStrategy implements SchedulingStrategy {

    private static final double EASE_FACTOR_FLOOR = 1.3;

    @Override
    public Revision schedule(Revision previousRevision, RecallQuality currentRecall, LocalDate lastSeen) {
        int quality = currentRecall.quality();
        double easeFactor = adjustEaseFactor(previousRevision.easeFactor(), quality);
        int repetitions = (quality < 3) ? 0 : previousRevision.repetitions() + 1;
        int intervalDays = (quality < 3) ? 1 : switch (repetitions) {
            case 1 -> 1;
            case 2 -> 6;
            default -> (int) Math.round(previousRevision.intervalDays() * easeFactor);
        };
        return new Revision(previousRevision.id(),
                            previousRevision.inflectionID(),
                            intervalDays,
                            easeFactor,
                            repetitions,
                            lastSeen.plusDays(intervalDays));
    }

    private double adjustEaseFactor(double easeFactor, int quality) {
        return Math.max(EASE_FACTOR_FLOOR, easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)));
    }
}
