package io.github.gourdoni.declension;

import io.github.gourdoni.declension.domain.InflectionRepository;
import io.github.gourdoni.declension.domain.LanguageRepository;
import io.github.gourdoni.declension.domain.NounCase;
import io.github.gourdoni.declension.domain.NounDeclension;
import io.github.gourdoni.declension.domain.NounGender;
import io.github.gourdoni.declension.domain.NounListEntryQuery;
import io.github.gourdoni.declension.domain.NounNo;
import io.github.gourdoni.declension.domain.NounRepository;
import io.github.gourdoni.declension.domain.ReferenceRepository;
import io.github.gourdoni.declension.domain.RevisionQueue;
import io.github.gourdoni.declension.domain.RevisionRepository;
import io.github.gourdoni.declension.persistence.Database;
import io.github.gourdoni.declension.persistence.SQLiteInflectionRepository;
import io.github.gourdoni.declension.persistence.SQLiteLanguageRepository;
import io.github.gourdoni.declension.persistence.SQLiteNounCaseRepository;
import io.github.gourdoni.declension.persistence.SQLiteNounDeclensionRepository;
import io.github.gourdoni.declension.persistence.SQLiteNounGenderRepository;
import io.github.gourdoni.declension.persistence.SQLiteNounListEntryQuery;
import io.github.gourdoni.declension.persistence.SQLiteNounNoRepository;
import io.github.gourdoni.declension.persistence.SQLiteNounRepository;
import io.github.gourdoni.declension.persistence.SQLiteRevisionQueue;
import io.github.gourdoni.declension.persistence.SQLiteRevisionRepository;
import io.github.gourdoni.declension.persistence.SchemaInitialiser;
import io.github.gourdoni.declension.scheduling.ActiveSchedulingStrategy;
import io.github.gourdoni.declension.scheduling.SchedulingStrategy;
import io.github.gourdoni.declension.scheduling.SchedulingStrategyFactory;
import io.github.gourdoni.declension.service.LanguageService;
import io.github.gourdoni.declension.service.NounService;
import io.github.gourdoni.declension.service.RevisionService;
import io.github.gourdoni.declension.web.APIServer;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

public final class App {

    private static final int PORT = 8080;
    private static final Path DATABASE_FILE = Path.of("data", "declensions.db");

    private App() {
    }

    public static void main(String[] args) throws IOException, SQLException {
        Database database = new Database(DATABASE_FILE);
        new SchemaInitialiser(database).createEntities();

        LanguageRepository languageRepository = new SQLiteLanguageRepository(database);
        ReferenceRepository<NounCase> caseRepository = new SQLiteNounCaseRepository(database);
        ReferenceRepository<NounNo> nounNoRepository = new SQLiteNounNoRepository(database);
        ReferenceRepository<NounGender> genderRepository = new SQLiteNounGenderRepository(database);
        ReferenceRepository<NounDeclension> declensionRepository = new SQLiteNounDeclensionRepository(database);
        NounRepository nounRepository = new SQLiteNounRepository(database);
        InflectionRepository inflectionRepository = new SQLiteInflectionRepository(database);

        NounListEntryQuery nounListEntryQuery = new SQLiteNounListEntryQuery(database);
        RevisionQueue revisionQueue = new SQLiteRevisionQueue(database);
        RevisionRepository revisionRepository = new SQLiteRevisionRepository(database);

        LanguageService languageService = new LanguageService(
                languageRepository, caseRepository, nounNoRepository, genderRepository, declensionRepository);
        NounService nounService = new NounService(nounRepository, inflectionRepository);
        SchedulingStrategy schedulingStrategy = new SchedulingStrategyFactory().create(ActiveSchedulingStrategy.SM_2);
        RevisionService revisionService = new RevisionService(revisionRepository, schedulingStrategy);

        SeedData.seedIfEmpty(languageService, nounService);

        APIServer server = new APIServer(PORT, languageService, nounListEntryQuery, nounService, revisionQueue, revisionService);
        server.start();
        System.out.println("Running at http://localhost:" + PORT);
        System.out.println("Press Ctrl+C to stop.");
    }
}
