import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// Shop owner dashboard: manage products, membership and password.
public class OwnerPanel extends JPanel {
    private static final String[] PAGES = {"products", "membership", "password"};
    private static final String[] TITLES = {"Products", "Membership", "Change password"};

    private final Main app;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Theme.RoundButton[] nav = new Theme.RoundButton[3];
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"#", "Product", "Price", "Discount", "Final price"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private JLabel memberSummary;

    public OwnerPanel(Main app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Theme.CREAM);
        add(sidebar(), BorderLayout.WEST);
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(28, 32, 28, 32));
        content.add(productsPage(), PAGES[0]);
        content.add(membershipPage(), PAGES[1]);
        content.add(passwordPage(), PAGES[2]);
        add(content, BorderLayout.CENTER);
        go(0);
    }

    private void go(int i) {
        cards.show(content, PAGES[i]);
        for (int k = 0; k < nav.length; k++) nav[k].setActive(k == i);
    }

    private JComponent sidebar() {
        Theme.GradientPanel p = new Theme.GradientPanel(Theme.DARK, Theme.CHOC);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(26, 20, 22, 20));
        p.setPreferredSize(new Dimension(250, 0));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JComponent art = new Theme.ChocoArt(Theme.CARAMEL, 84, 76);
        art.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel t = Theme.label("Owner Panel", Theme.font(Font.BOLD, 22), Color.WHITE);
        JLabel s = Theme.label("Signed in as " + app.owner.name(), Theme.SMALL, new Color(0xE9D9CB));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(art);
        top.add(Box.createVerticalStrut(8));
        top.add(t);
        top.add(s);
        top.add(Box.createVerticalStrut(26));
        for (int i = 0; i < nav.length; i++) {
            int idx = i;
            nav[i] = Theme.ghostLight(TITLES[i]);
            nav[i].setHorizontalAlignment(SwingConstants.LEFT);
            nav[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            nav[i].setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            nav[i].addActionListener(e -> go(idx));
            top.add(nav[i]);
            top.add(Box.createVerticalStrut(8));
        }
        p.add(top, BorderLayout.NORTH);

        Theme.RoundButton out = Theme.ghostLight("Logout");
        out.addActionListener(e -> app.showLogin());
        p.add(out, BorderLayout.SOUTH);
        return p;
    }

    private JPanel page(String title, String sub, JComponent body) {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setOpaque(false);
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JLabel t = Theme.label(title, Theme.font(Font.BOLD, 26), Theme.DARK);
        JLabel s = Theme.label(sub, Theme.BODY, Theme.MUTED);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(t);
        head.add(s);
        p.add(head, BorderLayout.NORTH);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private JComponent formCard(JComponent inner) {
        Theme.RoundPanel card = new Theme.RoundPanel(22, Color.WHITE, true).pad(26);
        card.setLayout(new BorderLayout(0, 12));
        card.add(inner);
        card.setPreferredSize(new Dimension(460, card.getPreferredSize().height));
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.setOpaque(false);
        wrap.add(card);
        return wrap;
    }

    private static int parse(String s, int min, int max) {
        try {
            int v = Integer.parseInt(s.trim());
            return v >= min && v <= max ? v : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ------------------------------------------------------------ products
    private JPanel productsPage() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);

        Theme.RoundButton add = Theme.primary("Add product");
        Theme.RoundButton disc = Theme.dark("Set discount");
        Theme.RoundButton rem = Theme.danger("Remove");
        add.addActionListener(e -> addProduct());
        disc.addActionListener(e -> setDiscount());
        rem.addActionListener(e -> removeProduct());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);
        btns.add(add);
        btns.add(disc);
        btns.add(rem);
        body.add(btns, BorderLayout.NORTH);

        table.setRowHeight(44);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(Theme.BODY);
        table.setForeground(Theme.TEXT);
        table.setSelectionBackground(new Color(0xFBE3BD));
        table.setSelectionForeground(Theme.DARK);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, false, r, c);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                if (!sel) setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xFFFAF3));
                return this;
            }
        });
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, false, false, r, c);
                setBackground(Theme.CHOC);
                setForeground(Color.WHITE);
                setFont(Theme.BOLD);
                setBorder(new EmptyBorder(11, 14, 11, 14));
                return this;
            }
        });
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        Theme.RoundPanel box = new Theme.RoundPanel(18, Color.WHITE, false);
        box.setLayout(new BorderLayout());
        box.setBorder(new EmptyBorder(4, 4, 4, 4));
        JScrollPane sp = new JScrollPane(table);
        Theme.styleScroll(sp);
        sp.getViewport().setOpaque(true);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(null);
        sp.setColumnHeaderView(table.getTableHeader());
        box.add(sp);
        body.add(box, BorderLayout.CENTER);
        reload();
        return page("Products", "Add, remove and discount the chocolates customers see.", body);
    }

    private void reload() {
        model.setRowCount(0);
        for (int i = 0; i < app.catalog.size(); i++) {
            Product p = app.catalog.get(i);
            model.addRow(new Object[]{i + 1, p.getName(), p.getPrice() + " Taka",
                    p.getDiscountPercent() > 0 ? p.getDiscountPercent() + "% OFF" : "-", p.getFinalPrice() + " Taka"});
        }
    }

    private int selected() {
        int row = table.getSelectedRow();
        if (row < 0) Dialogs.message(this, "Select a product", "Please click a product in the list first.", false);
        return row;
    }

    private void addProduct() {
        String[] v = Dialogs.form(this, "Add product", "Enter the new chocolate's details.", "Add",
                x -> {
                    if (x[0].isEmpty()) return "Name cannot be empty.";
                    if (parse(x[1], 1, 1000000) < 0) return "Price must be a whole number between 1 and 1000000.";
                    return null;
                }, "Product name", "Price in Taka");
        if (v == null) return;
        app.catalog.add(v[0], Integer.parseInt(v[1]));
        reload();
    }

    private void removeProduct() {
        int row = selected();
        if (row < 0) return;
        String name = app.catalog.get(row).getName();
        if (Dialogs.confirm(this, "Remove product", "Remove \"" + name + "\" from the shop?", "Remove")) {
            app.catalog.remove(row);
            reload();
        }
    }

    private void setDiscount() {
        int row = selected();
        if (row < 0) return;
        String[] v = Dialogs.form(this, "Product discount", "Discount for " + app.catalog.get(row).getName()
                + ". Use 0 to remove the discount.", "Save",
                x -> parse(x[0], 0, 90) < 0 ? "Discount must be a whole number from 0 to 90." : null,
                "Discount percent (0-90)");
        if (v == null) return;
        app.catalog.setDiscount(row, Integer.parseInt(v[0]));
        reload();
        table.setRowSelectionInterval(row, row);
    }

    // ------------------------------------------------------------ membership
    private JPanel membershipPage() {
        Form f = new Form();
        JTextField fee = f.text("Membership fee (Taka)");
        JTextField pct = f.text("Member discount (% off every order, 1-90)");
        fee.setText(String.valueOf(app.membership.getFee()));
        pct.setText(String.valueOf(app.membership.getDiscountPercent()));
        memberSummary = Theme.label("", Theme.BOLD, Theme.CARAMEL_DARK);
        updateSummary();
        Theme.RoundButton save = Theme.primary("Save changes");
        save.addActionListener(e -> {
            int a = parse(f.get(0), 1, 100000), b = parse(f.get(1), 1, 90);
            if (a < 0) { f.showError("Fee must be a whole number from 1 to 100000 Taka."); return; }
            if (b < 0) { f.showError("Discount must be a whole number from 1 to 90."); return; }
            app.membership.setFee(a);
            app.membership.setDiscountPercent(b);
            f.clearError();
            updateSummary();
            Dialogs.message(this, "Saved", "Membership is now " + a + " Taka with " + b + "% off every order.", false);
        });
        f.onEnter(save::doClick);
        JPanel inner = new JPanel(new BorderLayout(0, 14));
        inner.setOpaque(false);
        inner.add(memberSummary, BorderLayout.NORTH);
        inner.add(f, BorderLayout.CENTER);
        inner.add(save, BorderLayout.SOUTH);
        return page("Membership", "Set what members pay and how much they save.", formCard(inner));
    }

    private void updateSummary() {
        memberSummary.setText("Current: " + app.membership.getFee() + " Taka fee, "
                + app.membership.getDiscountPercent() + "% discount");
    }

    // ------------------------------------------------------------ password
    private JPanel passwordPage() {
        Form f = new Form();
        f.password("Current password");
        f.password("New password");
        f.password("Confirm new password");
        Theme.RoundButton save = Theme.primary("Change password");
        save.addActionListener(e -> {
            if (!app.owner.verify(f.get(0))) { f.showError("Current password is incorrect."); return; }
            List<String> pr = AuthService.passwordProblems(f.get(1));
            if (!pr.isEmpty()) { f.showError("New password still needs: " + String.join(", ", pr)); return; }
            if (!f.get(1).equals(f.get(2))) { f.showError("Passwords do not match."); return; }
            app.owner.save(app.owner.name(), f.get(1));
            f.clear();
            Dialogs.message(this, "Password changed", "Password changed successfully!", false);
        });
        f.onEnter(save::doClick);
        JPanel inner = new JPanel(new BorderLayout(0, 12));
        inner.setOpaque(false);
        inner.add(f, BorderLayout.CENTER);
        inner.add(save, BorderLayout.SOUTH);
        return page("Change password", "Keep the owner account safe.", formCard(inner));
    }
}
