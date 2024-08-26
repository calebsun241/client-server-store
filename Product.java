
import java.util.ArrayList;
/**
 * Project 5 -- Product
 *
 * The Product class keeps track of the product objects and all of their functionalities.
 *
 * @authors Akash Chenthil, lab sec L15
 *          Kipling Liu, lab sec L15
 *          Leo Navarro, lab sec L15
 *          Yixiao Sun, lab sec L15
 *
 * @version April 29, 2023
 */
public class Product {
    private String name;
    private String description;
    private int quantity;
    private double price;

    public Product(String name, String description, int quantity, double price) {
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public int getQuantity() {
        return quantity;
    }
    public double getPrice() {
        return price;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public String toFormat() {
        return name + "," + description + "," + quantity + "," + price;
    }
    public String toString(String sellerEmail, String storeName) {
        return String.format("Seller: %s\n" +
                             "   Store: %s\n" +
                             "      Product: %s\n" +
                             "         Price: %.2f\n" +
                             "         Quantity: %d",
                             sellerEmail,
                             storeName,
                             name,
                             price,
                             quantity);
    }

}
