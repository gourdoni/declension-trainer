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
                     SELECT i.id AS inflection_id, i.noun_id AS noun_id, c.title AS case_title,
                            num.title AS no_title, nn.gloss AS gloss, i.spelling AS spelling
                     FROM inflection i
                     JOIN noun nn ON nn.id = i.noun_id
                     JOIN noun_case c ON c.id = i.case_id
                     JOIN noun_no num ON num.id = i.no_id
                     LEFT JOIN revision r ON r.inflection_id = i.id
                     WHERE nn.language_id = ? AND (r.id IS NULL OR r.due_date <= ?)
                     ORDER BY (r.due_date IS NULL) DESC, r.due_date ASC, c.ordinal, num.ordinal
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
                                 queryResult.getLong("noun_id"),
                                 queryResult.getString("case_title"),
                                 queryResult.getString("no_title"),
                                 queryResult.getString("gloss"),
                                 queryResult.getString("spelling"));
    }
}
