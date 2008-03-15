import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class Downloader implements Runnable {

    private final String url;
    private final File dir;
    private final Audit audit;
    private final RapidShareResourceFinder resourceFinder;
    private double byteCount = 0;
    private long currentRate;
    public int length;
    private boolean isDownloading;
    public static final int READ_TIME = 1;

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
            download();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            audit.addMessage(e);
        } finally {
            isDownloading = false;
        }
    }

    private void download() throws InvalidRapidshareUrlException, IOException, SAXException, InterruptedException {
        final File file = new File(dir, url.substring(url.lastIndexOf("/") + READ_TIME));
        long startingByte = file.length();
        byteCount = startingByte;
        resourceFinder.connect(url, startingByte, new ResourceHandler() {
            public void handleStream(int length, InputStream is, String url) throws InterruptedException {
                isDownloading = true;

                try {
                    Downloader.this.length = length;

                    FileWriter fileWriter = new FileWriter(file, true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    char[] chars = new char[1024 * 64];
                    while (byteCount < length) {
                        TimeUnit.SECONDS.sleep(READ_TIME);

                        int sizeRead = reader.read(chars);
                        currentRate = sizeRead;
                        if (sizeRead == -READ_TIME) {
                            audit.addMessage("problem downloading " + url + ", " + (long) byteCount + "bytes read out of " + (long) length);
                            break;
                        }
                        fileWriter.write(chars, 0, sizeRead);
                        byteCount += sizeRead;
                    }

                    fileWriter.flush();
                    fileWriter.close();
                    is.close();
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
}
