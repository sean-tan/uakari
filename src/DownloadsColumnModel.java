import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

class DownloadsColumnModel extends DefaultTableColumnModel {
    enum Col {
        DOWNLOAD, RAPIDSHARE_FILE, PARENT_PAGE, SIZE, PROGRESS;

        private static final Map<Col, Integer> indexes = new HashMap() {
            {
                Col[] cols = Col.values();
                for (int i = 0; i < cols.length; i++) {
                    Col col = cols[i];
                    put(col, i);
                }
            }
        };

        public static int indexOf(Col col) {
            return indexes.get(col);
        }
    }

    {
        addColumn(new TableColumn(Col.indexOf(Col.DOWNLOAD), 60, striped(new JCheckBoxCellRenderer()), new DefaultCellEditor(new JCheckBox())) {
            {
                setHeaderValue(Col.DOWNLOAD.toString().toLowerCase());
                setMaxWidth(80);
            }
        });

        addColumn(new TableColumn(Col.indexOf(Col.RAPIDSHARE_FILE), 100, striped(new DownloadFileCellRenderer()), new DefaultCellEditor(new JTextField())) {
            {
                setHeaderValue(Col.RAPIDSHARE_FILE.toString().toLowerCase());
            }
        });

        addColumn(new TableColumn(Col.indexOf(Col.PARENT_PAGE), 400, striped(new DefaultTableCellRenderer()), new DefaultCellEditor(new JTextField())) {
            {
                setHeaderValue(Col.PARENT_PAGE.toString().toLowerCase());
            }
        });

        addColumn(new TableColumn(Col.indexOf(Col.SIZE), 90, striped(new DownloadSizeCellRenderer()), new DefaultCellEditor(new JTextField())) {
            {
                setHeaderValue(Col.SIZE.toString().toLowerCase());
            }
        });

        addColumn(new TableColumn(Col.indexOf(Col.PROGRESS), 20, striped(new ProgressCellRenderer()), new DefaultCellEditor(new JTextField())) {
            {
                setHeaderValue(Col.PROGRESS.toString().toLowerCase());
            }
        });
    }

    private TableCellRenderer striped(TableCellRenderer renderer) {
        return new StripeyCellRenderer(renderer);
    }

    private static class StripeyCellRenderer implements TableCellRenderer {
        private final TableCellRenderer renderer;

        public StripeyCellRenderer(TableCellRenderer renderer) {
            this.renderer = renderer;
        }

        public Component getTableCellRendererComponent(JTable jTable, Object object, boolean isSelected, boolean hasFocus, int row, int col) {
            Component component = renderer.getTableCellRendererComponent(jTable, object, isSelected, hasFocus, row, col);

            component.setForeground(Color.BLACK);
            if (row % 2 == 0)
                component.setBackground(Color.decode("#FBEC5D"));
            else
                component.setBackground(Color.WHITE);

            if (isSelected) {
                component.setBackground(Color.BLUE);
                component.setForeground(Color.WHITE);
            }

            return component;
        }
    }

    private static class ProgressCellRenderer implements TableCellRenderer {
        private final JProgressBar progressBar = new JProgressBar(0, 100);

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Downloader downloader = (Downloader) value;
            if (downloader == null) {
                progressBar.setValue(0);
            } else {
                progressBar.setMinimum(0);
                progressBar.setMaximum((int) downloader.getDownloadSize());
                progressBar.setValue((int) downloader.getDownloadedSoFar());
            }
            return progressBar;
        }
    }

    private static class DownloadFileCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String url = (String) value;
            int index = url.lastIndexOf("/") + 1;
            String filename = url.substring(index);
            label.setText(filename);
            label.setToolTipText(url);
            return label;
        }
    }

    private static class DownloadSizeCellRenderer extends DefaultTableCellRenderer {
        private static final int KILOBYTE = 1024;
        private static final int MEGABYTE = KILOBYTE * KILOBYTE;

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Downloader downloader = (Downloader) value;
            Boolean downloadChecked = (Boolean) table.getValueAt(row, Col.indexOf(Col.DOWNLOAD));
            if (downloader == null || !downloadChecked) {
                label.setText("");
            } else if (!downloader.hasStarted()) {
                label.setText("queued");
            } else {
                long downloadSizeInMB = downloader.getDownloadSize() / MEGABYTE;
                long currentSizeInMB = downloader.getDownloadedSoFar() / MEGABYTE;
                label.setText(currentSizeInMB + "Mb / " + downloadSizeInMB + "Mb (" + (downloader.getCurrentRate() / KILOBYTE) + "Kb/s)");
            }
            return label;
        }
    }
}
