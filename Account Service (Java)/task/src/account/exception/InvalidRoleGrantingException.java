package account.exception;

public class InvalidRoleGrantingException extends RuntimeException {
    public InvalidRoleGrantingException(String message) {
        super(message);
    }
}
