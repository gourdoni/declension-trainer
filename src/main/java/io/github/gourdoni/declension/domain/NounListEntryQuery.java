package io.github.gourdoni.declension.domain;

import java.util.List;

public interface NounListEntryQuery {
    List<NounListEntry> forLanguage(long languageID, NounSortOrder sort);
}
