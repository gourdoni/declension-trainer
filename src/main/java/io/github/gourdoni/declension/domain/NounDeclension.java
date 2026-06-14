package io.github.gourdoni.declension.domain;

public record NounDeclension(long id, long languageId, String title, int ordinal) implements ReferenceEntity {

    public NounDeclension {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Noun declension cannot be blank");
        }
    }

    public static NounDeclension of(long languageId, String title, int ordinal) {
        return new NounDeclension(0, languageId, title, ordinal);
    }
}
