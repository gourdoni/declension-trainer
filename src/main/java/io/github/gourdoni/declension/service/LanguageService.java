package io.github.gourdoni.declension.service;

import io.github.gourdoni.declension.domain.Language;
import io.github.gourdoni.declension.domain.LanguageRepository;
import io.github.gourdoni.declension.domain.NounCase;
import io.github.gourdoni.declension.domain.NounDeclension;
import io.github.gourdoni.declension.domain.NounGender;
import io.github.gourdoni.declension.domain.NounNo;
import io.github.gourdoni.declension.domain.ReferenceRepository;

import java.util.List;

public final class LanguageService {

    private final LanguageRepository languages;
    private final ReferenceRepository<NounCase> cases;
    private final ReferenceRepository<NounNo> numbers;
    private final ReferenceRepository<NounGender> genders;
    private final ReferenceRepository<NounDeclension> declensions;

    public LanguageService(LanguageRepository languages,
                           ReferenceRepository<NounCase> cases,
                           ReferenceRepository<NounNo> numbers,
                           ReferenceRepository<NounGender> genders,
                           ReferenceRepository<NounDeclension> declensions) {
        this.languages = languages;
        this.cases = cases;
        this.numbers = numbers;
        this.genders = genders;
        this.declensions = declensions;
    }

    public List<Language> all() {
        return languages.findAll();
    }

    public LanguageCategories categories(long languageID) {
        return new LanguageCategories(cases.findByLanguage(languageID),
                                      numbers.findByLanguage(languageID),
                                      genders.findByLanguage(languageID),
                                      declensions.findByLanguage(languageID));
    }

    public Language configure(LanguageDraft draft) {
        requireNonEmpty(draft.cases(), "case");
        requireNonEmpty(draft.numbers(), "number");
        requireNonEmpty(draft.genders(), "gender");
        requireNonEmpty(draft.declensions(), "declension");

        Language language = languages.save(Language.of(draft.title()));
        long languageID = language.id();

        long headCaseID = 0;
        int ordinal = 1;
        for (String title : draft.cases()) {
            long id = cases.save(NounCase.of(languageID, title, ordinal++, false)).id();
            if (headCaseID == 0) headCaseID = id;
        }
        long headNoID = 0;
        ordinal = 1;
        for (String title : draft.numbers()) {
            long id = numbers.save(NounNo.of(languageID, title, ordinal++)).id();
            if (headNoID == 0) headNoID = id;
        }
        for (String title : draft.genders()) {
            genders.save(NounGender.of(languageID, title));
        }
        ordinal = 1;
        for (String title : draft.declensions()) {
            declensions.save(NounDeclension.of(languageID, title, ordinal++));
        }
        return languages.save(language.usingHeader(headCaseID, headNoID));
    }

    private static void requireNonEmpty(List<String> values, String kind) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("A language needs at least one " + kind + ".");
        }
    }
}
