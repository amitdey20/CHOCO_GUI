import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Register, login, reset password 
public class AuthService {
    private static final int NAME = 0, PHONE = 1, USERNAME = 2, PASSWORD = 3;
    private static final String FILE = "users.txt";

    public static boolean isValidPhone(String s) { return s.matches("01[3-9][0-9]{8}"); }

    // What missing from a password
    public static List<String> passwordProblems(String p) {
        List<String> list = new ArrayList<>();
        if (p.length() < 6) list.add("at least 6 characters");
        if (!p.matches(".*[a-z].*")) list.add("a lowercase letter");
        if (!p.matches(".*[A-Z].*")) list.add("an uppercase letter");
        if (!p.matches(".*[0-9].*")) list.add("a number");
        if (!p.matches(".*[^A-Za-z0-9\\s].*")) list.add("a special character");
        if (p.matches(".*\\s.*")) list.add("no spaces");
        return list;
    }

    public String usernameError(String u) {
        if (!u.matches("[A-Za-z0-9_]{3,20}")) return "Username must be 3-20 characters (letters, numbers, underscore).";
        if (u.matches("[0-9]{11}")) return "Username cannot look like a phone number.";
        if (findRecord(USERNAME, u) != null) return "That username is already taken. Try another one.";
        return null;
    }

    public boolean hasUsers() { return !readLines().isEmpty(); }

    public boolean phoneTaken(String phone) { return findRecord(PHONE, phone) != null; }

    public User register(String name, String phone, String username, String password) {
        name = name.replace("\t", " ");
        List<String> lines = readLines();
        lines.add(name + "\t" + phone + "\t" + username + "\t" + password);
        saveLines(lines);
        return new User(name, phone, username);
    }

    // Returns the user, or null when the details are wrong.
    public User login(String id, String password) {
        String[] rec = findRecord(USERNAME, id);
        if (rec == null) rec = findRecord(PHONE, id);
        if (rec != null && rec[PASSWORD].equals(password)) return new User(rec[NAME], rec[PHONE], rec[USERNAME]);
        return null;
    }

    public String usernameOf(String phone) {
        String[] rec = findRecord(PHONE, phone);
        return rec == null ? null : rec[USERNAME];
    }

    public void resetPassword(String phone, String newPassword) {
        List<String> lines = readLines();
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\t", -1);
            if (parts.length == 4 && parts[PHONE].equals(phone)) {
                parts[PASSWORD] = newPassword;
                lines.set(i, String.join("\t", parts));
                break;
            }
        }
        saveLines(lines);
    }

    private String[] findRecord(int field, String value) {
        for (String line : readLines()) {
            String[] parts = line.split("\t", -1);
            if (parts.length == 4 && parts[field].equalsIgnoreCase(value)) return parts;
        }
        return null;
    }

    private List<String> readLines() {
        List<String> lines = new ArrayList<>();
        try {
            if (Files.exists(Paths.get(FILE))) lines.addAll(Files.readAllLines(Paths.get(FILE)));
        } catch (IOException e) {
            System.out.println("Could not read users file.");
        }
        return lines;
    }

    private void saveLines(List<String> lines) {
        try {
            Files.write(Paths.get(FILE), lines);
        } catch (IOException e) {
            System.out.println("Could not save users file.");
        }
    }
}
