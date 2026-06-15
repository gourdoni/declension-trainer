package io.github.gourdoni.declension.domain;

/**
 * A language being studied (e.g. Classical Latin).
 * Languages define the categories used for deriving headwords for all nouns.
 * ID is zero until saved.
 */
public record Language(long id, String title, Long headCaseID, Long headNoID) {

    public Language {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Language cannot be blank or null!");
        }
    }

    // Unsaved language; headword inflection IDs are null until cases exist (boxed `long`s).
    public static Language of(String title) {
        return new Language(0, title, null, null);
    }

    public Language usingHeader(long headCaseID, long headNoID) {
        return new Language(this.id, this.title, headCaseID, headNoID);
    }
}
