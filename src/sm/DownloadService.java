package sm;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class DownloadService {
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Map<String, Future> tasks = new ConcurrentHashMap<String, Future>();
    private final Map<String, Downloader> downloaders = new ConcurrentHashMap<String, Downloader>();
    private final Settings settings;
    private final Audit audit;
    private final RapidShareResourceFinder resourceFinder;

    public DownloadService(Settings settings, Audit audit, RapidShareResourceFinder resourceFinder) {
        this.settings = settings;
        this.audit = audit;
        this.resourceFinder = resourceFinder;
    }


    public Downloader getDownloader(String url) {
        return downloaders.get(url);
    }

    public void startDownloading(final String url) {
        Future future = tasks.get(url);
        if (future == null) {
            final Downloader downloader = new Downloader(resourceFinder, url, settings.getDownloadPath());
            downloaders.put(url, downloader);
            future = executorService.submit(new Runnable() {
                public void run() {
                    try {
                        downloader.download();
                    } catch (InterruptedException e) {
                        //cool - must have been cancelled by the user
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
        if(future != null)
            future.cancel(true);
    }

    public void stop() {
        executorService.shutdownNow();
    }

    public static void main(String[] args) throws Exception {
        String name = "foo.txt";
        new File(name).delete();
        
        RandomAccessFile file = new RandomAccessFile(name, "rw");
        file.setLength(1024);
        file.seek(20);
        file.writeChars("hello");
        file.seek(40);
        file.writeChars("world");
        file.close();
    }
}
