package ru.curs.bass;

/**
 * Bass runtime exception. All the instances of this exception are
 * caught in App.main, messages put out to user.
 */
public class BassException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BassException(Throwable cause) {
        super(cause);
    }

    public BassException(String message) {
        super(message);
    }
}
