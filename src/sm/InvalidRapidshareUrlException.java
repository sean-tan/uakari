package sm;

public class InvalidRapidshareUrlException extends Exception {
    public InvalidRapidshareUrlException(String url, String cause) {
        super("Bad url '" + url + "', " + cause);
    }

    public InvalidRapidshareUrlException(String url, Exception cause) {
        super("Bad url '" + url + "'", cause);
    }
}
