package io.github.gourdoni.declension.domain;

import java.util.Optional;

public interface RevisionRepository {
    Revision save(Revision revision);

    // Find the state of revision for a given inflection.
    Optional<Revision> findByInflection(long inflectionID);
}
