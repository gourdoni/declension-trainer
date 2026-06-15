package io.github.gourdoni.declension.domain;

import java.time.LocalDate;

/**
 * SM-2 spaced-repetition state for a single inflection.
 * A null due date indicates that the inflection is unseen by the user.
 */
public record Revision(long id, long inflectionID, int intervalDays, double easeFactor, int repetitions, LocalDate dueDate) {

    public static final double DEFAULT_EASE_FACTOR = 2.5;

    // For inflections that are unseen; have not been revised yet.
    public static Revision unseen(long inflectionID) {
        return new Revision(0, inflectionID, 0, DEFAULT_EASE_FACTOR, 0, null);
    }
}
