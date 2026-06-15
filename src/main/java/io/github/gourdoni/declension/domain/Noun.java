package io.github.gourdoni.declension.domain;

/**
 * A noun belonging to a language, classified by gender and declension.
 * Gloss (translation into source language, probably English) is optional.
 */
public record Noun(long id, long languageID, long genderID, long declensionID, String gloss) {

    public static Noun of(long languageID, long genderID, long declensionID, String gloss) {
        return new Noun(0, languageID, genderID, declensionID, gloss);
    }
}
