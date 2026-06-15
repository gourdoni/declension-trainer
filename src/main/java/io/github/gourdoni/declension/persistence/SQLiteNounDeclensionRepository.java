package io.github.gourdoni.declension.persistence;

import io.github.gourdoni.declension.domain.NounDeclension;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class SQLiteNounDeclensionRepository extends SQLiteReferenceRepository<NounDeclension> {

    public SQLiteNounDeclensionRepository(Database database) {
        super(database);
    }

    @Override
    protected String tableTitle() {
        return "noun_declension";
    }

    @Override
    protected List<String> editableAttributes() {
        return List.of("language_id", "title", "ordinal");
    }

    @Override
    protected void substituteEditableAttributes(PreparedStatement statement, NounDeclension nounDeclension) throws SQLException {
        statement.setLong(1, nounDeclension.languageId());
        statement.setString(2, nounDeclension.title());
        statement.setInt(3, nounDeclension.ordinal());
    }

    @Override
    protected NounDeclension readRecord(ResultSet queryResult) throws SQLException {
        return new NounDeclension(queryResult.getLong("id"),
                                  queryResult.getLong("language_id"),
                                  queryResult.getString("title"),
                                  queryResult.getInt("ordinal"));
    }

    @Override
    protected NounDeclension usingGeneratedID(NounDeclension nounDeclension, long generatedID) {
        return new NounDeclension(generatedID, nounDeclension.languageId(), nounDeclension.title(), nounDeclension.ordinal());
    }

    @Override
    protected String orderByAttribute() {
        return "ordinal";
    }
}
