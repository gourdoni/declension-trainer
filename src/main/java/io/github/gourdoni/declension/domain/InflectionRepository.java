package io.github.gourdoni.declension.domain;

import java.util.List;
import java.util.Optional;

public interface InflectionRepository {
    Inflection save(Inflection inflection);
    Optional<Inflection> findByID(long id);
    List<Inflection> findByNoun(long nounID);
}
