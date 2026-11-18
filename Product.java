public class Product {
    private String name;
    private int price;
    private int discountPercent;

    public Product(String name, int price, int discountPercent) {
        this.name = name;
        this.price = price;
        this.discountPercent = discountPercent;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    // Price after the product discount
    public int getFinalPrice() {
        return price - (price * discountPercent / 100);
    }
}
