package ru.curs.bass;

public class BassException extends RuntimeException {
    public BassException(Throwable cause) {
        super(cause);
    }

    public BassException(String message) {
        super(message);
    }
}
