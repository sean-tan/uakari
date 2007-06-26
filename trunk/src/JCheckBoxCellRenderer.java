import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.awt.event.ActionEvent;

class JCheckBoxCellRenderer implements TableCellRenderer {
    private final JCheckBox checkBox = new JCheckBox() {
        {
            addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent actionEvent) {
                    System.out.println(actionEvent);
                }
            });
        }
    };

    public Component getTableCellRendererComponent(JTable jTable, Object object, boolean b, boolean b1, int i, int i1) {
        Boolean checked = (Boolean) object;
        checkBox.setSelected(checked);
        return checkBox;
    }
}
