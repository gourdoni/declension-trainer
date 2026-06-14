package io.github.gourdoni.declension.domain;

import java.util.List;

public interface ReferenceRepository<T extends ReferenceValue> {
    T save(T value);

    // All values for given language in display.
    List<T> findByLanguage(long languageId);
}
