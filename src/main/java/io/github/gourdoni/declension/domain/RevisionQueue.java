package io.github.gourdoni.declension.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * Query interface for inflections due for revision; either unseen, or interval elapsed.
 */
public interface RevisionQueue {
    List<DueInflection> dueForLanguage(long languageID, LocalDate onDate);
}
