package io.github.gourdoni.declension;

import io.github.gourdoni.declension.domain.Language;
import io.github.gourdoni.declension.domain.ReferenceEntity;
import io.github.gourdoni.declension.service.LanguageCategories;
import io.github.gourdoni.declension.service.LanguageDraft;
import io.github.gourdoni.declension.service.LanguageDraft.CaseDraft;
import io.github.gourdoni.declension.service.LanguageService;
import io.github.gourdoni.declension.service.NounDraft;
import io.github.gourdoni.declension.service.NounDraft.InflectionDraft;
import io.github.gourdoni.declension.service.NounService;

import java.util.ArrayList;
import java.util.List;

public final class SeedData {

    private SeedData() {}

    public static void seedIfEmpty(LanguageService languageService, NounService nounService) {
        if (!languageService.all().isEmpty()) {
            return;
        }
        seedLatin(languageService, nounService);
    }

    private static void seedLatin(LanguageService languageService, NounService nounService) {
        Language latin = languageService.configure(new LanguageDraft(
                "Latin",
                List.of(new CaseDraft("Nominative", false),
                        new CaseDraft("Genitive", false),
                        new CaseDraft("Dative", false),
                        new CaseDraft("Accusative", false),
                        new CaseDraft("Ablative", false),
                        new CaseDraft("Vocative", false),
                        new CaseDraft("Locative", true)), // Vestigial/optional locative.
                List.of("Singular", "Plural"),
                List.of("Masculine", "Feminine", "Neuter"),
                List.of("First", "Second", "Third", "Fourth", "Fifth")));
        LanguageCategories categories = languageService.categories(latin.id());
        addNoun(nounService, latin.id(), categories, "Feminine", "First", "girl",
                row("Nominative", "puella", "puellae"),
                row("Genitive", "puellae", "puellārum"),
                row("Dative", "puellae", "puellīs"),
                row("Accusative", "puellam", "puellās"),
                row("Ablative", "puellā", "puellīs"),
                row("Vocative", "puella", "puellae"));
        addNoun(nounService, latin.id(), categories, "Masculine", "Second", "master",
                row("Nominative", "dominus", "dominī"),
                row("Genitive", "dominī", "dominōrum"),
                row("Dative", "dominō", "dominīs"),
                row("Accusative", "dominum", "dominōs"),
                row("Ablative", "dominō", "dominīs"),
                row("Vocative", "domine", "dominī"));
        addNoun(nounService, latin.id(), categories, "Neuter", "Second", "war",
                row("Nominative", "bellum", "bella"),
                row("Genitive", "bellī", "bellōrum"),
                row("Dative", "bellō", "bellīs"),
                row("Accusative", "bellum", "bella"),
                row("Ablative", "bellō", "bellīs"),
                row("Vocative", "bellum", "bella"));
        addNoun(nounService, latin.id(), categories, "Masculine", "Third", "king",
                row("Nominative", "rēx", "rēgēs"),
                row("Genitive", "rēgis", "rēgum"),
                row("Dative", "rēgī", "rēgibus"),
                row("Accusative", "rēgem", "rēgēs"),
                row("Ablative", "rēge", "rēgibus"),
                row("Vocative", "rēx", "rēgēs"));
        addNoun(nounService, latin.id(), categories, "Feminine", "Fourth", "hand",
                row("Nominative", "manus", "manūs"),
                row("Genitive", "manūs", "manuum"),
                row("Dative", "manuī", "manibus"),
                row("Accusative", "manum", "manūs"),
                row("Ablative", "manū", "manibus"),
                row("Vocative", "manus", "manūs"));
        addNoun(nounService, latin.id(), categories, "Feminine", "Fifth", "thing",
                row("Nominative", "rēs", "rēs"),
                row("Genitive", "reī", "rērum"),
                row("Dative", "reī", "rēbus"),
                row("Accusative", "rem", "rēs"),
                row("Ablative", "rē", "rēbus"),
                row("Vocative", "rēs", "rēs"));
    }

    private static void addNoun(NounService nounService, long languageID, LanguageCategories categories,
                                String genderTitle, String declensionTitle, String gloss, Row... rows) {
        long singularID = idByTitle(categories.numbers(), "Singular");
        long pluralID = idByTitle(categories.numbers(), "Plural");
        List<InflectionDraft> inflections = new ArrayList<>();
        for (Row row : rows) {
            long caseID = idByTitle(categories.cases(), row.caseTitle());
            if (!row.singular().isBlank()) {
                inflections.add(new InflectionDraft(caseID, singularID, row.singular()));
            }
            if (!row.plural().isBlank()) {
                inflections.add(new InflectionDraft(caseID, pluralID, row.plural()));
            }
        }
        nounService.create(new NounDraft(languageID,
                idByTitle(categories.genders(), genderTitle),
                idByTitle(categories.declensions(), declensionTitle),
                gloss, inflections));
    }

    private static long idByTitle(List<? extends ReferenceEntity> entities, String title) {
        return entities.stream()
                       .filter(entity -> entity.title().equals(title))
                       .findFirst()
                       .orElseThrow(() -> new IllegalStateException("Missing category: " + title))
                       .id();
    }

    private static Row row(String caseTitle, String singular, String plural) {
        return new Row(caseTitle, singular, plural);
    }

    private record Row(String caseTitle, String singular, String plural) {
    }
}
