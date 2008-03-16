package sm;

import java.io.InputStream;

interface ResourceHandler {
    void handleStream(InputStream is, String url) throws InterruptedException;

    void setTotal(int total);
}
