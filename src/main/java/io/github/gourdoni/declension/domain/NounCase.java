package io.github.gourdoni.declension.domain;

public record NounCase(long id, long languageId, String title, int ordinal, boolean isOptional) implements ReferenceEntity {

    public NounCase {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Noun case cannot be blank");
        }
    }

    public static NounCase of(long languageId, String title, int ordinal, boolean isOptional) {
        return new NounCase(0, languageId, title, ordinal, isOptional);
    }
}
