package io.github.gourdoni.declension.domain;

/**
 * An inflection of a noun; a single cell of its table.
 */
public record Inflection(long id, long nounID, long caseID, long noID, String spelling) {

    public Inflection {
        if (spelling == null || spelling.isBlank()) {
            throw new IllegalArgumentException("Inflection spelling cannot be blank or null!");
        }
    }

    public static Inflection of(long nounID, long caseID, long noID, String spelling) {
        return new Inflection(0, nounID, caseID, noID, spelling);
    }
}
