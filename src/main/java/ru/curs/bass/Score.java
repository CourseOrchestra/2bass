package ru.curs.bass;

import ru.curs.celesta.score.AbstractScore;

public class Score extends AbstractScore {

    public static final String SYSTEM_SCHEMA_NAME = "bass";

    public Score() {}

    @Override
    public String getSysSchemaName() {
        return SYSTEM_SCHEMA_NAME;
    }
}
