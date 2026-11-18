import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import java.util.function.Function;

/** Styled pop-ups: message, confirm, form, OTP and receipt. */
public final class Dialogs {
    private Dialogs() {}

    public static String html(String text, int width) {
        String s = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
        return "<html><div style='width:" + width + "px'>" + s + "</div></html>";
    }

    // The window a pop-up should belong to.
    public static Window owner(Component parent) {
        if (parent == null) return null;
        return parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
    }

    public static JDialog base(Component parent, String title, int width) {
        Window w = owner(parent);
        JDialog d = new JDialog(w, title, Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.setResizable(false);
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.CREAM);
        d.setContentPane(content);
        Theme.GradientPanel header = new Theme.GradientPanel(Theme.DARK, Theme.CHOC);
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 26, 16, 26));
        header.add(Theme.label(title, Theme.H2, Color.WHITE), BorderLayout.CENTER);
        header.setPreferredSize(new Dimension(width, 62));
        content.add(header, BorderLayout.NORTH);
        return d;
    }

    private static JPanel buttons(JComponent... bs) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        for (JComponent b : bs) p.add(b);
        return p;
    }

    private static JPanel bodyPanel() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(22, 26, 22, 26));
        return body;
    }

    public static void message(Component parent, String title, String text, boolean error) {
        JDialog d = base(parent, title, 420);
        JPanel body = bodyPanel();
        JLabel l = new JLabel(html(text, 350));
        l.setFont(Theme.BODY);
        l.setForeground(error ? Theme.RED : Theme.TEXT);
        Theme.RoundButton ok = Theme.primary("OK");
        ok.addActionListener(e -> d.dispose());
        body.add(l, BorderLayout.CENTER);
        body.add(buttons(ok), BorderLayout.SOUTH);
        d.add(body, BorderLayout.CENTER);
        d.getRootPane().setDefaultButton(ok);
        d.pack();
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }

    public static boolean confirm(Component parent, String title, String text, String yesText) {
        JDialog d = base(parent, title, 440);
        boolean[] yes = {false};
        JPanel body = bodyPanel();
        JLabel l = new JLabel(html(text, 370));
        l.setFont(Theme.BODY);
        l.setForeground(Theme.TEXT);
        Theme.RoundButton ok = Theme.primary(yesText);
        Theme.RoundButton no = Theme.ghost("Cancel");
        ok.addActionListener(e -> { yes[0] = true; d.dispose(); });
        no.addActionListener(e -> d.dispose());
        body.add(l, BorderLayout.CENTER);
        body.add(buttons(no, ok), BorderLayout.SOUTH);
        d.add(body, BorderLayout.CENTER);
        d.getRootPane().setDefaultButton(ok);
        d.pack();
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
        return yes[0];
    }

    public static String[] form(Component parent, String title, String subtitle, String okText,
                                Function<String[], String> validator, String... labels) {
        JDialog d = base(parent, title, 440);
        Form f = new Form();
        for (String l : labels) {
            if (l.startsWith("*")) f.password(l.substring(1)); else f.text(l);
        }
        String[][] result = {null};
        String[] after = {null};
        Theme.RoundButton ok = Theme.primary(okText);
        Theme.RoundButton cancel = Theme.ghost("Cancel");
        ok.addActionListener(e -> {
            String[] vals = f.values();
            String err = validator == null ? null : validator.apply(vals);
            if (err != null) {
                if (err.startsWith("!")) { after[0] = err.substring(1); d.dispose(); }
                else f.showError(err);
                return;
            }
            result[0] = vals;
            d.dispose();
        });
        cancel.addActionListener(e -> d.dispose());
        f.onEnter(ok::doClick);

        JPanel body = bodyPanel();
        if (subtitle != null) {
            JLabel s = new JLabel(html(subtitle, 370));
            s.setFont(Theme.BODY);
            s.setForeground(Theme.MUTED);
            body.add(s, BorderLayout.NORTH);
        }
        body.add(f, BorderLayout.CENTER);
        body.add(buttons(cancel, ok), BorderLayout.SOUTH);
        d.add(body, BorderLayout.CENTER);
        d.getRootPane().setDefaultButton(ok);
        d.pack();
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
        if (after[0] != null) message(parent, title, after[0], true);
        return result[0];
    }

    // Simulated OTP check (3 times). Returns true when verified.
    public static boolean otp(Component parent, String phone) {
        String masked = phone.substring(0, 3) + "*****" + phone.substring(8);
        int code = 1000 + new Random().nextInt(9000);
        JDialog d = base(parent, "Verify your number", 440);
        boolean[] ok = {false};
        int[] left = {3};

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        JLabel info = new JLabel(html("We are sending a 4-digit code to " + masked, 370));
        info.setFont(Theme.BODY);
        info.setForeground(Theme.MUTED);
        Theme.RoundPanel demo = new Theme.RoundPanel(14, new Color(0xFFF1D6), false);
        demo.setLayout(new BorderLayout());
        demo.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel demoLbl = Theme.label("Sending code ...", Theme.H2, Theme.CARAMEL_DARK);
        demoLbl.setHorizontalAlignment(SwingConstants.CENTER);
        demo.add(demoLbl);
        top.add(info, BorderLayout.NORTH);
        top.add(demo, BorderLayout.CENTER);

        Form f = new Form();
        JTextField field = f.text("Enter OTP");
        Theme.RoundButton verify = Theme.primary("Verify");
        verify.setEnabled(false);
        Theme.RoundButton cancel = Theme.ghost("Cancel");
        cancel.addActionListener(e -> d.dispose());
        verify.addActionListener(e -> {
            if (f.get(0).equals(String.valueOf(code))) { ok[0] = true; d.dispose(); return; }
            left[0]--;
            if (left[0] <= 0) { d.dispose(); return; }
            f.showError("Incorrect OTP. Attempts remaining: " + left[0]);
            field.selectAll();
        });
        f.onEnter(verify::doClick);

        javax.swing.Timer t = new javax.swing.Timer(1300, e -> {
            demoLbl.setText("Simulation - Your OTP is: " + code);
            verify.setEnabled(true);
            field.requestFocusInWindow();
        });
        t.setRepeats(false);
        t.start();
        d.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) { t.stop(); }
        });

        JPanel body = bodyPanel();
        body.add(top, BorderLayout.NORTH);
        body.add(f, BorderLayout.CENTER);
        body.add(buttons(cancel, verify), BorderLayout.SOUTH);
        d.add(body, BorderLayout.CENTER);
        d.getRootPane().setDefaultButton(verify);
        d.pack();
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
        if (!ok[0] && left[0] <= 0) message(parent, "Verification failed", "Too many incorrect attempts.", true);
        return ok[0];
    }

    public static void receipt(Component parent, String title, String text) {
        JDialog d = base(parent, title, 560);
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setForeground(Theme.TEXT);
        area.setOpaque(false);
        Theme.RoundPanel paper = new Theme.RoundPanel(18, Color.WHITE, false);
        paper.setLayout(new BorderLayout());
        paper.setBorder(new EmptyBorder(18, 20, 18, 20));
        paper.add(area);
        Theme.RoundButton done = Theme.primary("Done");
        done.addActionListener(e -> d.dispose());
        JPanel body = bodyPanel();
        body.add(paper, BorderLayout.CENTER);
        body.add(buttons(done), BorderLayout.SOUTH);
        d.add(body, BorderLayout.CENTER);
        d.getRootPane().setDefaultButton(done);
        d.pack();
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }
}
