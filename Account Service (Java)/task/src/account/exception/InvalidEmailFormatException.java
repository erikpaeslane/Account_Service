package account.exception;

public class InvalidEmailFormatException extends RuntimeException {

    private final String message;

    public InvalidEmailFormatException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
