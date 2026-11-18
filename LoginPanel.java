import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

// Welcome screen: login / register / forgot password.
public class LoginPanel extends JPanel {
    private final Main app;
    // the card is only as tall as the page being shown
    private final CardLayout cards = new CardLayout() {
        @Override
        public Dimension preferredLayoutSize(Container parent) {
            for (Component c : parent.getComponents()) {
                if (c.isVisible()) return c.getPreferredSize();
            }
            return super.preferredLayoutSize(parent);
        }
    };
    private final JPanel stack = new JPanel(cards);
    private final Theme.RoundButton tabLogin = Theme.ghost("Sign in");
    private final Theme.RoundButton tabRegister = Theme.ghost("Create account");
    private int failed = 0;

    public LoginPanel(Main app) {
        this.app = app;
        setLayout(new BorderLayout());
        add(hero(), BorderLayout.WEST);
        add(right(), BorderLayout.CENTER);
        show("login");
    }

    private JComponent hero() {
        Theme.GradientPanel p = new Theme.GradientPanel(Theme.DARK, Theme.CHOC);
        p.setPreferredSize(new Dimension(470, 0));
        p.setLayout(new GridBagLayout());
        JPanel c = new JPanel();
        c.setOpaque(false);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        Theme.ChocoArt art = new Theme.ChocoArt(Theme.CARAMEL, 260, 230);
        art.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.add(art);
        c.add(Box.createVerticalStrut(10));
        c.add(center(Theme.label("CHOCOLALA LAND", Theme.font(Font.BOLD, 36), Color.WHITE)));
        c.add(Box.createVerticalStrut(8));
        c.add(center(Theme.label("Sweet moments, delivered to your door.", Theme.font(Font.PLAIN, 16), new Color(0xF0D9B5))));
        c.add(Box.createVerticalStrut(34));
        String[] perks = {"Fresh chocolates from around the world", "Member discounts on every order",
                          "Pay with bKash, Nagad, cards and more"};
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        for (String s : perks) {
            JLabel l = Theme.label("\u2022  " + s, Theme.BODY, new Color(0xE9D9CB));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(l);
            list.add(Box.createVerticalStrut(8));
        }
        list.setMaximumSize(list.getPreferredSize());
        list.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.add(list);
        p.add(c);
        return p;
    }

    private static JComponent center(JComponent c) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        return c;
    }

    private JComponent right() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Theme.CREAM);

        Theme.RoundPanel card = new Theme.RoundPanel(28, Color.WHITE, true) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(450, super.getPreferredSize().height);
            }
        }.pad(30);
        card.setLayout(new BorderLayout(0, 18));

        JPanel tabs = new JPanel(new GridLayout(1, 2, 8, 0));
        tabs.setOpaque(false);
        tabs.add(tabLogin);
        tabs.add(tabRegister);
        tabLogin.addActionListener(e -> show("login"));
        tabRegister.addActionListener(e -> show("register"));
        card.add(tabs, BorderLayout.NORTH);

        stack.setOpaque(false);
        stack.add(loginPage(), "login");
        stack.add(registerPage(), "register");
        stack.add(forgotPage(), "forgot");
        card.add(stack, BorderLayout.CENTER);

        JButton owner = Theme.link("Shop owner? Sign in here");
        owner.addActionListener(e -> app.ownerLogin());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        south.setOpaque(false);
        south.add(owner);
        card.add(south, BorderLayout.SOUTH);

        outer.add(card);
        return outer;
    }

    private void show(String name) {
        cards.show(stack, name);
        revalidate();
        repaint();
        tabLogin.setActive(name.equals("login"));
        tabRegister.setActive(name.equals("register"));
    }

    private JPanel header(String title, String sub) {
        JPanel h = new JPanel();
        h.setOpaque(false);
        h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
        JLabel t = Theme.label(title, Theme.font(Font.BOLD, 24), Theme.DARK);
        JLabel s = Theme.label(sub, Theme.BODY, Theme.MUTED);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        h.add(t);
        h.add(Box.createVerticalStrut(2));
        h.add(s);
        h.add(Box.createVerticalStrut(16));
        return h;
    }

    // ------------------------------------------------------------ login
    private JPanel loginPage() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(header("Welcome back", "Sign in to continue shopping"), BorderLayout.NORTH);

        Form f = new Form();
        f.text("Username or phone number");
        f.password("Password");
        p.add(f, BorderLayout.CENTER);

        Theme.RoundButton go = Theme.primary("Sign in");
        JButton forgot = Theme.link("Forgot password?");
        forgot.addActionListener(e -> show("forgot"));
        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        south.add(go, BorderLayout.NORTH);
        JPanel fp = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        fp.setOpaque(false);
        fp.add(forgot);
        south.add(fp, BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);

        go.addActionListener(e -> {
            String id = f.get(0), pw = f.get(1);
            if (id.isEmpty() || pw.isEmpty()) { f.showError("Please enter your username/phone and password."); return; }
            if (!app.auth.hasUsers()) { f.showError("No registered accounts found. Please register first."); return; }
            User u = app.auth.login(id, pw);
            if (u != null) {
                f.clear();
                failed = 0;
                app.showShop(u);
                return;
            }
            failed++;
            if (failed >= 3) {
                failed = 0;
                f.showError("Too many wrong attempts. If you forgot your password, use 'Forgot password?'.");
            } else {
                f.showError("Incorrect username/phone or password. Attempts remaining: " + (3 - failed));
            }
        });
        f.onEnter(go::doClick);
        return p;
    }

    // ------------------------------------------------------------ register
    private JPanel registerPage() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(header("Join Chocolala Land", "Create your account in a minute"), BorderLayout.NORTH);

        Form f = new Form();
        f.text("Full name");
        f.text("Mobile number (01XXXXXXXXX)");
        f.text("Username (3-20 letters, numbers, _)");
        JPasswordField pw = f.password("Password");
        f.password("Confirm password");
        JLabel rules = Theme.label("", Theme.SMALL, Theme.MUTED);
        Theme.liveRules(pw, rules);
        JPanel mid = new JPanel(new BorderLayout(0, 4));
        mid.setOpaque(false);
        mid.add(f, BorderLayout.CENTER);
        mid.add(rules, BorderLayout.SOUTH);
        p.add(mid, BorderLayout.CENTER);

        Theme.RoundButton go = Theme.primary("Verify phone & create account");
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(10, 0, 0, 0));
        south.add(go);
        p.add(south, BorderLayout.SOUTH);

        go.addActionListener(e -> {
            String name = f.get(0), phone = f.get(1), user = f.get(2), pass = f.get(3), pass2 = f.get(4);
            if (name.length() < 2) { f.showError("Name must be at least 2 characters."); return; }
            if (!AuthService.isValidPhone(phone)) { f.showError("Use a valid BD mobile number (11 digits)."); return; }
            if (app.auth.phoneTaken(phone)) { f.showError("This phone number is already registered. Use another one or log in."); return; }
            String ue = app.auth.usernameError(user);
            if (ue != null) { f.showError(ue); return; }
            List<String> problems = AuthService.passwordProblems(pass);
            if (!problems.isEmpty()) { f.showError("Password still needs: " + String.join(", ", problems)); return; }
            if (!pass.equals(pass2)) { f.showError("Passwords do not match."); return; }
            f.clearError();
            if (!Dialogs.otp(this, phone)) {
                f.showError("Phone number was not verified. Registration cancelled.");
                return;
            }
            User u = app.auth.register(name, phone, user, pass);
            Dialogs.message(this, "Registration successful", "Welcome, " + u.getName() + "!", false);
            f.clear();
            app.showShop(u);
        });
        return p;
    }

    // ------------------------------------------------------------ forgot password
    private JPanel forgotPage() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(header("Reset password", "We will verify your phone number first"), BorderLayout.NORTH);

        Form f = new Form();
        f.text("Mobile number you registered with");
        p.add(f, BorderLayout.CENTER);

        Theme.RoundButton go = Theme.primary("Send OTP");
        JButton back = Theme.link("Back to sign in");
        back.addActionListener(e -> show("login"));
        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        south.add(go, BorderLayout.NORTH);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bp.setOpaque(false);
        bp.add(back);
        south.add(bp, BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);

        go.addActionListener(e -> {
            String phone = f.get(0);
            if (!AuthService.isValidPhone(phone)) { f.showError("Use a valid BD mobile number (11 digits)."); return; }
            if (!app.auth.hasUsers()) { f.showError("No registered accounts found. Please register first."); return; }
            String username = app.auth.usernameOf(phone);
            if (username == null) { f.showError("No account found with that phone number."); return; }
            f.clearError();
            if (!Dialogs.otp(this, phone)) { f.showError("Password reset failed - OTP was not verified."); return; }
            String[] v = Dialogs.form(this, "Create new password", "Your username is: " + username,
                    "Change password", x -> {
                        List<String> pr = AuthService.passwordProblems(x[0]);
                        if (!pr.isEmpty()) return "Password still needs: " + String.join(", ", pr);
                        if (!x[0].equals(x[1])) return "Passwords do not match.";
                        return null;
                    }, "*New password", "*Confirm new password");
            if (v == null) return;
            app.auth.resetPassword(phone, v[0]);
            f.clear();
            Dialogs.message(this, "Password changed", "Password changed successfully! Please sign in with your new password.", false);
            show("login");
        });
        f.onEnter(go::doClick);
        return p;
    }
}
