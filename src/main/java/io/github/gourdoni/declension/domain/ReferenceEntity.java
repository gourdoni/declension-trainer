package io.github.gourdoni.declension.domain;

/**
 * Any language-scoped entity (i.e constitutent of a language itself) that the user defines:
 * e.g. noun, noun case, noun gender, etc.
 */
public interface ReferenceEntity {
    long id();
    long languageId();
    String title();
}
