package io.github.gourdoni.declension.domain;

/**
 * Any language-scoped entity (i.e constituent of a language itself) that the user defines:
 * e.g. noun case, noun gender, etc.
 */
public interface ReferenceEntity {
    long id();
    long languageID();
    String title();
}
