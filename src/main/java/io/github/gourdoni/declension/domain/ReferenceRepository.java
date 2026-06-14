package io.github.gourdoni.declension.domain;

import java.util.List;

public interface ReferenceRepository<T extends ReferenceEntity> {
    T save(T value);

    // All entities for given language in display.
    List<T> findByLanguage(long languageId);
}
