package io.github.gourdoni.declension.domain;

public record NounDeclension(long id, long languageID, String title, int ordinal) implements ReferenceEntity {

    public NounDeclension {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Noun declension cannot be blank or null!");
        }
    }

    public static NounDeclension of(long languageID, String title, int ordinal) {
        return new NounDeclension(0, languageID, title, ordinal);
    }
}
