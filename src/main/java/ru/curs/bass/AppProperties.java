package ru.curs.bass;

import info.macias.kaconf.Property;

/**
 * Bass configuration, this object holds all the runtime options.
 */
public final class AppProperties {
    @Property("score.path")
    private String scorePath;
    @Property("jdbc.url")
    private String jdbcUrl;
    @Property("jdbc.username")
    private String jdbcUserName = "";
    @Property("jdbc.password")
    private String jdbcPassword = "";
    @Property("outputFilePath")
    private String filePath;
    @Property("debug")
    private boolean debug;

    private App.Command command;


    private void checkValue(Object value, String name) {
        if (value == null) {
            throw new BassException(String.format("%s parameter not provided", name));
        }
    }

    public String getScorePath() {
        checkValue(scorePath, "score.path");
        return scorePath;
    }

    public void setScorePath(String scorePath) {
        this.scorePath = scorePath;
    }

    public String getJdbcUrl() {
        checkValue(jdbcUrl, "jdbc.url");
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcUserName() {
        if (jdbcUserName == null) {
            return "";
        } else {
            return jdbcUserName;
        }
    }

    public void setJdbcUserName(String jdbcUserName) {
        this.jdbcUserName = jdbcUserName;
    }

    public String getJdbcPassword() {
        if (jdbcPassword == null) {
            return "";
        } else {
            return jdbcPassword;
        }
    }

    public void setJdbcPassword(String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public App.Command getCommand() {
        return command;
    }

    public void setCommand(App.Command cmd) {
        this.command = cmd;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
