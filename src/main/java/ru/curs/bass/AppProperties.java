package ru.curs.bass;

public class AppProperties {

    private String scorePath;
    private String jdbcUrl;
    private String jdbcUserName = "";
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
