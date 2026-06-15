package io.github.gourdoni.declension.persistence;

import io.github.gourdoni.declension.domain.DataAccessException;
import io.github.gourdoni.declension.domain.Noun;
import io.github.gourdoni.declension.domain.NounRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SQLiteNounRepository implements NounRepository {

    private final Database database;

    public SQLiteNounRepository(Database database) {
        this.database = database;
    }

    @Override
    public Noun save(Noun noun) {
        return noun.id() == 0 ? insert(noun) : update(noun);
    }

    private Noun insert(Noun noun) {
        String sql = "INSERT INTO noun (language_id, gloss, gender_id, declension_id) VALUES (?, ?, ?, ?)";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            substituteEditableAttributes(statement, noun);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new Noun(keys.getLong(1), noun.languageID(), noun.genderID(), noun.declensionID(), noun.gloss());
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not insert noun: " + noun.gloss(), exception);
        }
    }

    private Noun update(Noun noun) {
        String sql = "UPDATE noun SET language_id = ?, gloss = ?, gender_id = ?, declension_id = ? WHERE id = ?";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            substituteEditableAttributes(statement, noun);
            statement.setLong(5, noun.id());
            statement.executeUpdate();
            return noun;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not update noun ID " + noun.id(), exception);
        }
    }

    @Override
    public Optional<Noun> findByID(long id) {
        String sql = "SELECT id, language_id, gloss, gender_id, declension_id FROM noun WHERE id = ?";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet queryResult = statement.executeQuery()) {
                return queryResult.next() ? Optional.of(readRecord(queryResult)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not find noun ID " + id, exception);
        }
    }

    @Override
    public List<Noun> findByLanguage(long languageID) {
        String sql = "SELECT id, language_id, gloss, gender_id, declension_id FROM noun WHERE language_id = ? ORDER BY id";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, languageID);
            try (ResultSet queryResult = statement.executeQuery()) {
                List<Noun> nouns = new ArrayList<>();
                while (queryResult.next()) {
                    nouns.add(readRecord(queryResult));
                }
                return nouns;
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not list nouns for language " + languageID, exception);
        }
    }

    private void substituteEditableAttributes(PreparedStatement statement, Noun noun) throws SQLException {
        statement.setLong(1, noun.languageID());
        statement.setString(2, noun.gloss()); // Sends `SQL NULL` if gloss is `null`.
        statement.setLong(3, noun.genderID());
        statement.setLong(4, noun.declensionID());
    }

    private Noun readRecord(ResultSet queryResult) throws SQLException {
        return new Noun(queryResult.getLong("id"),
                        queryResult.getLong("language_id"),
                        queryResult.getLong("gender_id"),
                        queryResult.getLong("declension_id"),
                        queryResult.getString("gloss"));
    }
}
