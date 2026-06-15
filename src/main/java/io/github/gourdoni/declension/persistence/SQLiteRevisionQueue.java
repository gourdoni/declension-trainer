package io.github.gourdoni.declension.persistence;

import io.github.gourdoni.declension.domain.DataAccessException;
import io.github.gourdoni.declension.domain.DueInflection;
import io.github.gourdoni.declension.domain.RevisionQueue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class SQLiteRevisionQueue implements RevisionQueue {

    private final Database database;

    public SQLiteRevisionQueue(Database database) {
        this.database = database;
    }

    @Override
    public List<DueInflection> dueForLanguage(long languageID, LocalDate onDate) {
        String sql = """
                     SELECT
                         inflection.id AS inflection_id,
                         noun_case.title AS case_title,
                         noun_number.title AS number_title,
                         noun.gloss AS gloss,
                         inflection.spelling AS spelling
                     FROM inflection
                     JOIN noun
                         ON noun.id = inflection.noun_id
                     JOIN noun_case
                         ON noun_case.id = inflection.case_id
                     JOIN noun_no AS noun_number
                         ON noun_number.id = inflection.no_id
                     LEFT JOIN revision
                         ON revision.inflection_id = inflection.id
                     WHERE noun.language_id = ?
                       AND (revision.id IS NULL OR revision.due_date <= ?)
                     ORDER BY
                         (revision.due_date IS NULL) DESC,
                         revision.due_date ASC,
                         noun_case.ordinal,
                         noun_number.ordinal
                     """;
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, languageID);
            statement.setString(2, onDate.toString());
            try (ResultSet queryResult = statement.executeQuery()) {
                List<DueInflection> due = new ArrayList<>();
                while (queryResult.next()) {
                    due.add(readRecord(queryResult));
                }
                return due;
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not list inflections due for language " + languageID, exception);
        }
    }

    private DueInflection readRecord(ResultSet queryResult) throws SQLException {
        return new DueInflection(queryResult.getLong("inflection_id"),
                                 queryResult.getString("case_title"),
                                 queryResult.getString("no_title"),
                                 queryResult.getString("gloss"),
                                 queryResult.getString("spelling"));
    }
}
