package io.github.gourdoni.declension.scheduling;

/**
 * A user's self-assessed recall quality on the SM-2 0–5 scale.
 * A lapse (`AGAIN`) is anything less than 3; the others are passing grades.
 */
public enum RecallQuality {
    AGAIN(1),
    HARD(3),
    GOOD(4),
    EASY(5);

    private final int quality;

    RecallQuality(int quality) {
        this.quality = quality;
    }

    public int quality() {
        return quality;
    }
}
