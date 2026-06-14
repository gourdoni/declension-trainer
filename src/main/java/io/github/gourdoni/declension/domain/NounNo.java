package io.github.gourdoni.declension.domain;

public record NounNo(long id, long languageId, String title, int ordinal) implements ReferenceEntity {

    public NounNo {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Noun no. cannot be blank or null!");
        }
    }

    public static NounNo of(long languageId, String title, int ordinal) {
        return new NounNo(0, languageId, title, ordinal);
    }
}
