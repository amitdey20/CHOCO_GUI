import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;

// Payment window: MFS or card, OTP, processing animation, success.
public class PaymentDialog extends JDialog {
    private static final String[] MFS = {
        "bKash", "Nagad", "Rocket", "Upay", "CellFin", "mCash", "SureCash", "tap",
        "OK Wallet", "MYCash", "Islamic Wallet", "Meghna Pay", "Pathao Pay", "FirstCash", "TeleCash", "UCash"
    };
    private static final Color[] TILE = {
        new Color(0xE2136E), new Color(0xF6921E), new Color(0x8C3494), new Color(0x1B75BB),
        new Color(0x00A651), new Color(0xD9262C), new Color(0x2E3192), new Color(0xF15A29)
    };

    private final User user;
    private final int amount;
    private boolean paid, busy;
    private final CardLayout pages = new CardLayout();
    private final JPanel stack = new JPanel(pages);
    private final Form mfsForm = new Form();
    private final Form cardForm = new Form();
    private final JLabel mfsTitle = Theme.label("", Theme.TITLE, Theme.DARK);
    private String mfsName = "";

    public static boolean show(Component parent, User user, int amount, String purpose) {
        PaymentDialog d = new PaymentDialog(parent, user, amount, purpose);
        d.setVisible(true);
        return d.paid;
    }

    private PaymentDialog(Component parent, User user, int amount, String purpose) {
        super(Dialogs.owner(parent), "Secure Payment",
                Dialog.ModalityType.APPLICATION_MODAL);
        this.user = user;
        this.amount = amount;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { if (!busy) dispose(); }
        });
        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);
        root.add(summary(purpose), BorderLayout.WEST);
        stack.setBackground(Theme.CREAM);
        stack.add(methodsPage(), "methods");
        stack.add(mfsPage(), "mfs");
        stack.add(cardPage(), "card");
        stack.add(processingPage(), "processing");
        root.add(stack, BorderLayout.CENTER);
        setSize(860, 640);
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private String money() { return String.format("%,d", amount); }

    // ------------------------------------------------------------ left summary
    private JComponent summary(String purpose) {
        Theme.GradientPanel p = new Theme.GradientPanel(Theme.DARK, Theme.CHOC);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(30, 26, 26, 26));
        p.setPreferredSize(new Dimension(260, 0));
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(left(Theme.label("SECURE CHECKOUT", Theme.SMALL_BOLD, Theme.CARAMEL)));
        top.add(Box.createVerticalStrut(18));
        top.add(left(Theme.label(purpose, Theme.BODY, new Color(0xE9D9CB))));
        top.add(Box.createVerticalStrut(4));
        top.add(left(Theme.label(money() + " Taka", Theme.font(Font.BOLD, 32), Color.WHITE)));
        top.add(Box.createVerticalStrut(28));
        top.add(left(Theme.label("Paying as", Theme.SMALL, new Color(0xC9B5A5))));
        top.add(left(Theme.label(user.getName(), Theme.BOLD, Color.WHITE)));
        top.add(left(Theme.label(user.getPhone(), Theme.BODY, new Color(0xE9D9CB))));
        p.add(top, BorderLayout.NORTH);
        JPanel art = new JPanel(new FlowLayout(FlowLayout.CENTER));
        art.setOpaque(false);
        art.add(new Theme.ChocoArt(Theme.CARAMEL, 150, 130));
        p.add(art, BorderLayout.SOUTH);
        return p;
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private JPanel page() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(28, 30, 24, 30));
        return p;
    }

    // ------------------------------------------------------------ pages
    private JPanel methodsPage() {
        JPanel p = page();
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(left(Theme.label("Choose a payment method", Theme.TITLE, Theme.DARK)));
        head.add(Box.createVerticalStrut(14));
        Theme.RoundButton card = Theme.dark("Pay with Debit / Credit Card");
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        card.addActionListener(e -> { cardForm.clearError(); pages.show(stack, "card"); });
        head.add(card);
        head.add(Box.createVerticalStrut(16));
        head.add(left(Theme.label("MOBILE BANKING", Theme.SMALL_BOLD, Theme.MUTED)));
        p.add(head, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 3, 10, 10));
        grid.setOpaque(false);
        for (int i = 0; i < MFS.length; i++) {
            String name = MFS[i];
            Tile t = new Tile(name, TILE[i % TILE.length]);
            t.addActionListener(e -> openMfs(name));
            grid.add(t);
        }
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(grid, BorderLayout.NORTH);
        p.add(holder, BorderLayout.CENTER);

        Theme.RoundButton cancel = Theme.ghost("Cancel payment");
        cancel.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        south.setOpaque(false);
        south.add(cancel);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private void openMfs(String name) {
        mfsName = name;
        mfsTitle.setText(name + " payment");
        mfsForm.clear();
        pages.show(stack, "mfs");
        mfsForm.field(0).requestFocusInWindow();
    }

    private JPanel mfsPage() {
        JPanel p = page();
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JButton back = Theme.link("< Back to methods");
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> pages.show(stack, "methods"));
        head.add(back);
        head.add(Box.createVerticalStrut(8));
        mfsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(mfsTitle);
        head.add(Box.createVerticalStrut(4));
        head.add(left(Theme.label("An OTP will be sent to your number to confirm.", Theme.BODY, Theme.MUTED)));
        head.add(Box.createVerticalStrut(18));
        p.add(head, BorderLayout.NORTH);

        mfsForm.text("Account number (11 digits)");
        mfsForm.password("PIN (4-5 digits)");
        p.add(mfsForm, BorderLayout.CENTER);

        Theme.RoundButton pay = Theme.primary("Pay " + money() + " Taka");
        pay.addActionListener(e -> payMobile());
        mfsForm.onEnter(pay::doClick);
        p.add(pay, BorderLayout.SOUTH);
        return p;
    }

    private JPanel cardPage() {
        JPanel p = page();
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JButton back = Theme.link("< Back to methods");
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> pages.show(stack, "methods"));
        head.add(back);
        head.add(Box.createVerticalStrut(8));
        head.add(left(Theme.label("Card payment", Theme.TITLE, Theme.DARK)));
        head.add(Box.createVerticalStrut(4));
        head.add(left(Theme.label("A bank OTP will be sent to your registered phone.", Theme.BODY, Theme.MUTED)));
        head.add(Box.createVerticalStrut(18));
        p.add(head, BorderLayout.NORTH);

        cardForm.text("Card number (16 digits)");
        cardForm.text("Expiry month (1-12)");
        cardForm.text("Expiry year (YYYY)");
        cardForm.password("CVC / CVV (3 digits)");
        p.add(cardForm, BorderLayout.CENTER);

        Theme.RoundButton pay = Theme.primary("Pay " + money() + " Taka");
        pay.addActionListener(e -> payCard());
        cardForm.onEnter(pay::doClick);
        p.add(pay, BorderLayout.SOUTH);
        return p;
    }

    private JPanel processingPage() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JPanel c = new JPanel();
        c.setOpaque(false);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        JLabel l = Theme.label("Processing payment ...", Theme.TITLE, Theme.DARK);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel s = Theme.label("Please do not close this window", Theme.BODY, Theme.MUTED);
        s.setAlignmentX(Component.CENTER_ALIGNMENT);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setBorderPainted(false);
        bar.setForeground(Theme.CARAMEL);
        bar.setBackground(Theme.LINE);
        bar.setMaximumSize(new Dimension(300, 8));
        bar.setPreferredSize(new Dimension(300, 8));
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.add(l);
        c.add(Box.createVerticalStrut(8));
        c.add(s);
        c.add(Box.createVerticalStrut(26));
        c.add(bar);
        p.add(c);
        return p;
    }

    private JPanel successPage(String via) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JPanel c = new JPanel();
        c.setOpaque(false);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        CheckIcon icon = new CheckIcon();
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel t = Theme.label("Payment Successful!", Theme.TITLE, Theme.GREEN);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel a = Theme.label(money() + " Taka paid via " + via, Theme.H2, Theme.DARK);
        a.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel th = Theme.label("Thank you, " + user.getName() + "!", Theme.BODY, Theme.MUTED);
        th.setAlignmentX(Component.CENTER_ALIGNMENT);
        Theme.RoundButton done = Theme.primary("Continue");
        done.setAlignmentX(Component.CENTER_ALIGNMENT);
        done.addActionListener(e -> dispose());
        c.add(icon);
        c.add(Box.createVerticalStrut(16));
        c.add(t);
        c.add(Box.createVerticalStrut(8));
        c.add(a);
        c.add(Box.createVerticalStrut(4));
        c.add(th);
        c.add(Box.createVerticalStrut(26));
        c.add(done);
        p.add(c);
        return p;
    }

    // ------------------------------------------------------------ actions
    private void payMobile() {
        String num = mfsForm.get(0), pin = mfsForm.get(1);
        if (!AuthService.isValidPhone(num)) {
            mfsForm.showError("Enter a valid 11-digit BD mobile number.");
            return;
        }
        if (!pin.matches("[0-9]{4,5}")) {
            mfsForm.showError("PIN must be 4 or 5 digits.");
            return;
        }
        mfsForm.clearError();
        if (!Dialogs.otp(this, num)) {
            mfsForm.showError("Payment cancelled - OTP was not verified.");
            return;
        }
        process(mfsName);
    }

    private void payCard() {
        String number = cardForm.get(0).replaceAll("\\s", "");
        if (!number.matches("[0-9]{16}")) { cardForm.showError("Card number must be exactly 16 digits."); return; }
        int month, year;
        try {
            month = Integer.parseInt(cardForm.get(1));
            year = Integer.parseInt(cardForm.get(2));
        } catch (NumberFormatException e) {
            cardForm.showError("Expiry month and year must be numbers.");
            return;
        }
        if (month < 1 || month > 12) { cardForm.showError("Expiry month must be between 1 and 12."); return; }
        if (year < 1000 || year > 2100) { cardForm.showError("Enter the expiry year as YYYY."); return; }
        LocalDate today = LocalDate.now();
        if (year < today.getYear() || (year == today.getYear() && month < today.getMonthValue())) {
            cardForm.showError("Payment failed - this card is expired.");
            return;
        }
        if (!cardForm.get(3).matches("[0-9]{3}")) { cardForm.showError("CVC / CVV must be exactly 3 digits."); return; }
        cardForm.clearError();
        if (!Dialogs.otp(this, user.getPhone())) {
            cardForm.showError("Payment cancelled - OTP was not verified.");
            return;
        }
        process("Card");
    }

    private void process(String via) {
        busy = true;
        pages.show(stack, "processing");
        javax.swing.Timer t = new javax.swing.Timer(2400, e -> {
            busy = false;
            paid = true;
            stack.add(successPage(via), "success");
            pages.show(stack, "success");
        });
        t.setRepeats(false);
        t.start();
    }

    // ------------------------------------------------------------ small widgets
    private static class Tile extends JButton {
        private final Color color;
        private boolean over;

        Tile(String name, Color color) {
            super(name);
            this.color = color;
            setFont(Theme.font(Font.BOLD, 13));
            setForeground(Theme.TEXT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(0, 52, 0, 6));
            setPreferredSize(new Dimension(150, 46));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { over = true; repaint(); }
                public void mouseExited(MouseEvent e) { over = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Theme.smooth(g);
            int w = getWidth(), h = getHeight();
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
            g2.setColor(over ? Theme.CARAMEL : Theme.LINE);
            g2.setStroke(new BasicStroke(over ? 2f : 1f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 16, 16);
            g2.setColor(color);
            g2.fillOval(11, h / 2 - 14, 28, 28);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.font(Font.BOLD, 14));
            String ch = getText().substring(0, 1).toUpperCase();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(ch, 25 - fm.stringWidth(ch) / 2, h / 2 + fm.getAscent() / 2 - 2);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class CheckIcon extends JComponent {
        CheckIcon() { setPreferredSize(new Dimension(110, 110)); setMaximumSize(new Dimension(110, 110)); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Theme.smooth(g);
            g2.setColor(new Color(46, 158, 103, 40));
            g2.fillOval(0, 0, 110, 110);
            g2.setColor(Theme.GREEN);
            g2.fillOval(10, 10, 90, 90);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawPolyline(new int[]{34, 50, 78}, new int[]{57, 72, 40}, 3);
            g2.dispose();
        }
    }
}
