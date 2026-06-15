package io.github.gourdoni.declension.domain;

/**
 * An inflection due for revision.
 */
public record DueInflection(long inflectionID, String nounCaseTitle, String nounNoTitle, String gloss, String spelling) {}
