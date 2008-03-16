package sm;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

class DownloadStarter implements TableModelListener {
    private final DownloadsTableModel tableModel;
    private final DownloadService service;

    public DownloadStarter(DownloadsTableModel tableModel, DownloadService service) {
        this.tableModel = tableModel;
        this.service = service;
    }

    public void tableChanged(TableModelEvent tableModelEvent) {
        if (tableModelEvent.getColumn() == DownloadsColumnModel.Col.indexOf(DownloadsColumnModel.Col.DOWNLOAD)) {
            int row = tableModelEvent.getFirstRow();
            String url = (String) tableModel.getValueAt(row, DownloadsColumnModel.Col.indexOf(DownloadsColumnModel.Col.RAPIDSHARE_FILE));
            Boolean flag = (Boolean) tableModel.getValueAt(row, DownloadsColumnModel.Col.indexOf(DownloadsColumnModel.Col.DOWNLOAD));
            if (flag)
                service.startDownloading(url);
            else
                service.stopDownloading(url);
        }
    }
}
