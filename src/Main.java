import org.jdesktop.swingx.JXFrame;

import javax.swing.JFrame;

public class Main {
    /*
    - default to home dir
    - save settings to file (maybe temp dir)
    - add rapidshare .de / .com option
    - use jgoodies l&f
    - use swingx table
    - add remove button to row
     */

    public static void main(String[] args) throws Exception {
//        UIManager.setLookAndFeel(new AquaLookAndFeel());
//        UIManager.put("ClassLoader", LookUtils.class.getClassLoader());
        
        String homeDir = System.getenv().get("HOME");
        if(homeDir == null)
            homeDir = System.getenv().get("HOMEPATH");
        
        DownloadsColumnModel columnModel = new DownloadsColumnModel();
        String username = args.length >= 1 ? args[0] : "";
        String password = args.length >= 2 ? args[1] : "";
        MutableSettings settings = new MutableSettings(homeDir, username, password);
        SwingAudit audit = new SwingAudit();
        RapidShareResourceFinder finder = new RapidShareResourceFinder(settings);
        DownloadService service = new DownloadService(settings, audit, finder);
        DownloadsTableModel tableModel = new DownloadsTableModel(columnModel, service);
        RapidForm form = new RapidForm(tableModel, columnModel, settings, audit);
        ProgressMonitor progressMonitor = new ProgressMonitor(tableModel);
        progressMonitor.start();

        JXFrame frame = new JXFrame("uakari");
        form.showUsing(frame);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1024, frame.getHeight());
        frame.setVisible(true);

        RapidShareUrlParser urlParser = new RapidShareUrlParser(audit);
        ThreadedSearcher searcher = new ThreadedSearcher(urlParser, audit);
        form.setSearcher(searcher);

        searcher.addSearchListener(new TableModelSearchListener(tableModel, audit, form));

        tableModel.addTableModelListener(new DownloadStarter(tableModel, service));

    }

}