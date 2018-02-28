package ru.curs.bass;

import ru.curs.celesta.score.AbstractScore;
import ru.curs.celesta.score.validator.AnsiQuotedIdentifierValidator;
import ru.curs.celesta.score.validator.IdentifierValidator;

public class Score extends AbstractScore {

    public static final String SYSTEM_SCHEMA_NAME = "bass";

    private IdentifierValidator identifierValidator = new AnsiQuotedIdentifierValidator();

    public Score() {}

    @Override
    public IdentifierValidator getIdentifierValidator() {
        return identifierValidator;
    }

    @Override
    public String getSysSchemaName() {
        return SYSTEM_SCHEMA_NAME;
    }
}
