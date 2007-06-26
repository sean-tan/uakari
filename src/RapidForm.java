import org.jdesktop.swingx.border.DropShadowBorder;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.TableColumnModel;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.io.File;

public class RapidForm {
    private JTable table;
    private JTextField urlField;
    private JButton browseButton;
    private JPanel panel;
    private JTextField downloadPathField;
    private JButton scanPageButton;
    private JButton searchWebButton;
    private JTextField searchField;
    private JTabbedPane tabbedPane;
    private JPanel searchPanel;
    private JTabbedPane controlsTabbedPane;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextArea logTextArea;
    private JButton stopButton;
    private Searcher searcher;

    public RapidForm(final DownloadsTableModel tableModel, TableColumnModel columnModel, final MutableSettings settings, SwingAudit audit) {
        audit.setTextArea(logTextArea);
        table.setAutoCreateColumnsFromModel(false);
        table.setModel(tableModel);
        table.setColumnModel(columnModel);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Color.GRAY);
        table.setOpaque(false);
        controlsTabbedPane.setBorder(new DropShadowBorder(true));
        tabbedPane.setBorder(new DropShadowBorder(true));

        searchWebButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                searchStarted();
                searcher.searchWeb(searchField.getText());
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchStarted();
                    searcher.searchWeb(searchField.getText());
                }
            }
        });

        scanPageButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                searchStarted();
                searcher.scanPage(urlField.getText());
            }
        });

        urlField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchStarted();
                    searcher.scanPage(urlField.getText());
                }
            }
        });

        stopButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                searcher.interuptSearch();
            }
        });

        browseButton.addActionListener(new SelelectDownloadPathAction());

        downloadPathField.setText(settings.getDownloadPath().getAbsolutePath());
        downloadPathField.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                settings.setDownloadPath(downloadPathField.getText());
            }
        });

        passwordField.setText(settings.getPassword());
        passwordField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent focusEvent) {

            }

            public void focusLost(FocusEvent focusEvent) {
                settings.setPassword(new String(passwordField.getPassword()));
            }
        });

        usernameField.setText(settings.getUsername());
        usernameField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent focusEvent) {
                throw new UnsupportedOperationException();
            }

            public void focusLost(FocusEvent focusEvent) {
                settings.setUsername(usernameField.getText());
            }
        });
    }

    public void showUsing(Container c) {
        c.add(panel);
    }

    public void setSearcher(Searcher searcher) {
        this.searcher = searcher;
    }

    public Settings getSettings() {
        return new Settings() {
            public File getDownloadPath() {
                return new File(downloadPathField.getText());
            }

            public String getUsername() {
                return usernameField.getText();
            }

            public String getPassword() {
                return new String(passwordField.getPassword());
            }
        };
    }

    public void searchFinished() {
        this.searchField.setEnabled(true);
        this.urlField.setEnabled(true);
        this.searchWebButton.setEnabled(true);
        this.scanPageButton.setEnabled(true);
        this.stopButton.setEnabled(false);
    }

    public void searchStarted() {
        this.searchField.setEnabled(false);
        this.urlField.setEnabled(false);
        this.searchWebButton.setEnabled(false);
        this.scanPageButton.setEnabled(false);
        this.stopButton.setEnabled(true);
    }

    private class SelelectDownloadPathAction extends AbstractAction {
        public void actionPerformed(ActionEvent actionEvent) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setCurrentDirectory(new File(downloadPathField.getText()));
            int i = chooser.showOpenDialog(browseButton);
            if (i == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                downloadPathField.setText(file.getAbsolutePath());
            }
        }
    }
}