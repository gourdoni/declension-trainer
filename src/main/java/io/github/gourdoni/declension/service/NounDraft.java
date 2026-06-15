package io.github.gourdoni.declension.service;

import java.util.List;

public record NounDraft(long languageID, long genderID, long declensionID, String gloss, List<InflectionDraft> inflections) {
    public record InflectionDraft(long caseID, long noID, String spelling) {}
}
