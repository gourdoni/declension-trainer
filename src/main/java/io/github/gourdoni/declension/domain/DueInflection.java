package io.github.gourdoni.declension.domain;

/**
 * An inflection due for revision.
 */
public record DueInflection(long inflectionID, long nounID, String nounCaseTitle, String nounNoTitle, String gloss, String spelling) {}
