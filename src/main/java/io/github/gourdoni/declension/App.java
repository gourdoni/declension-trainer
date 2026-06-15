package io.github.gourdoni.declension;

import com.sun.net.httpserver.HttpServer;

import io.github.gourdoni.declension.web.APIServer;

import io.github.gourdoni.declension.domain.Language;
import io.github.gourdoni.declension.domain.LanguageRepository;
import io.github.gourdoni.declension.domain.Noun;
import io.github.gourdoni.declension.domain.NounRepository;
import io.github.gourdoni.declension.domain.Inflection;
import io.github.gourdoni.declension.domain.InflectionRepository;
import io.github.gourdoni.declension.domain.ReferenceEntity;
import io.github.gourdoni.declension.domain.NounCase;
import io.github.gourdoni.declension.domain.NounNo;
import io.github.gourdoni.declension.domain.NounGender;
import io.github.gourdoni.declension.domain.NounDeclension;
import io.github.gourdoni.declension.domain.ReferenceRepository;

import io.github.gourdoni.declension.domain.NounListEntryQuery;
import io.github.gourdoni.declension.domain.RevisionQueue;
import io.github.gourdoni.declension.domain.RevisionRepository;

import io.github.gourdoni.declension.persistence.SQLiteNounListEntryQuery;
import io.github.gourdoni.declension.persistence.SQLiteRevisionQueue;
import io.github.gourdoni.declension.persistence.SQLiteRevisionRepository;

import io.github.gourdoni.declension.service.NounService;
import io.github.gourdoni.declension.service.RevisionService;

import io.github.gourdoni.declension.scheduling.SchedulingStrategy;
import io.github.gourdoni.declension.scheduling.SchedulingStrategyFactory;
import io.github.gourdoni.declension.scheduling.ActiveSchedulingStrategy;

import io.github.gourdoni.declension.persistence.Database;
import io.github.gourdoni.declension.persistence.SchemaInitialiser;
import io.github.gourdoni.declension.persistence.SQLiteNounRepository;
import io.github.gourdoni.declension.persistence.SQLiteInflectionRepository;
import io.github.gourdoni.declension.persistence.SQLiteLanguageRepository;
import io.github.gourdoni.declension.persistence.SQLiteNounCaseRepository;
import io.github.gourdoni.declension.persistence.SQLiteNounNoRepository;
import io.github.gourdoni.declension.persistence.SQLiteNounGenderRepository;
import io.github.gourdoni.declension.persistence.SQLiteNounDeclensionRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public class App {
    public static void main(String[] args) throws IOException, SQLException {
        Path databaseFile = Path.of("data", "declensions.db");
        // Create database and entities if not already configured on prior invocation.
        Database database = new Database(databaseFile);
        SchemaInitialiser initialiser = new SchemaInitialiser(database);
        initialiser.createEntities();
        System.out.println("Connected to database and created tables: " + databaseFile);

        LanguageRepository languageRepository = new SQLiteLanguageRepository(database);
        ReferenceRepository<NounCase> caseRepository = new SQLiteNounCaseRepository(database);
        ReferenceRepository<NounNo> nounNoRepository = new SQLiteNounNoRepository(database);
        ReferenceRepository<NounGender> genderRepository = new SQLiteNounGenderRepository(database);
        ReferenceRepository<NounDeclension> declensionRepository = new SQLiteNounDeclensionRepository(database);
        NounRepository nounRepository = new SQLiteNounRepository(database);
        InflectionRepository inflectionRepository = new SQLiteInflectionRepository(database);
        seedDevData(languageRepository, caseRepository, nounNoRepository, genderRepository, declensionRepository, nounRepository, inflectionRepository);

        NounListEntryQuery nounListEntryQuery = new SQLiteNounListEntryQuery(database);
        RevisionQueue revisionQueue = new SQLiteRevisionQueue(database);
        RevisionRepository revisionRepository = new SQLiteRevisionRepository(database);

        NounService nounService = new NounService(nounRepository, inflectionRepository);
        SchedulingStrategy schedulingStrategy = new SchedulingStrategyFactory().create(ActiveSchedulingStrategy.SM_2);
        RevisionService revisionService = new RevisionService(revisionRepository, inflectionRepository, schedulingStrategy);

        // Create and execute HTTP server.
        APIServer server = new APIServer(8080, languageRepository, nounListEntryQuery, nounService, revisionQueue, revisionService);
        server.start();
        System.out.println("Server running: http://localhost:8080");
    }

    // Delete later; UI should supply this data.
    private static void seedDevData(LanguageRepository languages,
                                    ReferenceRepository<NounCase> nounCases,
                                    ReferenceRepository<NounNo> nounNos,
                                    ReferenceRepository<NounGender> nounGenders,
                                    ReferenceRepository<NounDeclension> nounDeclensions,
                                    NounRepository nouns,
                                    InflectionRepository inflections) {
        long latinID = languages.findAll().isEmpty()
                ? languages.save(Language.of("Latin")).id()
                : languages.findAll().get(0).id();
        if (nounCases.findByLanguage(latinID).isEmpty()) {
            int ordinal = 1;
            nounCases.save(NounCase.of(latinID, "Nominative", ordinal++, false));
            nounCases.save(NounCase.of(latinID, "Genitive", ordinal++, false));
            nounCases.save(NounCase.of(latinID, "Dative", ordinal++, false));
            nounCases.save(NounCase.of(latinID, "Accusative", ordinal++, false));
            nounCases.save(NounCase.of(latinID, "Ablative", ordinal++, false));
            nounCases.save(NounCase.of(latinID, "Vocative", ordinal++, false));
            // The locative case is vestigial in Classical Latin, and therefore optional.
            nounCases.save(NounCase.of(latinID, "Locative", ordinal++, true));
        }
        if (nounNos.findByLanguage(latinID).isEmpty()) {
            nounNos.save(NounNo.of(latinID, "Singular", 1));
            nounNos.save(NounNo.of(latinID, "Plural", 2));
        }
        if (nounGenders.findByLanguage(latinID).isEmpty()) {
            nounGenders.save(NounGender.of(latinID, "Masculine"));
            nounGenders.save(NounGender.of(latinID, "Feminine"));
            nounGenders.save(NounGender.of(latinID, "Neuter"));
        }
        if (nounDeclensions.findByLanguage(latinID).isEmpty()) {
            nounDeclensions.save(NounDeclension.of(latinID, "First", 1));
            nounDeclensions.save(NounDeclension.of(latinID, "Second", 2));
            nounDeclensions.save(NounDeclension.of(latinID, "Third", 3));
            nounDeclensions.save(NounDeclension.of(latinID, "Fourth", 4));
            nounDeclensions.save(NounDeclension.of(latinID, "Fifth", 5));
        }
        Language latin = languages.findByID(latinID).orElseThrow();
        if (latin.headCaseID() == null) {
            languages.save(latin.usingHeader(findIDByTitle(nounCases.findByLanguage(latinID), "Nominative"),
                                              findIDByTitle(nounNos.findByLanguage(latinID), "Singular")));
        }
        if (nouns.findByLanguage(latinID).isEmpty()) {
            long feminineID = findIDByTitle(nounGenders.findByLanguage(latinID), "Feminine");
            long firstDeclensionID = findIDByTitle(nounDeclensions.findByLanguage(latinID), "First");
            long puellaID = nouns.save(Noun.of(latinID, feminineID, firstDeclensionID, "girl")).id();
            long singularID = findIDByTitle(nounNos.findByLanguage(latinID), "Singular");
            List<NounCase> cases = nounCases.findByLanguage(latinID);
            inflections.save(Inflection.of(puellaID, findIDByTitle(cases, "Nominative"), singularID, "puella"));
            inflections.save(Inflection.of(puellaID, findIDByTitle(cases, "Genitive"),   singularID, "puellae"));
            inflections.save(Inflection.of(puellaID, findIDByTitle(cases, "Dative"),     singularID, "puellae"));
            inflections.save(Inflection.of(puellaID, findIDByTitle(cases, "Accusative"), singularID, "puellam"));
            inflections.save(Inflection.of(puellaID, findIDByTitle(cases, "Ablative"),   singularID, "puellā"));
            inflections.save(Inflection.of(puellaID, findIDByTitle(cases, "Vocative"),   singularID, "puella"));
            // Note: no locative!
        }
        System.out.println("Latin nouns: " + nouns.findByLanguage(latinID));
        nouns.findByLanguage(latinID).forEach(noun -> System.out.println("Inflections for noun " + noun.id() + ": " + inflections.findByNoun(noun.id())));
        System.out.println("Latin cases: " + nounCases.findByLanguage(latinID));
        System.out.println("Latin nos: " + nounNos.findByLanguage(latinID));
        System.out.println("Latin genders: " + nounGenders.findByLanguage(latinID));
        System.out.println("Latin declensions: " + nounDeclensions.findByLanguage(latinID));
    }

    private static long findIDByTitle(List<? extends ReferenceEntity> entities, String title) {
        return entities.stream()
                .filter(entity -> entity.title().equals(title))
                .findFirst()
                .orElseThrow()
                .id();
    }
}
