import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Membership (members.txt)
public class Membership {
    private int fee = 500;              // Taka
    private int discountPercent = 10;   // % off the cart

    public Membership() {
        loadSettings();
    }

    public int getFee() {
        return fee;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setFee(int fee) {
        this.fee = fee;
        saveSettings();
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
        saveSettings();
    }

    public boolean isMember(String phone) {
        try {
            if (Files.exists(Paths.get("members.txt"))) {
                for (String line : Files.readAllLines(Paths.get("members.txt"))) {
                    if (line.trim().equals(phone)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read members file.");
        }
        return false;
    }

    public boolean join(String phone) {
        try {
            List<String> lines = new ArrayList<>();
            if (Files.exists(Paths.get("members.txt"))) {
                lines.addAll(Files.readAllLines(Paths.get("members.txt")));
            }
            lines.add(phone);
            Files.write(Paths.get("members.txt"), lines);
            return true;
        } catch (IOException e) {
            System.out.println("Could not save membership.");
            return false;
        }
    }

    private void loadSettings() {
        if (!Files.exists(Paths.get("settings.txt"))) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(Paths.get("settings.txt"));
            if (!lines.isEmpty()) {
                String[] parts = lines.get(0).split("\t");
                if (parts.length == 2) {
                    fee = Integer.parseInt(parts[0]);
                    discountPercent = Integer.parseInt(parts[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read settings file.");
        } catch (NumberFormatException e) {
            System.out.println("Settings file has a wrong number.");
        }
    }

    private void saveSettings() {
        List<String> lines = new ArrayList<>();
        lines.add(fee + "\t" + discountPercent);
        try {
            Files.write(Paths.get("settings.txt"), lines);
        } catch (IOException e) {
            System.out.println("Could not save settings file.");
        }
    }
}
