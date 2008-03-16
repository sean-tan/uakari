package sm;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

class DownloadService {
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final Map<String, Future> tasks = new ConcurrentHashMap<String, Future>();
    private final Map<String, Downloader> downloaders = new ConcurrentHashMap<String, Downloader>();
    private final ScheduledExecutorService scheduledExecutorService;
    private final Settings settings;
    private final Audit audit;

    public DownloadService(ScheduledExecutorService scheduledExecutorService, Settings settings, Audit audit) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.settings = settings;
        this.audit = audit;
    }

    public Downloader getDownloader(String url) {
        return downloaders.get(url);
    }

    public void startDownloading(final String url) {
        Future future = tasks.get(url);
        if (future == null) {
            RapidShareResourceFinder finder = new RapidShareResourceFinder(scheduledExecutorService, settings, audit);
            final Downloader downloader = new Downloader(scheduledExecutorService, finder, url, settings.getDownloadPath());
            downloaders.put(url, downloader);
            future = executorService.submit(new Runnable() {
                public void run() {
                    try {
                        downloader.download();
                    } catch (InterruptedException e) {
                        downloader.stop();
                    } catch (Exception e) {
                        audit.addMessage(e);
                    } finally {
                        tasks.remove(url);
                    }
                }
            });
            tasks.put(url, future);
        }
    }

    public void stopDownloading(String url) {
        Future future = tasks.get(url);
        if (future != null)
            future.cancel(true);
    }

    public void stop() {
        executorService.shutdownNow();
    }

    public static void main(String[] args) throws Exception {
        String name = "foo.txt";
        new File(name).delete();

        RandomAccessFile file = new RandomAccessFile(name, "rw");
        file.setLength(40);
        file.seek(5);
        file.writeBytes("hello");
        file.seek(20);
        file.writeChars("world");
        file.close();


        RandomAccessFile file2 = new RandomAccessFile("/Users/nickpomfret/Lesbian_Bridal_Stories_vol_1.part03.rar.html", "r");
        int i;
        int count = 0;
        while((i = file2.read()) != -1) {
            System.out.println(count++ + ": " + i);
        }
        file2.close();
    }
}
