package io.github.gourdoni.declension.domain;

import java.time.LocalDate;

public record NounListEntry(long nounID,
                            String headword,
                            String gloss,
                            String genderTitle,
                            String declensionTitle,
                            LocalDate nextDue,
                            int unseenCount) {}