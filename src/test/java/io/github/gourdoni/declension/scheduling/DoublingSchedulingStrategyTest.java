package io.github.gourdoni.declension.scheduling;

import io.github.gourdoni.declension.domain.Revision;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DoublingSchedulingStrategyTest {

    private final SchedulingStrategy strategy = new DoublingSchedulingStrategy();
    private final LocalDate today = LocalDate.of(2026, 1, 1);

    @Test
    void firstSuccessStartsAtOneDay() {
        Revision result = strategy.schedule(Revision.unseen(10), RecallQuality.GOOD, today);
        assertEquals(1, result.intervalDays());
        assertEquals(1, result.repetitions());
        assertEquals(today.plusDays(1), result.dueDate());
    }

    @Test
    void successDoublesTheInterval() {
        Revision current = new Revision(1, 20, 4, Revision.DEFAULT_EASE_FACTOR, 3, today);
        Revision result = strategy.schedule(current, RecallQuality.GOOD, today);
        assertEquals(8, result.intervalDays());
    }

    @Test
    void lapseResetsToOneDay() {
        Revision current = new Revision(1, 30, 16, Revision.DEFAULT_EASE_FACTOR, 4, today);
        Revision result = strategy.schedule(current, RecallQuality.AGAIN, today);
        assertEquals(1, result.intervalDays());
        assertEquals(0, result.repetitions());
    }
}
