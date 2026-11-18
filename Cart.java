import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cart {
    private final LinkedHashMap<Product, Integer> items = new LinkedHashMap<>();

    public void add(Product p) { items.merge(p, 1, Integer::sum); }

    public void remove(Product p) {
        Integer q = items.get(p);
        if (q == null) return;
        if (q <= 1) items.remove(p); else items.put(p, q - 1);
    }

    public Map<Product, Integer> items() { return Collections.unmodifiableMap(items); }

    public int count() {
        int n = 0;
        for (int q : items.values()) n += q;
        return n;
    }

    // after product discounts, before membership discount
    public int subtotal() {
        int sum = 0;
        for (Map.Entry<Product, Integer> e : items.entrySet()) sum += e.getValue() * e.getKey().getFinalPrice();
        return sum;
    }

    public int memberDiscount(boolean member, int percent) { return member ? subtotal() * percent / 100 : 0; }

    public int total(boolean member, int percent) { return subtotal() - memberDiscount(member, percent); }
}
