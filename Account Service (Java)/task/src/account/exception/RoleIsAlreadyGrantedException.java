package account.exception;

public class RoleIsAlreadyGrantedException extends RuntimeException {
    public RoleIsAlreadyGrantedException(String message) {
        super(message);
    }
}
