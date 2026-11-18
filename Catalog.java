import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Product list (saved in products.txt)
public class Catalog {
    private static final String FILE = "products.txt";
    private ArrayList<Product> products = new ArrayList<>();

    public Catalog() {
        load();
        // first time : add some default chocolates
        if (!Files.exists(Paths.get(FILE))) {
            String[] names = {"Dairy Milk", "KitKat", "Snickers", "Ferrero Rocher", "Toblerone",
                              "Mars", "Bounty", "Twix", "Cadbury Bournville", "Milky Way"};
            int[] prices = {100, 80, 120, 350, 250, 110, 130, 90, 200, 70};
            for (int i = 0; i < names.length; i++) {
                products.add(new Product(names[i], prices[i], 0));
            }
            save();
        }
    }

    public int size() {
        return products.size();
    }

    public Product get(int index) {
        return products.get(index);
    }

    public void add(String name, int price) {
        products.add(new Product(name.replace("\t", " "), price, 0));
        save();
    }

    public void remove(int index) {
        products.remove(index);
        save();
    }

    public void setDiscount(int index, int percent) {
        products.get(index).setDiscountPercent(percent);
        save();
    }

    private void load() {
        if (!Files.exists(Paths.get(FILE))) {
            return;
        }
        try {
            for (String line : Files.readAllLines(Paths.get(FILE))) {
                String[] parts = line.split("\t");
                if (parts.length == 3) {
                    products.add(new Product(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read products file.");
        } catch (NumberFormatException e) {
            System.out.println("Products file has a wrong number.");
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (Product p : products) {
            lines.add(p.getName() + "\t" + p.getPrice() + "\t" + p.getDiscountPercent());
        }
        try {
            Files.write(Paths.get(FILE), lines);
        } catch (IOException e) {
            System.out.println("Could not save products file.");
        }
    }
}
