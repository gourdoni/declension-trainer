package io.github.gourdoni.declension.persistence;

import io.github.gourdoni.declension.domain.DataAccessException;
import io.github.gourdoni.declension.domain.NounSortOrder;
import io.github.gourdoni.declension.domain.NounListEntry;
import io.github.gourdoni.declension.domain.NounListEntryQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class SQLiteNounListEntryQuery implements NounListEntryQuery {

    private final Database database;

    public SQLiteNounListEntryQuery(Database database) {
        this.database = database;
    }

    @Override
    public List<NounListEntry> forLanguage(long languageID, NounSortOrder sort) {
        String sql = """
                     SELECT n.id AS noun_id,
                            (SELECT hi.spelling FROM inflection hi
                               WHERE hi.noun_id = n.id AND hi.case_id = lang.head_case_id AND hi.no_id = lang.head_no_id) AS headword,
                            n.gloss AS gloss, g.title AS gender_title, d.title AS declension_title,
                            MIN(r.due_date) AS next_due,
                            SUM(CASE WHEN i.id IS NOT NULL AND r.id IS NULL THEN 1 ELSE 0 END) AS unseen_count
                     FROM noun n
                     JOIN language lang ON lang.id = n.language_id
                     JOIN noun_gender g ON g.id = n.gender_id
                     JOIN noun_declension d ON d.id = n.declension_id
                     LEFT JOIN inflection i ON i.noun_id = n.id
                     LEFT JOIN revision r ON r.inflection_id = i.id
                     WHERE n.language_id = ?
                     GROUP BY n.id
                     """ + orderClause(sort);
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, languageID);
            try (ResultSet queryResult = statement.executeQuery()) {
                List<NounListEntry> nounListEntries = new ArrayList<>();
                while (queryResult.next()) {
                    nounListEntries.add(readRecord(queryResult));
                }
                return nounListEntries;
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not list nouns for language " + languageID, exception);
        }
    }

    private String orderClause(NounSortOrder sort) {
        return switch (sort) {
            case ALPHABETICAL -> "ORDER BY headword IS NULL, headword COLLATE NOCASE";
            case DUE -> "ORDER BY (SUM(CASE WHEN i.id IS NOT NULL AND r.id IS NULL THEN 1 ELSE 0 END) > 0) DESC, MIN(r.due_date) IS NULL DESC, MIN(r.due_date)";
        };
    }

    private NounListEntry readRecord(ResultSet queryResult) throws SQLException {
        String nextDue = queryResult.getString("next_due");
        return new NounListEntry(queryResult.getLong("noun_id"),
                                 queryResult.getString("headword"),
                                 queryResult.getString("gloss"),
                                 queryResult.getString("gender_title"),
                                 queryResult.getString("declension_title"),
                                 nextDue == null ? null : LocalDate.parse(nextDue),
                                 queryResult.getInt("unseen_count"));
    }
}
