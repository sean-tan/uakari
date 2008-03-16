package sm;

import javax.swing.event.TableModelEvent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class ProgressMonitor implements Runnable {
    private final ScheduledExecutorService scheduledExecutorService;
    private final DownloadsTableModel model;
    private ScheduledFuture<?> scheduledFuture;

    public ProgressMonitor(ScheduledExecutorService scheduledExecutorService, DownloadsTableModel downloadsTableModel) {
        this.scheduledExecutorService = scheduledExecutorService;
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
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this, 0, Downloader.READ_TIME, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduledFuture.cancel(true);
    }
}
