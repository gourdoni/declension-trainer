package io.github.gourdoni.declension.persistence;

import io.github.gourdoni.declension.domain.DataAccessException;
import io.github.gourdoni.declension.domain.Inflection;
import io.github.gourdoni.declension.domain.InflectionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SQLiteInflectionRepository implements InflectionRepository {

    private final Database database;

    public SQLiteInflectionRepository(Database database) {
        this.database = database;
    }

    @Override
    public Inflection save(Inflection inflection) {
        return inflection.id() == 0 ? insert(inflection) : update(inflection);
    }

    private Inflection insert(Inflection inflection) {
        String sql = "INSERT INTO inflection (noun_id, case_id, no_id, spelling) VALUES (?, ?, ?, ?)";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            substituteEditableAttributes(statement, inflection);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new Inflection(keys.getLong(1), inflection.nounID(), inflection.caseID(), inflection.noID(), inflection.spelling());
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not insert inflection: " + inflection.spelling(), exception);
        }
    }

    private Inflection update(Inflection inflection) {
        String sql = "UPDATE inflection SET noun_id = ?, case_id = ?, no_id = ?, spelling = ? WHERE id = ?";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            substituteEditableAttributes(statement, inflection);
            statement.setLong(5, inflection.id());
            statement.executeUpdate();
            return inflection;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not update inflection ID " + inflection.id(), exception);
        }
    }

    @Override
    public Optional<Inflection> findByID(long id) {
        String sql = "SELECT id, noun_id, case_id, no_id, spelling FROM inflection WHERE id = ?";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet queryResult = statement.executeQuery()) {
                return queryResult.next() ? Optional.of(readRecord(queryResult)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not find inflection ID " + id, exception);
        }
    }

    @Override
    public List<Inflection> findByNoun(long nounID) {
        String sql = "SELECT id, noun_id, case_id, no_id, spelling FROM inflection WHERE noun_id = ? ORDER BY id";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, nounID);
            try (ResultSet queryResult = statement.executeQuery()) {
                List<Inflection> inflections = new ArrayList<>();
                while (queryResult.next()) {
                    inflections.add(readRecord(queryResult));
                }
                return inflections;
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not list inflections for noun " + nounID, exception);
        }
    }

    private void substituteEditableAttributes(PreparedStatement statement, Inflection inflection) throws SQLException {
        statement.setLong(1, inflection.nounID());
        statement.setLong(2, inflection.caseID());
        statement.setLong(3, inflection.noID());
        statement.setString(4, inflection.spelling());
    }

    private Inflection readRecord(ResultSet queryResult) throws SQLException {
        return new Inflection(queryResult.getLong("id"),
                              queryResult.getLong("noun_id"),
                              queryResult.getLong("case_id"),
                              queryResult.getLong("no_id"),
                              queryResult.getString("spelling"));
    }
}
