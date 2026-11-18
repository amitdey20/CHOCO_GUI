public class User {
    private String name;
    private String phone;
    private String username;

    public User(String name, String phone, String username) {
        this.name = name;
        this.phone = phone;
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getUsername() {
        return username;
    }
}
