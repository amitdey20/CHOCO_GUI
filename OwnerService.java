import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Shop owner account (owner.txt).
public class OwnerService {
    private static final String FILE = "owner.txt";

    public boolean exists() { return Files.exists(Paths.get(FILE)); }

    public String[] read() {
        try {
            String[] parts = Files.readAllLines(Paths.get(FILE)).get(0).split("\t", -1);
            if (parts.length == 2) return parts;
        } catch (IOException | IndexOutOfBoundsException e) {
            // fall through
        }
        return null;
    }

    public String name() {
        String[] o = read();
        return o == null ? "Owner" : o[0];
    }

    public boolean verify(String password) {
        String[] o = read();
        return o != null && o[1].equals(password);
    }

    public void save(String name, String password) {
        List<String> lines = new ArrayList<>();
        lines.add(name.replace("\t", " ") + "\t" + password);
        try {
            Files.write(Paths.get(FILE), lines);
        } catch (IOException e) {
            System.out.println("Could not save owner file.");
        }
    }
}
