package sm;

import org.jdesktop.swingx.JXFrame;

import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Uakari {
    /*
    - default to home dir
    - save settings to file (maybe temp dir)
    - add rapidshare .de / .com option
    - use jgoodies l&f
    - use swingx table
    - add remove button to row
     */

    public static void main(String[] args) throws Exception {
        String homeDir = System.getenv().get("HOME");
        if(homeDir == null)
            homeDir = System.getenv().get("HOMEPATH");
        
        DownloadsColumnModel columnModel = new DownloadsColumnModel();
        String username = args.length >= 1 ? args[0] : "";
        String password = args.length >= 2 ? args[1] : "";
        MutableSettings settings = new MutableSettings(homeDir, username, password);

        SwingAudit audit = new SwingAudit();
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(20);
        final DownloadService service = new DownloadService(executorService, settings, audit);
        DownloadsTableModel tableModel = new DownloadsTableModel(columnModel, service);
        RapidForm form = new RapidForm(tableModel, columnModel, settings, audit);
        final ProgressMonitor progressMonitor = new ProgressMonitor(executorService, tableModel);
        progressMonitor.start();

        JXFrame frame = new JXFrame("uakari");
        form.showUsing(frame);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1024, frame.getHeight());
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent windowEvent) {
                progressMonitor.stop();
                service.stop();
                executorService.shutdownNow();
            }
        });

        RapidShareUrlParser urlParser = new RapidShareUrlParser(audit);
        ThreadedSearcher searcher = new ThreadedSearcher(executorService, urlParser, audit);
        form.setSearcher(searcher);

        searcher.addSearchListener(new TableModelSearchListener(tableModel, audit, form));

        tableModel.addTableModelListener(new DownloadStarter(tableModel, service));

    }

}