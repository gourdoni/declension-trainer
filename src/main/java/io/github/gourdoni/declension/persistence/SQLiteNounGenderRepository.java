package io.github.gourdoni.declension.persistence;

import io.github.gourdoni.declension.domain.NounGender;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class SQLiteNounGenderRepository extends SQLiteReferenceRepository<NounGender> {

    public SQLiteNounGenderRepository(Database database) {
        super(database);
    }

    @Override
    protected String tableTitle() {
        return "noun_gender";
    }

    @Override
    protected List<String> editableAttributes() {
        return List.of("language_id", "title");
    }

    @Override
    protected void substituteEditableAttributes(PreparedStatement statement, NounGender nounGender) throws SQLException {
        statement.setLong(1, nounGender.languageID());
        statement.setString(2, nounGender.title());
    }

    @Override
    protected NounGender readRecord(ResultSet queryResult) throws SQLException {
        return new NounGender(queryResult.getLong("id"),
                              queryResult.getLong("language_id"),
                              queryResult.getString("title"));
    }

    @Override
    protected NounGender usingGeneratedID(NounGender nounGender, long generatedID) {
        return new NounGender(generatedID, nounGender.languageID(), nounGender.title());
    }
}
