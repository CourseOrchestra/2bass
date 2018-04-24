package ru.curs.bass;

import ru.curs.celesta.score.AbstractScore;
import ru.curs.celesta.score.validator.AnsiQuotedIdentifierParser;
import ru.curs.celesta.score.validator.IdentifierParser;

public class Score extends AbstractScore {

    public static final String SYSTEM_SCHEMA_NAME = "bass";

    private IdentifierParser identifierParser = new AnsiQuotedIdentifierParser();

    @Override
    public IdentifierParser getIdentifierParser() {
        return identifierParser;
    }

    @Override
    public String getSysSchemaName() {
        return SYSTEM_SCHEMA_NAME;
    }
}
