package xyz.semio;

public class SessionException extends Exception {
  SessionException() {
    super();
  }
  SessionException(final String message) {
    super(message);
  }
  SessionException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
