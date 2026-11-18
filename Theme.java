import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;

/** Colours, fonts and small custom-painted widgets used by the whole app. */
public final class Theme {
    private Theme() {}

    public static final Color DARK = new Color(0x2E1A12);
    public static final Color CHOC = new Color(0x5A3222);
    public static final Color CARAMEL = new Color(0xE09F3E);
    public static final Color CARAMEL_DARK = new Color(0xC17F22);
    public static final Color CREAM = new Color(0xFBF3E8);
    public static final Color LINE = new Color(0xEBDCCB);
    public static final Color TEXT = new Color(0x3A2A24);
    public static final Color MUTED = new Color(0x8D786B);
    public static final Color GREEN = new Color(0x2E9E67);
    public static final Color RED = new Color(0xD0453B);
    public static final Color PINK = new Color(0xE0527E);

    private static final Color[] WRAPS = {
        new Color(0xC8433B), new Color(0x2F7FBF), new Color(0x2E9E67), new Color(0x8B4FBF),
        new Color(0xE09F3E), new Color(0xD9548A), new Color(0x1F9AA8), new Color(0xB5562B)
    };

    public static Color wrapFor(String name) {
        int x = name.hashCode() * 0x9E3779B1;
        x ^= x >>> 15;
        return WRAPS[(x >>> 3) % WRAPS.length];
    }

    public static Font font(int style, int size) { return new Font("Segoe UI", style, size); }

    public static final Font BODY = font(Font.PLAIN, 14);
    public static final Font BOLD = font(Font.BOLD, 14);
    public static final Font SMALL = font(Font.PLAIN, 12);
    public static final Font SMALL_BOLD = font(Font.BOLD, 12);
    public static final Font H2 = font(Font.BOLD, 18);
    public static final Font TITLE = font(Font.BOLD, 26);

    public static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    public static JLabel label(String text, Font f, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    // ------------------------------------------------------------------ buttons
    public static class RoundButton extends JButton {
        private final Color base, hover, outline;
        private boolean over, active;

        public RoundButton(String text, Color base, Color hover, Color fg, Color outline) {
            super(text);
            this.base = base;
            this.hover = hover;
            this.outline = outline;
            setForeground(fg);
            setFont(BOLD);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(11, 22, 11, 22));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { over = true; repaint(); }
                public void mouseExited(MouseEvent e) { over = false; repaint(); }
            });
        }

        public RoundButton compact() {
            setBorder(new EmptyBorder(5, 12, 5, 12));
            return this;
        }

        public void setActive(boolean a) { active = a; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            Color c = !isEnabled() ? new Color(0xD9CFC6) : active ? CARAMEL : over ? hover : base;
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            if (outline != null && !active) {
                g2.setColor(outline);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final Color CLEAR = new Color(0, 0, 0, 0);

    public static RoundButton primary(String t) { return new RoundButton(t, CARAMEL, CARAMEL_DARK, Color.WHITE, null); }
    public static RoundButton dark(String t) { return new RoundButton(t, CHOC, DARK, Color.WHITE, null); }
    public static RoundButton danger(String t) { return new RoundButton(t, RED, RED.darker(), Color.WHITE, null); }
    public static RoundButton ghost(String t) { return new RoundButton(t, CLEAR, LINE, CHOC, null); }
    public static RoundButton ghostLight(String t) {
        return new RoundButton(t, CLEAR, new Color(255, 255, 255, 40), Color.WHITE, new Color(255, 255, 255, 90));
    }

    public static JButton link(String text) {
        JButton b = new JButton(text);
        b.setFont(BOLD);
        b.setForeground(CARAMEL_DARK);
        b.setBorder(new EmptyBorder(2, 2, 2, 2));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ------------------------------------------------------------------ panels
    public static class RoundPanel extends JPanel {
        private final int arc;
        private final Color fill;
        private final boolean shadow;

        public RoundPanel(int arc, Color fill, boolean shadow) {
            this.arc = arc;
            this.fill = fill;
            this.shadow = shadow;
            setOpaque(false);
        }

        public RoundPanel pad(int p) {
            setBorder(new EmptyBorder(p, p, p + (shadow ? 6 : 0), p));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            int sh = shadow ? 6 : 0;
            int w = getWidth(), h = getHeight() - sh;
            if (shadow) {
                for (int i = 1; i <= sh; i++) {
                    g2.setColor(new Color(70, 35, 10, 9));
                    g2.fillRoundRect(i / 2, i, w - i, h, arc, arc);
                }
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, arc, arc);
            g2.setColor(LINE);
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class GradientPanel extends JPanel {
        private final Color a, b;

        public GradientPanel(Color a, Color b) { this.a = a; this.b = b; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, a, w, h, b));
            g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(255, 255, 255, 12));
            g2.fillOval(-90, h - 250, 360, 360);
            g2.fillOval(w - 170, -100, 300, 300);
            g2.setColor(new Color(224, 159, 62, 26));
            g2.fillOval(w / 2, h / 2 - 40, 220, 220);
            g2.dispose();
        }
    }

    /** A panel that is happy inside a JScrollPane (follows the viewport width). */
    public static class ScrollPanel extends JPanel implements Scrollable {
        public ScrollPanel() { setOpaque(false); }
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 24; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 140; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    // ------------------------------------------------------------------ chocolate artwork
    public static class ChocoArt extends JComponent {
        private final Color wrap;

        public ChocoArt(Color wrap, int w, int h) {
            this.wrap = wrap;
            setPreferredSize(new Dimension(w, h));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            draw((Graphics2D) g, 0, 0, getWidth(), getHeight(), wrap);
        }

        /** Draws a wrapped chocolate bar. No image files needed. */
        public static void draw(Graphics2D g0, int x, int y, int w, int h, Color wrap) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.rotate(Math.toRadians(-7), x + w / 2.0, y + h / 2.0);
            int bw = (int) (w * 0.62), bh = (int) (h * 0.86);
            int bx = x + (w - bw) / 2, by = y + (h - bh) / 2;
            int arc = Math.max(8, bw / 6);

            g.setColor(new Color(40, 20, 5, 38));
            g.fillRoundRect(bx + 4, by + 7, bw, bh, arc, arc);

            int topH = (int) (bh * 0.34);
            g.setPaint(new GradientPaint(bx, by, new Color(0x7A4630), bx, by + topH + 10, new Color(0x4A2618)));
            g.fillRoundRect(bx, by, bw, topH + 14, arc, arc);
            int gap = Math.max(2, bw / 24);
            int cols = 3, sw = (bw - gap * (cols + 1)) / cols, sh = topH - gap - 6;
            for (int c = 0; c < cols; c++) {
                int sx = bx + gap + c * (sw + gap);
                g.setColor(new Color(0x93593C));
                g.fillRoundRect(sx, by + gap, sw, sh, 6, 6);
                g.setColor(new Color(255, 255, 255, 40));
                g.fillRoundRect(sx + 2, by + gap + 2, Math.max(1, sw - 4), 4, 4, 4);
            }

            int wy = by + topH, wh = by + bh - wy;
            g.setPaint(new GradientPaint(bx, wy, wrap.brighter(), bx + bw, wy + wh, wrap.darker()));
            g.fillRoundRect(bx, wy, bw, wh, arc, arc);

            Path2D z = new Path2D.Double();
            int teeth = 8;
            double tw = bw / (double) teeth;
            z.moveTo(bx, wy + 6);
            for (int i = 0; i < teeth; i++) {
                z.lineTo(bx + tw * i + tw / 2, wy - 6);
                z.lineTo(bx + tw * (i + 1), wy + 6);
            }
            z.closePath();
            g.setPaint(new GradientPaint(bx, wy, new Color(0xF2F2F5), bx + bw, wy, new Color(0xBFC0C8)));
            g.fill(z);

            int lh = (int) (wh * 0.36), ly = wy + (wh - lh) / 2 + 5;
            int lx = bx + bw / 9, lw = bw - 2 * (bw / 9);
            g.setColor(new Color(255, 246, 232));
            g.fillRoundRect(lx, ly, lw, lh, 8, 8);
            g.setColor(wrap.darker());
            g.fillRoundRect(lx + 7, ly + lh / 2 - 3, Math.max(4, lw / 2), 5, 5, 5);
            g.setColor(CARAMEL);
            g.fillOval(lx + lw - 16, ly + lh / 2 - 5, 10, 10);
            g.setColor(new Color(255, 255, 255, 50));
            g.fillRoundRect(bx + 5, wy + 12, 7, Math.max(4, wh - 24), 6, 6);
            g.dispose();
        }
    }

    // ------------------------------------------------------------------ inputs & misc
    private static Border fieldBorder(boolean focus) {
        return focus
                ? new CompoundBorder(new LineBorder(CARAMEL, 2), new EmptyBorder(8, 12, 8, 12))
                : new CompoundBorder(new LineBorder(LINE, 1), new EmptyBorder(9, 13, 9, 13));
    }

    public static void styleField(JTextField f) {
        f.setFont(BODY);
        f.setForeground(TEXT);
        f.setCaretColor(CHOC);
        f.setBackground(Color.WHITE);
        f.setBorder(fieldBorder(false));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { f.setBorder(fieldBorder(true)); }
            public void focusLost(FocusEvent e) { f.setBorder(fieldBorder(false)); }
        });
    }

    public static void onChange(JTextComponent c, Runnable r) {
        c.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { r.run(); }
            public void removeUpdate(DocumentEvent e) { r.run(); }
            public void changedUpdate(DocumentEvent e) { r.run(); }
        });
    }

    public static String rulesHtml(String p) {
        String[] names = {"6+ characters", "a-z", "A-Z", "0-9", "symbol"};
        boolean[] ok = {p.length() >= 6, p.matches(".*[a-z].*"), p.matches(".*[A-Z].*"),
                        p.matches(".*[0-9].*"), p.matches(".*[^A-Za-z0-9\\s].*")};
        StringBuilder sb = new StringBuilder("<html>");
        for (int i = 0; i < names.length; i++) {
            sb.append("<span style='color:").append(ok[i] ? "#2E9E67" : "#B3A296").append("'>&bull; ")
              .append(names[i]).append("</span>&nbsp;&nbsp; ");
        }
        return sb.append("</html>").toString();
    }

    /** Live password checklist that turns green as the rules are met. */
    public static void liveRules(JPasswordField pf, JLabel out) {
        Runnable r = () -> out.setText(rulesHtml(new String(pf.getPassword())));
        onChange(pf, r);
        r.run();
    }

    public static void styleScroll(JScrollPane sp) {
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollBar bar = sp.getVerticalScrollBar();
        bar.setUnitIncrement(24);
        bar.setPreferredSize(new Dimension(10, 0));
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() { thumbColor = new Color(0xD5BFA8); }
            @Override protected JButton createDecreaseButton(int o) { return zero(); }
            @Override protected JButton createIncreaseButton(int o) { return zero(); }
            private JButton zero() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = smooth(g);
                g2.setColor(thumbColor);
                g2.fillRoundRect(r.x + 2, r.y, r.width - 4, r.height, 8, 8);
                g2.dispose();
            }
        });
    }
}
