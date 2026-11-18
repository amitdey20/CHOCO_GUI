import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// vertical stack of labelled input fields with an inline error line.
public class Form extends JPanel {
    private final List<JTextField> fields = new ArrayList<>();
    private final JPanel box = new JPanel();
    private final JLabel error = Theme.label(" ", Theme.SMALL_BOLD, Theme.RED);

    public Form() {
        super(new BorderLayout());
        setOpaque(false);
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        error.setBorder(new EmptyBorder(2, 0, 0, 0));
        add(box, BorderLayout.CENTER);
        add(error, BorderLayout.SOUTH);
    }

    public JTextField text(String label) { return addField(label, new JTextField()); }

    public JPasswordField password(String label) { return addField(label, new JPasswordField()); }

    private <T extends JTextField> T addField(String label, T field) {
        JLabel l = Theme.label(label, Theme.SMALL_BOLD, Theme.MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        Theme.styleField(field);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        box.add(l);
        box.add(Box.createVerticalStrut(4));
        box.add(field);
        box.add(Box.createVerticalStrut(11));
        fields.add(field);
        return field;
    }

    public JTextField field(int i) { return fields.get(i); }

    public String get(int i) {
        JTextField f = fields.get(i);
        return f instanceof JPasswordField ? new String(((JPasswordField) f).getPassword()) : f.getText().trim();
    }

    public String[] values() {
        String[] v = new String[fields.size()];
        for (int i = 0; i < v.length; i++) v[i] = get(i);
        return v;
    }

    public void showError(String msg) { error.setText("<html>" + msg.replace("\n", "<br>") + "</html>"); }

    public void clearError() { error.setText(" "); }

    public void clear() {
        for (JTextField f : fields) f.setText("");
        clearError();
    }

    // Calls the runnable when enter is pressed in any field.
    public void onEnter(Runnable r) {
        for (JTextField f : fields) f.addActionListener(e -> r.run());
    }
}
