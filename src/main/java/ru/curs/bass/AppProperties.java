package ru.curs.bass;

import info.macias.kaconf.Property;

public class AppProperties {

    @Property("score.path")
    private String scorePath;
    @Property("jdbc.url")
    private String jdbcUrl;
    @Property("jdbc.username")
    private String jdbcUserName = "";
    @Property("jdbc.password")
    private String jdbcPassword = "";


    public String getScorePath() {
        return scorePath;
    }

    public void setScorePath(String scorePath) {
        this.scorePath = scorePath;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcUserName() {
        return jdbcUserName;
    }

    public void setJdbcUserName(String jdbcUserName) {
        this.jdbcUserName = jdbcUserName;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public void setJdbcPassword(String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
    }
}
