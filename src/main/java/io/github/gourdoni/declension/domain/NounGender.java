package io.github.gourdoni.declension.domain;

public record NounGender(long id, long languageId, String title) implements ReferenceEntity {

    public NounGender {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Noun gender cannot be blank or null!");
        }
    }

    public static NounGender of(long languageId, String title) {
        return new NounGender(0, languageId, title);
    }
}
