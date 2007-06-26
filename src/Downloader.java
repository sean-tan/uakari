import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Downloader implements Runnable {

    private final String url;
    private final File dir;
    private final Audit audit;
    private final RapidShareResourceFinder resourceFinder;
    private double byteCount = 0;
    private long currentRate;
    public int length;
    private boolean stopped = true;

    public Downloader(RapidShareResourceFinder resourceFinder, String url, File dir, Audit audit) {
        this.resourceFinder = resourceFinder;
        this.url = url;
        this.dir = dir;
        this.audit = audit;
    }

    public long getDownloadSize() {
        return (long) length;
    }

    public long getDownloadedSoFar() {
        return (long) byteCount;
    }

    public void run() {
        try {
            this.stopped = false;
            download();
        } catch (Exception e) {
            audit.addMessage(e);
        }
    }

    private void download() throws Exception {
        resourceFinder.connect(url, new ResourceHandler() {
            public void handleStream(int length, InputStream is, String url) throws IOException {

                File file = new File(dir, url.substring(url.lastIndexOf("/") + 1));
                Downloader.this.length = length;
                byteCount = 0;
                if (file.exists() && file.length() == length) {
                    byteCount = length;
                    return;
                } else if (file.exists()) {
                    file.delete();
                }

                FileWriter fileWriter = new FileWriter(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                char[] chars = new char[1024 * 512];
                while (byteCount < length && !stopped) {
                    int sizeRead = reader.read(chars);
                    if (sizeRead == -1) {
                        audit.addMessage("problem downloading " + url + ", " + (long) byteCount + "bytes read out of " + (long) length);
                        break;
                    }
                    fileWriter.write(chars, 0, sizeRead);
                    byteCount += sizeRead;
                }

                fileWriter.flush();
                fileWriter.close();
                is.close();
            }
        });
    }

    public void setCurrentRate(long currentRate) {
        this.currentRate = currentRate;
    }

    public long getCurrentRate() {
        return currentRate;
    }

    public void stop() {
        this.stopped = true;
    }

    public boolean hasStarted() {
        return !stopped;
    }
}
