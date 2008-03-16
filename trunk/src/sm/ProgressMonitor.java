package sm;

import javax.swing.event.TableModelEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class ProgressMonitor implements Runnable {
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final DownloadsTableModel model;

    public ProgressMonitor(DownloadsTableModel downloadsTableModel) {
        this.model = downloadsTableModel;
    }

    public void run() {
        model.foreachDownloader(new DownloadsTableModel.DownloaderVisitor() {
            public void visit(Downloader downloader, String url, int rowIndex) {
                model.notifyListeners(
                        new TableModelEvent(model, rowIndex, rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
            }
        });
    }

    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(this, 0, Downloader.READ_TIME, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduledExecutorService.shutdownNow();
    }
}
