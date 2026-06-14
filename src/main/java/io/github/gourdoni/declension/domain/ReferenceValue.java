package io.github.gourdoni.declension.domain;

// Any entity that the user defines (e.g. language, noun, noun case, etc.).
public interface ReferenceValue {
    long id();
    long languageId();
    String title();
}
