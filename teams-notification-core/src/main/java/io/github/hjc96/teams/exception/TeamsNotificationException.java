package io.github.hjc96.teams.exception;

public class TeamsNotificationException extends RuntimeException {

    public TeamsNotificationException(String message) {
        super(message);
    }

    public TeamsNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}