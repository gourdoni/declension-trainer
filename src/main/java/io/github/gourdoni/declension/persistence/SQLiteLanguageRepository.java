package io.github.gourdoni.declension.persistence;

import io.github.gourdoni.declension.domain.DataAccessException;
import io.github.gourdoni.declension.domain.Language;
import io.github.gourdoni.declension.domain.LanguageRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SQLiteLanguageRepository implements LanguageRepository {

    private final Database database;

    public SQLiteLanguageRepository(Database database) {
        this.database = database;
    }

    @Override
    public Language save(Language language) {
        return language.id() == 0 ? insert(language) : update(language);
    }

    private Language insert(Language language) {
        String sql = "INSERT INTO language (title, head_case_id, head_no_id) VALUES (?, ?, ?)";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            substituteEditableAttributes(statement, language);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new Language(keys.getLong(1), language.title(), language.headCaseID(), language.headNoID());
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Failed to insert language: " + language.title(), exception);
        }
    }

    private Language update(Language language) {
        String sql = "UPDATE language SET title = ?, head_case_id = ?, head_no_id = ? WHERE id = ?";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            substituteEditableAttributes(statement, language);
            statement.setLong(4, language.id());
            statement.executeUpdate();
            return language;
        } catch (SQLException exception) {
            throw new DataAccessException("Failed to update language ID " + language.id(), exception);
        }
    }

    @Override
    public Optional<Language> findByID(long id) {
        String sql = "SELECT id, title, head_case_id, head_no_id FROM language WHERE id = ?";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet queryResult = statement.executeQuery()) {
                return queryResult.next() ? Optional.of(readRecord(queryResult)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not find language ID " + id, exception);
        }
    }

    @Override
    public List<Language> findAll() {
        String sql = "SELECT id, title, head_case_id, head_no_id FROM language ORDER BY title";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet queryResult = statement.executeQuery()) {
            List<Language> languages = new ArrayList<>();
            while (queryResult.next()) {
                languages.add(readRecord(queryResult));
            }
            return languages;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not list languages", exception);
        }
    }

    private void substituteEditableAttributes(PreparedStatement statement, Language language) throws SQLException {
        statement.setString(1, language.title());
        setNullableLong(statement, 2, language.headCaseID());
        setNullableLong(statement, 3, language.headNoID());
    }

    private void setNullableLong(PreparedStatement statement, int index, Long value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setLong(index, value);
        }
    }

    private Language readRecord(ResultSet queryResult) throws SQLException {
        return new Language(queryResult.getLong("id"), queryResult.getString("title"), getNullableLong(queryResult, "head_case_id"), getNullableLong(queryResult, "head_no_id"));
    }

    private Long getNullableLong(ResultSet queryResult, String column) throws SQLException {
        long value = queryResult.getLong(column);
        return queryResult.wasNull() ? null : value;
    }
}
