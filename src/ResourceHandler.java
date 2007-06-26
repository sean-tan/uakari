import java.io.InputStream;
import java.io.IOException;

interface ResourceHandler {
    void handleStream(int length, InputStream is, String url) throws IOException;
}
