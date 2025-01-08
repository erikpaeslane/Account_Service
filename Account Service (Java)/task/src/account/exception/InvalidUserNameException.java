package account.exception;

public class InvalidUserNameException extends RuntimeException {

    private final String message;

    public InvalidUserNameException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
