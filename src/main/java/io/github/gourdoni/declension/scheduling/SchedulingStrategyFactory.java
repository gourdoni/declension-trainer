package io.github.gourdoni.declension.scheduling;

public final class SchedulingStrategyFactory {
    public SchedulingStrategy create(ActiveSchedulingStrategy activeStrategy) {
        return switch (activeStrategy) {
            case SM_2 -> new SM2SchedulingStrategy();
            case DOUBLING -> new DoublingSchedulingStrategy();
        };
    }
}
