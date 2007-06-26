import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class DownloadService {
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final Map<String, Future> tasks = new ConcurrentHashMap<String, Future>();
    private final Map<String, Downloader> downloaders = new ConcurrentHashMap();
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

    public void startDownloading(String url) {
        Future future = tasks.get(url);
        if ((future == null || future.isCancelled() || future.isDone())) {
            Downloader downloader = new Downloader(resourceFinder, url, settings.getDownloadPath(), audit);
            downloaders.put(url, downloader);
            tasks.put(url, executorService.submit(downloader));
        }
    }

    public void stopDownloading(String url) {
        downloaders.get(url).stop();
//        try {
        Future future = tasks.remove(url);
        future.cancel(false);
//        } catch (InterruptedException e) {
//            audit.addMessage(e);
//        } catch (ExecutionException e) {
//            audit.addMessage(e);
//        }
    }

}
