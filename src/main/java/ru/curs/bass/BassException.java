package ru.curs.bass;

/**
 * Bass runtime exception. All the instances of this exception are
 * catched in App.main, messages outputted to user.
 */
public class BassException extends RuntimeException {
    public BassException(Throwable cause) {
        super(cause);
    }

    public BassException(String message) {
        super(message);
    }
}
