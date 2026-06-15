package io.github.gourdoni.declension.persistence;

import io.github.gourdoni.declension.domain.NounCase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class SQLiteNounCaseRepository extends SQLiteReferenceRepository<NounCase> {

    public SQLiteNounCaseRepository(Database database) {
        super(database);
    }

    @Override
    protected String tableTitle() {
        return "noun_case";
    }

    @Override
    protected List<String> editableAttributes() {
        return List.of("language_id", "title", "ordinal", "is_optional");
    }

    @Override
    protected void substituteEditableAttributes(PreparedStatement statement, NounCase nounCase) throws SQLException {
        statement.setLong(1, nounCase.languageID());
        statement.setString(2, nounCase.title());
        statement.setInt(3, nounCase.ordinal());
        statement.setInt(4, nounCase.isOptional() ? 1 : 0);
    }

    @Override
    protected NounCase readRecord(ResultSet queryResult) throws SQLException {
        return new NounCase(queryResult.getLong("id"),
                            queryResult.getLong("language_id"),
                            queryResult.getString("title"),
                            queryResult.getInt("ordinal"),
                            queryResult.getInt("is_optional") != 0);
    }

    @Override
    protected NounCase usingGeneratedID(NounCase nounCase, long generatedID) {
        return new NounCase(generatedID,
                            nounCase.languageID(),
                            nounCase.title(),
                            nounCase.ordinal(),
                            nounCase.isOptional());
    }

    @Override
    protected String orderByAttribute() {
        return "ordinal";
    }
}
