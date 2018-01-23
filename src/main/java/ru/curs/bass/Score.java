package ru.curs.bass;

import ru.curs.celesta.CelestaException;
import ru.curs.celesta.score.AbstractScore;
import ru.curs.celesta.score.discovery.ScoreDiscovery;

public class Score extends AbstractScore {

    public static final String SYSTEM_SCHEMA_NAME = "bass";

    public Score(String scorePath, ScoreDiscovery scoreDiscovery) throws CelestaException {
        super(scorePath, scoreDiscovery);
    }

    @Override
    public String getSysSchemaName() {
        return SYSTEM_SCHEMA_NAME;
    }
}
