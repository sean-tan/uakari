import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import java.io.StringWriter;
import java.io.PrintWriter;

class SwingAudit implements Audit{ 
    private static final int MAX_LOG_LENGTH = (1024 * 1024);

    private JTextArea textArea;

    public void setTextArea(JTextArea textArea) {
        this.textArea = textArea;
    }

    public void addMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    String m = message;
                    if (!message.endsWith("\n")) {
                        m += "\n";
                    }
                    Document document = textArea.getDocument();
                    document.insertString(0, m, null);
                    int size = document.getLength();
                    if (size > MAX_LOG_LENGTH) {
                        document.remove(MAX_LOG_LENGTH, size);
                    }
                    textArea.setCaretPosition(0);
                } catch (BadLocationException e) {
                    throw new RuntimeException("Todo: handle this correctly", e);
                }
            }
        });
    }

    public void addMessage(Exception e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        String s = stringWriter.toString();
        addMessage(s);
    }
}
