package io.github.gourdoni.declension.domain;

public record NounCase(long id, long languageID, String title, int ordinal, boolean isOptional) implements ReferenceEntity {

    public NounCase {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Noun case cannot be blank or null!");
        }
    }

    public static NounCase of(long languageID, String title, int ordinal, boolean isOptional) {
        return new NounCase(0, languageID, title, ordinal, isOptional);
    }
}
