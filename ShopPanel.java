import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

// Customer screen: product cards, live cart, membership, checkout.
public class ShopPanel extends JPanel {
    private final Main app;
    private final User user;
    private final Cart cart = new Cart();
    private boolean member;

    private final JPanel grid = new JPanel(new GridLayout(0, 3, 18, 18));
    private JTextField search;
    private JPanel cartItems;
    private JLabel countLbl, subLbl, discLbl, totalLbl;
    private JPanel discRow;
    private Theme.RoundButton checkout;

    public ShopPanel(Main app, User user) {
        this.app = app;
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(Theme.CREAM);
        build();
    }

    private int pct() { return app.membership.getDiscountPercent(); }

    private void build() {
        removeAll();
        member = app.membership.isMember(user.getPhone());
        add(header(), BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(18, 26, 14, 20));
        center.add(banner(), BorderLayout.NORTH);
        center.add(productsArea(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
        add(cartPanel(), BorderLayout.EAST);
        refreshCart();
        revalidate();
        repaint();
    }

    // ------------------------------------------------------------ header & banner
    private JComponent header() {
        Theme.GradientPanel h = new Theme.GradientPanel(Theme.DARK, Theme.CHOC);
        h.setLayout(new BorderLayout());
        h.setBorder(new EmptyBorder(10, 26, 10, 26));

        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logo.setOpaque(false);
        logo.add(new Theme.ChocoArt(Theme.CARAMEL, 44, 44));
        logo.add(Theme.label("CHOCOLALA LAND", Theme.font(Font.BOLD, 21), Color.WHITE));
        h.add(logo, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        right.setOpaque(false);
        Theme.RoundPanel badge = new Theme.RoundPanel(14, member ? Theme.CARAMEL : new Color(255, 255, 255, 40), false);
        badge.setLayout(new BorderLayout());
        badge.setBorder(new EmptyBorder(4, 12, 4, 12));
        badge.add(Theme.label(member ? "MEMBER" : "Not a member", Theme.SMALL_BOLD, Color.WHITE));
        right.add(badge);
        right.add(Theme.label("Hi, " + user.getName(), Theme.BOLD, Color.WHITE));
        Theme.RoundButton out = Theme.ghostLight("Logout");
        out.compact();
        out.addActionListener(e -> {
            if (cart.count() == 0 || Dialogs.confirm(this, "Logout", "Your cart will be emptied. Log out?", "Logout")) {
                app.showLogin();
            }
        });
        right.add(out);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    private JComponent banner() {
        Color fill = member ? new Color(0xE3F5EA) : new Color(0xFFEFD2);
        Theme.RoundPanel b = new Theme.RoundPanel(18, fill, false);
        b.setLayout(new BorderLayout(12, 0));
        b.setBorder(new EmptyBorder(10, 18, 10, 14));
        if (member) {
            b.add(Theme.label("Membership active - " + pct() + "% off every order", Theme.BOLD, new Color(0x1F7A4D)));
        } else {
            b.add(Theme.label("Become a member for " + app.membership.getFee() + " Taka and save "
                    + pct() + "% on every order", Theme.BOLD, Theme.CHOC));
            Theme.RoundButton join = Theme.primary("Join now");
            join.compact();
            join.addActionListener(e -> buyMembership());
            b.add(join, BorderLayout.EAST);
        }
        return b;
    }

    // ------------------------------------------------------------ products
    private JComponent productsArea() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(Theme.label("Our Chocolates", Theme.font(Font.BOLD, 24), Theme.DARK), BorderLayout.WEST);
        search = new JTextField(16);
        Theme.styleField(search);
        search.setToolTipText("Search chocolates");
        JPanel sb = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sb.setOpaque(false);
        sb.add(Theme.label("Search", Theme.SMALL_BOLD, Theme.MUTED));
        sb.add(search);
        top.add(sb, BorderLayout.EAST);
        Theme.onChange(search, this::fillGrid);
        p.add(top, BorderLayout.NORTH);

        grid.setOpaque(false);
        Theme.ScrollPanel holder = new Theme.ScrollPanel();
        holder.setLayout(new BorderLayout());
        holder.setBorder(new EmptyBorder(2, 2, 8, 6));
        holder.add(grid, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(holder);
        Theme.styleScroll(sp);
        p.add(sp, BorderLayout.CENTER);
        fillGrid();
        return p;
    }

    private void fillGrid() {
        grid.removeAll();
        String q = search == null ? "" : search.getText().trim().toLowerCase();
        int shown = 0;
        for (int i = 0; i < app.catalog.size(); i++) {
            Product p = app.catalog.get(i);
            if (!q.isEmpty() && !p.getName().toLowerCase().contains(q)) continue;
            grid.add(productCard(p));
            shown++;
        }
        // keep cards a normal size when only a few are shown
        for (int i = shown; i > 0 && i < 3; i++) {
            JPanel spacer = new JPanel();
            spacer.setOpaque(false);
            grid.add(spacer);
        }
        if (shown == 0) {
            JLabel none = Theme.label(app.catalog.size() == 0 ? "No products available right now."
                    : "No chocolate matches your search.", Theme.BODY, Theme.MUTED);
            grid.add(none);
        }
        grid.revalidate();
        grid.repaint();
    }

    private JComponent productCard(Product p) {
        Theme.RoundPanel c = new Theme.RoundPanel(22, Color.WHITE, true).pad(14);
        c.setLayout(new BorderLayout(0, 6));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        top.setOpaque(false);
        top.setPreferredSize(new Dimension(10, 26));
        if (p.getDiscountPercent() > 0) {
            Theme.RoundPanel badge = new Theme.RoundPanel(12, Theme.PINK, false);
            badge.setLayout(new BorderLayout());
            badge.setBorder(new EmptyBorder(3, 9, 3, 9));
            badge.add(Theme.label("-" + p.getDiscountPercent() + "% OFF", Theme.SMALL_BOLD, Color.WHITE));
            top.add(badge);
        }
        c.add(top, BorderLayout.NORTH);
        c.add(new Theme.ChocoArt(Theme.wrapFor(p.getName()), 150, 120), BorderLayout.CENTER);

        JPanel info = new JPanel(new GridLayout(0, 1, 0, 6));
        info.setOpaque(false);
        JLabel name = Theme.label(p.getName(), Theme.font(Font.BOLD, 16), Theme.DARK);
        name.setHorizontalAlignment(SwingConstants.CENTER);
        String price = p.getDiscountPercent() > 0
                ? "<html><b style='color:#C17F22;font-size:14px'>" + p.getFinalPrice() + " Taka</b> &nbsp;<span style='color:#8D786B'><s>"
                  + p.getPrice() + "</s></span></html>"
                : "<html><b style='color:#C17F22;font-size:14px'>" + p.getPrice() + " Taka</b></html>";
        JLabel pl = new JLabel(price);
        pl.setFont(Theme.BODY);
        pl.setHorizontalAlignment(SwingConstants.CENTER);
        Theme.RoundButton add = Theme.primary("Add to Cart");
        add.addActionListener(e -> { cart.add(p); refreshCart(); });
        info.add(name);
        info.add(pl);
        info.add(add);
        c.add(info, BorderLayout.SOUTH);
        return c;
    }

    // ------------------------------------------------------------ cart
    private JComponent cartPanel() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Color.WHITE);
        side.setPreferredSize(new Dimension(360, 0));
        side.setBorder(new MatteBorder(0, 1, 0, 0, Theme.LINE));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(20, 22, 10, 22));
        head.add(Theme.label("Your Cart", Theme.font(Font.BOLD, 22), Theme.DARK), BorderLayout.WEST);
        countLbl = Theme.label("0 items", Theme.BODY, Theme.MUTED);
        head.add(countLbl, BorderLayout.EAST);
        side.add(head, BorderLayout.NORTH);

        cartItems = new Theme.ScrollPanel();
        cartItems.setLayout(new BoxLayout(cartItems, BoxLayout.Y_AXIS));
        cartItems.setBorder(new EmptyBorder(0, 22, 0, 16));
        JScrollPane sp = new JScrollPane(cartItems);
        Theme.styleScroll(sp);
        side.add(sp, BorderLayout.CENTER);

        JPanel sum = new JPanel();
        sum.setBackground(new Color(0xFFFAF3));
        sum.setLayout(new BoxLayout(sum, BoxLayout.Y_AXIS));
        sum.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Theme.LINE), new EmptyBorder(16, 22, 20, 22)));
        subLbl = Theme.label("", Theme.BODY, Theme.TEXT);
        discLbl = Theme.label("", Theme.BODY, Theme.GREEN);
        totalLbl = Theme.label("", Theme.font(Font.BOLD, 22), Theme.DARK);
        sum.add(row("Subtotal", subLbl, Theme.MUTED));
        discRow = row("Member discount (" + pct() + "%)", discLbl, Theme.GREEN);
        sum.add(discRow);
        sum.add(Box.createVerticalStrut(6));
        sum.add(row("Total", totalLbl, Theme.DARK));
        sum.add(Box.createVerticalStrut(14));
        checkout = Theme.primary("Checkout & Pay");
        checkout.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        checkout.addActionListener(e -> checkout());
        sum.add(checkout);
        side.add(sum, BorderLayout.SOUTH);
        return side;
    }

    private JPanel row(String left, JLabel value, Color leftColor) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);
        r.setAlignmentX(Component.LEFT_ALIGNMENT);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        r.add(Theme.label(left, Theme.BODY, leftColor), BorderLayout.WEST);
        r.add(value, BorderLayout.EAST);
        return r;
    }

    private void refreshCart() {
        cartItems.removeAll();
        if (cart.count() == 0) {
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            empty.setBorder(new EmptyBorder(50, 0, 0, 0));
            Theme.ChocoArt art = new Theme.ChocoArt(new Color(0xC9B8A8), 110, 96);
            art.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel l1 = Theme.label("Your cart is empty", Theme.H2, Theme.DARK);
            JLabel l2 = Theme.label("Pick some chocolate you love!", Theme.BODY, Theme.MUTED);
            l1.setAlignmentX(Component.CENTER_ALIGNMENT);
            l2.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(art);
            empty.add(Box.createVerticalStrut(10));
            empty.add(l1);
            empty.add(Box.createVerticalStrut(4));
            empty.add(l2);
            cartItems.add(empty);
        } else {
            for (Map.Entry<Product, Integer> e : new ArrayList<>(cart.items().entrySet())) {
                cartItems.add(cartRow(e.getKey(), e.getValue()));
            }
            cartItems.add(Box.createVerticalGlue());
        }
        int n = cart.count();
        countLbl.setText(n + (n == 1 ? " item" : " items"));
        int sub = cart.subtotal(), disc = cart.memberDiscount(member, pct());
        subLbl.setText(sub + " Taka");
        discLbl.setText("-" + disc + " Taka");
        discRow.setVisible(member);
        totalLbl.setText(cart.total(member, pct()) + " Taka");
        checkout.setEnabled(n > 0);
        cartItems.revalidate();
        cartItems.repaint();
    }

    private JComponent cartRow(Product p, int qty) {
        JPanel r = new JPanel(new BorderLayout(12, 0));
        r.setOpaque(false);
        r.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.LINE), new EmptyBorder(12, 0, 12, 0)));
        r.add(new Theme.ChocoArt(Theme.wrapFor(p.getName()), 48, 48), BorderLayout.WEST);

        JPanel mid = new JPanel();
        mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        JLabel name = Theme.label(p.getName(), Theme.BOLD, Theme.DARK);
        JLabel unit = Theme.label(p.getFinalPrice() + " Taka each", Theme.SMALL, Theme.MUTED);
        JPanel ctl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        ctl.setOpaque(false);
        Theme.RoundButton minus = Theme.ghost("-").compact();
        Theme.RoundButton plus = Theme.ghost("+").compact();
        minus.setBorder(new EmptyBorder(0, 0, 0, 0));
        plus.setBorder(new EmptyBorder(0, 0, 0, 0));
        minus.setPreferredSize(new Dimension(32, 28));
        plus.setPreferredSize(new Dimension(32, 28));
        minus.addActionListener(e -> { cart.remove(p); refreshCart(); });
        plus.addActionListener(e -> { cart.add(p); refreshCart(); });
        ctl.add(minus);
        ctl.add(Theme.label(String.valueOf(qty), Theme.BOLD, Theme.TEXT));
        ctl.add(plus);
        for (JComponent c : new JComponent[]{name, unit, ctl}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        mid.add(name);
        mid.add(unit);
        mid.add(Box.createVerticalStrut(4));
        mid.add(ctl);
        r.add(mid, BorderLayout.CENTER);
        r.add(Theme.label(qty * p.getFinalPrice() + " Taka", Theme.BOLD, Theme.CHOC), BorderLayout.EAST);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, r.getPreferredSize().height));
        return r;
    }

    // ------------------------------------------------------------ actions
    private void checkout() {
        if (cart.count() == 0) return;
        int total = cart.total(member, pct());
        if (PaymentDialog.show(this, user, total, "Order payment")) {
            Dialogs.receipt(this, "Order receipt", receiptText());
            app.showLogin();
        }
    }

    private void buyMembership() {
        int fee = app.membership.getFee();
        if (!Dialogs.confirm(this, "Chocolala Membership",
                "Membership fee: " + fee + " Taka.\nYou get " + pct() + "% off every order on this phone number ("
                        + user.getPhone() + ").\n\nBuy membership?", "Buy now")) return;
        if (PaymentDialog.show(this, user, fee, "Membership fee") && app.membership.join(user.getPhone())) {
            Dialogs.message(this, "Welcome to the club!", "Congratulations! You are now a member. "
                    + pct() + "% discount will apply to your orders.", false);
            build();
        }
    }

    private String receiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("        CHOCOLALA LAND - ORDER RECEIPT\n");
        sb.append("==========================================\n");
        sb.append(String.format("Customer   : %s%n", user.getName()));
        sb.append(String.format("Phone      : %s%n", user.getPhone()));
        sb.append(String.format("Membership : %s%n", member ? "Member (" + pct() + "% off)" : "No"));
        sb.append("------------------------------------------\n");
        for (Map.Entry<Product, Integer> e : cart.items().entrySet()) {
            Product p = e.getKey();
            sb.append(String.format("%-22s x%-2d %6d Taka%n", p.getName(), e.getValue(), e.getValue() * p.getFinalPrice()));
        }
        sb.append("------------------------------------------\n");
        sb.append(String.format("Subtotal           : %8d Taka%n", cart.subtotal()));
        if (member) sb.append(String.format("Member discount    : %8d Taka%n", -cart.memberDiscount(true, pct())));
        sb.append(String.format("TOTAL PAID         : %8d Taka%n", cart.total(member, pct())));
        sb.append("==========================================\n");
        sb.append("Shop     : CHOCOLALA LAND\n");
        sb.append("Location : USTC, Foy's Lake, Zakir Hossain Road,\n           Chattogram 4202\n");
        sb.append("Email    : registrar@digdig.org\n");
        sb.append("Phone    : +8809678632793\n");
        return sb.toString();
    }
}
