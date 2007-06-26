import javax.swing.event.TableModelEvent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.Map;

class ProgressMonitor implements Runnable {
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, Long> rates = new ConcurrentHashMap();
    private final DownloadsTableModel model;

    public ProgressMonitor(DownloadsTableModel downloadsTableModel) {
        this.model = downloadsTableModel;
    }

    public void run() {
        model.foreachDownloader(new DownloadsTableModel.DownloaderVisitor() {
           public void visit(Downloader downloader, String url, int rowIndex) {
               Long lastSize = rates.get(url);
               long soFar = downloader.getDownloadedSoFar();
               if (lastSize == null) {
                   lastSize = soFar;
               }

               long currentRate = soFar - lastSize;
               downloader.setCurrentRate(currentRate);
               rates.put(url, soFar);

               model.notifyListeners(new TableModelEvent(model, rowIndex, rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
           }
        });
    }

    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
    }
}
