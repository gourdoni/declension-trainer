package io.github.gourdoni.declension.domain;

import java.util.List;
import java.util.Optional;

public interface NounRepository {
    Noun save(Noun noun);
    Optional<Noun> findByID(long id);
    List<Noun> findByLanguage(long languageID);
}
