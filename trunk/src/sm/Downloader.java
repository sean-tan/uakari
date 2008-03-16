package sm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class Downloader {

    public static final int READ_TIME = 1;

    private final String url;
    private final File dir;
    private final RapidShareResourceFinder resourceFinder;
    private long byteCount = 0;
    private long currentRate;
    private long length;
    private boolean isDownloading;

    public Downloader(RapidShareResourceFinder resourceFinder, String url, File dir) {
        this.resourceFinder = resourceFinder;
        this.url = url;
        this.dir = dir;
    }

    public long getDownloadSize() {
        return length;
    }

    public long getDownloadedSoFar() {
        return byteCount;
    }

    public void download() throws InvalidRapidshareUrlException, IOException, InterruptedException {
        final File file = new File(dir, url.substring(url.lastIndexOf("/") + 1));
        long startingByte = file.length();
        byteCount = startingByte;
        
        resourceFinder.connect(url, startingByte, new ResourceHandler() {
            public void setTotal(int total) {
                length = total;
            }

            public void handleStream(InputStream is, String url) throws InterruptedException {
                isDownloading = true;

                try {

                    FileWriter fileWriter = new FileWriter(file, true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    char[] chars = new char[DownloadsTableModel.KILOBYTE * 128];
                    int sizeRead;
                    while ((sizeRead = reader.read(chars)) != -1) {
                        TimeUnit.SECONDS.sleep(READ_TIME);
                        currentRate = sizeRead;
                        fileWriter.write(chars, 0, sizeRead);
                        byteCount += sizeRead;
                    }

                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e) {
                    throw new Bug("Cannot write to " + file.getAbsolutePath(), e);
                } finally {
                    isDownloading = false;
                }
            }
        });
    }

    public long getCurrentRate() {
        return currentRate;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public boolean isComplete() {
        return length == byteCount;
    }
}
