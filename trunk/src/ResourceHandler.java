import java.io.InputStream;

interface ResourceHandler {
    void handleStream(int length, InputStream is, String url) throws InterruptedException;
}
