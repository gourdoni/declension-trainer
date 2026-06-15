package io.github.gourdoni.declension.persistence;

import io.github.gourdoni.declension.domain.DataAccessException;
import io.github.gourdoni.declension.domain.Revision;
import io.github.gourdoni.declension.domain.RevisionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Optional;

public final class SQLiteRevisionRepository implements RevisionRepository {

    private final Database database;

    public SQLiteRevisionRepository(Database database) {
        this.database = database;
    }

    @Override
    public Revision save(Revision revision) {
        return revision.id() == 0 ? insert(revision) : update(revision);
    }

    private Revision insert(Revision revision) {
        String sql = "INSERT INTO revision (inflection_id, interval_days, ease_factor, repetitions, due_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            substituteEditableAttributes(statement, revision);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new Revision(keys.getLong(1),
                                    revision.inflectionID(),
                                    revision.intervalDays(),
                                    revision.easeFactor(),
                                    revision.repetitions(),
                                    revision.dueDate());
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not insert revision for inflection " + revision.inflectionID(), exception);
        }
    }

    private Revision update(Revision revision) {
        String sql = "UPDATE revision SET inflection_id = ?, interval_days = ?, ease_factor = ?, repetitions = ?, due_date = ? WHERE id = ?";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            substituteEditableAttributes(statement, revision);
            statement.setLong(6, revision.id());
            statement.executeUpdate();
            return revision;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not update revision ID " + revision.id(), exception);
        }
    }

    @Override
    public Optional<Revision> findByInflection(long inflectionID) {
        String sql = "SELECT id, inflection_id, interval_days, ease_factor, repetitions, due_date FROM revision WHERE inflection_id = ?";
        try (Connection connection = database.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, inflectionID);
            try (ResultSet queryResult = statement.executeQuery()) {
                return queryResult.next() ? Optional.of(readRecord(queryResult)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not find revision for inflection " + inflectionID, exception);
        }
    }

    private void substituteEditableAttributes(PreparedStatement statement, Revision revision) throws SQLException {
        statement.setLong(1, revision.inflectionID());
        statement.setInt(2, revision.intervalDays());
        statement.setDouble(3, revision.easeFactor());
        statement.setInt(4, revision.repetitions());
        setNullableDate(statement, 5, revision.dueDate());
    }

    private void setNullableDate(PreparedStatement statement, int index, LocalDate date) throws SQLException {
        if (date == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            // Uses ISO-8601, e.g. 2026-06-15.
            statement.setString(index, date.toString());
        }
    }

    private Revision readRecord(ResultSet queryResult) throws SQLException {
        String dueDate = queryResult.getString("due_date");
        return new Revision(queryResult.getLong("id"),
                            queryResult.getLong("inflection_id"),
                            queryResult.getInt("interval_days"),
                            queryResult.getDouble("ease_factor"),
                            queryResult.getInt("repetitions"),
                            dueDate == null ? null : LocalDate.parse(dueDate));
    }
}
