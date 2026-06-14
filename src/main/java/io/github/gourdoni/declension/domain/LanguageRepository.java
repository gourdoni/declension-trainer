package io.github.gourdoni.declension.domain;

import java.util.List;
import java.util.Optional;

public interface LanguageRepository {
    Language save(Language language);
    Optional<Language> findById(long id);
    List<Language> findAll();
}
