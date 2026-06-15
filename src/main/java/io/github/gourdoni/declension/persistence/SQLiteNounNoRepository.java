package io.github.gourdoni.declension.persistence;

import io.github.gourdoni.declension.domain.NounNo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class SQLiteNounNoRepository extends SQLiteReferenceRepository<NounNo> {

    public SQLiteNounNoRepository(Database database) {
        super(database);
    }

    @Override
    protected String tableTitle() {
        return "noun_no";
    }

    @Override
    protected List<String> editableAttributes() {
        return List.of("language_id", "title", "ordinal");
    }

    @Override
    protected void substituteEditableAttributes(PreparedStatement statement, NounNo nounNo) throws SQLException {
        statement.setLong(1, nounNo.languageID());
        statement.setString(2, nounNo.title());
        statement.setInt(3, nounNo.ordinal());
    }

    @Override
    protected NounNo readRecord(ResultSet queryResult) throws SQLException {
        return new NounNo(queryResult.getLong("id"),
                          queryResult.getLong("language_id"),
                          queryResult.getString("title"),
                          queryResult.getInt("ordinal"));
    }

    @Override
    protected NounNo usingGeneratedID(NounNo nounNo, long generatedID) {
        return new NounNo(generatedID, nounNo.languageID(), nounNo.title(), nounNo.ordinal());
    }

    @Override
    protected String orderByAttribute() {
        return "ordinal";
    }
}
