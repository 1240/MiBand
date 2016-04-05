package ru.l240.miband;

public class GBException extends Exception {
    public GBException(String message, Throwable cause) {
        super(message, cause);
    }

    public GBException(String message) {
        super(message);
    }

    public GBException(Throwable cause) {
        super(cause);
    }

    public GBException() {
        super();
    }
}
