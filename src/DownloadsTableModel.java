import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

class DownloadsTableModel implements TableModel {
    private final DownloadsColumnModel columnModel;
    private final DownloadService downloadService;
    private final List<Row> data = new ArrayList<Row>();
    private final Set<String> urls = new HashSet();
    private final List<TableModelListener> listeners = new ArrayList<TableModelListener>();
    private final Set<Integer> downloaders = new CopyOnWriteArraySet<Integer>();

    public DownloadsTableModel(DownloadsColumnModel columnModel, DownloadService downloadService) {
        this.columnModel = columnModel;
        this.downloadService = downloadService;
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return columnModel.getColumnCount();
    }

    public String getColumnName(int i) {
        return columnModel.getColumn(i).getHeaderValue().toString();
    }

    public Class<?> getColumnClass(int i) {
        switch (col(i)) {
            case DOWNLOAD:
                return Boolean.class;
            case SIZE:
                return Downloader.class;
            case PROGRESS:
                return Downloader.class;
            default:
                return String.class;
        }
    }

    private DownloadsColumnModel.Col col(int i) {
        return DownloadsColumnModel.Col.values()[i];
    }

    public boolean isCellEditable(int row, int col) {
        return DownloadsColumnModel.Col.DOWNLOAD == col(0);
    }

    public Object getValueAt(int rowIndex, int colIndex) {
        Row row = data.get(rowIndex);
        DownloadsColumnModel.Col col = col(colIndex);
        switch (col) {
            case PROGRESS:
                return downloadService.getDownloader(row.getUrl());
            case SIZE:
                return downloadService.getDownloader(row.getUrl());
            default:
                return row.value(col);
        }

    }

    public void setValueAt(Object object, int rowIndex, int colIndex) {
        if (DownloadsColumnModel.Col.DOWNLOAD != col(0))
            return;

        Boolean flag = (Boolean) object;
        Row row = data.get(rowIndex);
        row.setDownloading(flag);
        notifyListeners(new TableModelEvent(this, rowIndex, rowIndex, colIndex, TableModelEvent.UPDATE));

        if (flag)
            this.downloaders.add(rowIndex);
    }

    public void addTableModelListener(TableModelListener tableModelListener) {
        listeners.add(tableModelListener);
    }

    public void removeTableModelListener(TableModelListener tableModelListener) {
        Iterator<TableModelListener> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            TableModelListener listener = iterator.next();
            if (listener == tableModelListener)
                iterator.remove();
        }
    }

    public void add(Row row) {
        this.data.add(row);
        urls.add(row.getUrl());
        notifyListeners(new TableModelEvent(this, data.size(), data.size(), TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    public void notifyListeners(final TableModelEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (TableModelListener listener : listeners) {
                    listener.tableChanged(event);
                }
            }
        });
    }

    public boolean containsUrl(String url) {
        return urls.contains(url);
    }

    public void foreachDownloader(DownloadsTableModel.DownloaderVisitor downloaderVisitor) {
        for (Integer rowIndex : downloaders) {
            String url = (String) getValueAt(rowIndex, DownloadsColumnModel.Col.indexOf(DownloadsColumnModel.Col.FILE));
            Downloader downloader = (Downloader) getValueAt(rowIndex, DownloadsColumnModel.Col.indexOf(DownloadsColumnModel.Col.PROGRESS));
            if (downloader != null)
                downloaderVisitor.visit(downloader, url, rowIndex);
        }
    }

    public interface DownloaderVisitor {
        void visit(Downloader downloader, String url, int rowIndex);
    }
}
