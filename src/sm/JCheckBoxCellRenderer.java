package sm;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;

class JCheckBoxCellRenderer implements TableCellRenderer {
    private final JCheckBox checkBox = new JCheckBox();

    public Component getTableCellRendererComponent(JTable jTable, Object object, boolean b, boolean b1, int i, int i1) {
        Boolean checked = (Boolean) object;
        checkBox.setSelected(checked);
        return checkBox;
    }
}
