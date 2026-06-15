package io.github.gourdoni.declension.scheduling;

import io.github.gourdoni.declension.domain.Revision;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SM2SchedulingStrategyTest {

    private final SchedulingStrategy strategy = new SM2SchedulingStrategy();
    private final LocalDate today = LocalDate.of(2026, 1, 1);

    @Test
    void firstSuccessfulRevisionSchedulesOneDayOut() {
        Revision result = strategy.schedule(Revision.unseen(10), RecallQuality.GOOD, today);
        assertEquals(1, result.repetitions());
        assertEquals(1, result.intervalDays());
        assertEquals(today.plusDays(1), result.dueDate());
        assertEquals(2.5, result.easeFactor(), 1e-9);
        assertEquals(10, result.inflectionID());
    }

    @Test
    void secondSuccessfulRevisionSchedulesSixDaysOut() {
        Revision first = strategy.schedule(Revision.unseen(20), RecallQuality.GOOD, today);
        Revision second = strategy.schedule(first, RecallQuality.GOOD, today);
        assertEquals(2, second.repetitions());
        assertEquals(6, second.intervalDays());
    }

    @Test
    void thirdRevisionMultipliesIntervalByEaseFactor() {
        Revision current = new Revision(1, 30, 6, 2.5, 2, today);
        Revision result = strategy.schedule(current, RecallQuality.GOOD, today);
        assertEquals(3, result.repetitions());
        assertEquals(15, result.intervalDays());
    }

    @Test
    void lapseResetsRepetitionsAndShrinksEaseFactor() {
        Revision revision = new Revision(1, 40, 15, 2.5, 3, today);
        Revision result = strategy.schedule(revision, RecallQuality.AGAIN, today);
        assertEquals(0, result.repetitions());
        assertEquals(1, result.intervalDays());
        assertEquals(1.96, result.easeFactor(), 1e-9);
    }

    @Test
    void easeFactorNeverLessThanFloor() {
        Revision revision = new Revision(1, 50, 10, 1.4, 5, today);
        Revision result = strategy.schedule(revision, RecallQuality.AGAIN, today);
        assertEquals(1.3, result.easeFactor(), 1e-9);
    }
}
