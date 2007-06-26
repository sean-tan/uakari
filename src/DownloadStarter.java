import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

class DownloadStarter implements TableModelListener {
    private final DownloadsTableModel tableModel;
    private final DownloadService service;

    public DownloadStarter(DownloadsTableModel tableModel, DownloadService service) {
        this.tableModel = tableModel;
        this.service = service;
    }

    public void tableChanged(TableModelEvent tableModelEvent) {
        if(tableModelEvent.getColumn() == 0) {
            int row = tableModelEvent.getFirstRow();
            String url = (String) tableModel.getValueAt(row, 1);
            Boolean flag = (Boolean) tableModel.getValueAt(row, 0);
            if (flag)
                service.startDownloading(url);
            else
                service.stopDownloading(url);
        }
    }
}
