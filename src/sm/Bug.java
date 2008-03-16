package sm;

public class Bug extends RuntimeException {
    public Bug(String message) {
        super(message);    
    }

    public Bug(String message, Throwable cause) {
        super(message, cause);
    }
}
