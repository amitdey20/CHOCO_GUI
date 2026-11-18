import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

// Chocolala Land - Swing GUI.
public class Main extends JFrame {
    public final AuthService auth = new AuthService();
    public final Catalog catalog = new Catalog();
    public final Membership membership = new Membership();
    public final OwnerService owner = new OwnerService();

    public Main() {
        super("Chocolala Land");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        BufferedImage icon = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        Theme.ChocoArt.draw(g, 0, 0, 64, 64, Theme.CARAMEL);
        g.dispose();
        setIconImage(icon);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1080, 720));
        setLocationRelativeTo(null);
        showLogin();
    }

    private void setScreen(JComponent c) {
        getContentPane().removeAll();
        getContentPane().add(c, BorderLayout.CENTER);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    public void showLogin() { setScreen(new LoginPanel(this)); }

    public void showShop(User u) { setScreen(new ShopPanel(this, u)); }

    public void showOwner() { setScreen(new OwnerPanel(this)); }

    // First time: register the owner. Then ask for the owner password (3 attempts).
    public void ownerLogin() {
        if (!owner.exists()) {
            String[] v = Dialogs.form(this, "Shop owner registration", "First time here - create the owner account.",
                    "Create account", x -> {
                        if (x[0].length() < 2) return "Name must be at least 2 characters.";
                        List<String> pr = AuthService.passwordProblems(x[1]);
                        if (!pr.isEmpty()) return "Password still needs: " + String.join(", ", pr);
                        if (!x[1].equals(x[2])) return "Passwords do not match.";
                        return null;
                    }, "Owner name", "*Create password", "*Confirm password");
            if (v == null) return;
            owner.save(v[0], v[1]);
            Dialogs.message(this, "Owner registered", "Owner registration successful! Please sign in with your password.", false);
        }
        int[] tries = {0};
        String[] v = Dialogs.form(this, "Shop owner login", "Enter the owner password to continue.", "Sign in",
                x -> {
                    if (owner.verify(x[0])) return null;
                    tries[0]++;
                    return tries[0] >= 3 ? "!Too many wrong attempts. Access denied."
                            : "Incorrect password. Attempts remaining: " + (3 - tries[0]);
                }, "*Owner password");
        if (v != null) showOwner();
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) { }
            new Main().setVisible(true);
        });
    }
}
