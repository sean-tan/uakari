package sm;

import static sm.DownloadsColumnModel.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Downloader {

    public static final int READ_TIME = 1;

    private final String url;
    private final File dir;
    private final RapidShareResourceFinder resourceFinder;
    private final AtomicLong byteCount = new AtomicLong(0);
    private boolean isDownloading;
    private long currentRate;
    private RandomAccessFile output;

    public Downloader(ScheduledExecutorService scheduledExecutorService, RapidShareResourceFinder resourceFinder, String url, File dir) {
        this.resourceFinder = resourceFinder;
        this.url = url;
        this.dir = dir;

        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            private long lastRead = 0;

            public void run() {
                long total = getDownloadedSoFar();
                currentRate = total - lastRead;
                lastRead = total;
            }
        }, 0, READ_TIME, TimeUnit.SECONDS);
    }

    public long getDownloadSize() {
        try {
            return output == null ? 0 : output.length();
        } catch (IOException e) {
            throw new Bug("Should not happend");
        }
    }

    public long getDownloadedSoFar() {
        return byteCount.longValue();
    }

    public void download() throws InvalidRapidshareUrlException, IOException, InterruptedException {
        final File file = new File(dir, filenameFromUrl(url));
        file.delete();
        if (file.exists())
            throw new Bug("Cannot delete " + file);

        output = new RandomAccessFile(file, "rw");

        int numberOfConcurrentConnections = 4;
        ResourceHandler[] handlers = new  ResourceHandler[numberOfConcurrentConnections];
        CountDownLatch latch = new CountDownLatch(handlers.length);
        for (int i = 0; i < handlers.length; i++) {
            handlers[i] = new FileSaveResourceHandler(output, latch);
        }

        isDownloading = true;
        resourceFinder.connect(url, handlers);
        latch.await();
        isDownloading = false;
    }

    public long getCurrentRate() {
        return currentRate;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public boolean isComplete() {
        try {
            return output != null && output.length() > 0 && output.length() == byteCount.longValue();
        } catch (IOException e) {
            throw new Bug("Shouldn't happen", e);
        }
    }

    public void stop() {
        resourceFinder.cancel();
    }

    private class FileSaveResourceHandler implements ResourceHandler {
        private final RandomAccessFile file;
        private final CountDownLatch latch;

        public FileSaveResourceHandler(RandomAccessFile file, CountDownLatch latch) {
            this.file = file;
            this.latch = latch;
        }

        public void setTotal(int total) {
            try {
                synchronized (file) {
                    file.setLength(total);
                }
            } catch (IOException e) {
                throw new Bug("Cannot set file size", e);
            }
        }

        public void handleStream(InputStream is, long startingByte) throws InterruptedException {
            try {
                long filePointer = startingByte;
                byte[] chars = new byte[DownloadsTableModel.KILOBYTE * 128];
                int sizeRead;
                while ((sizeRead = is.read(chars)) != -1) {
                    TimeUnit.SECONDS.sleep(READ_TIME);
                    synchronized (file) {
                        file.seek(filePointer);
                        file.write(chars, 0, sizeRead);
                        filePointer += sizeRead;
                    }
                    byteCount.addAndGet(sizeRead);
                }
            } catch (IOException e) {
                throw new Bug("Cannot write to " + file, e);
            } finally {
                latch.countDown();
            }
        }
    }
}
