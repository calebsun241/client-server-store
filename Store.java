import java.util.ArrayList;
/**
 * Project 5 -- Store
 *
 * The store class keeps track of the store objects and all the functionalities, along with keeping
 * track of each product of the store.
 *
 * @authors Akash Chenthil, lab sec L15
 *          Kipling Liu, lab sec L15
 *          Leo Navarro, lab sec L15
 *          Yixiao Sun, lab sec L15
 *
 * @version April 29, 2023
 */
public class Store {
    private String name;
    private ArrayList<Product> productsSold;

    public Store(String name) {
        this.name = name;
        productsSold = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public ArrayList<Product> getProductsSold() {
        return productsSold;
    }
    public Product searchProductsByName(String name) {
        for (Product p : productsSold) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void addProduct(Product p) {
        productsSold.add(p);
    }
    public void removeProduct(Product p) {
        productsSold.remove(p);
    }

    public String toFormat() {
        ArrayList<String> tmp = new ArrayList<>();
        for (Product p : productsSold) {
            tmp.add(p.toFormat());
        }
        return name + ";" + String.join(";", tmp);
    }
}
