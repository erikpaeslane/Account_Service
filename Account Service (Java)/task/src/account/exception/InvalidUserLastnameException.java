package account.exception;

public class InvalidUserLastnameException extends RuntimeException {
    private final String message;

    public InvalidUserLastnameException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
