package sm;

import java.io.InputStream;

interface ResourceHandler {
    void handleStream(InputStream is, long startingByte) throws InterruptedException;

    void setTotal(int total);
}
