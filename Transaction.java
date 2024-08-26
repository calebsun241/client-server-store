/**
 * Project 5 -- Transaction
 *
 * The transaction class keeps track of any purchase that is made by a customer, along with the product,
 * the quantity, the price, the store of the product, and the seller of the product. This information is used for
 * transaction history.
 *
 * @authors Akash Chenthil, lab sec L15
 *          Kipling Liu, lab sec L15
 *          Leo Navarro, lab sec L15
 *          Yixiao Sun, lab sec L15
 *
 * @version April 29, 2023
 */
public class Transaction {
    private final String productName;
    private final String sellerEmail;
    private final String storeName;
    private final double price;
    private final int quantityBought;
    private final String customerEmail;

    public Transaction(String productName, String storeName, String sellerEmail, double price, int quantityBought, String customerEmail) {
        this.productName = productName;
        this.sellerEmail = sellerEmail;
        this.storeName = storeName;
        this.price = price;
        this.quantityBought = quantityBought;
        this.customerEmail = customerEmail;
    }

    public String getProductName() {
        return productName;
    }
    public String getSellerEmail() {
        return sellerEmail;
    }
    public String getStoreName() {
        return storeName;
    }
    public double getPrice() {
        return price;
    }
    public int getQuantityBought() {
        return quantityBought;
    }
    public String getCustomerEmail() {
        return customerEmail;
    }

    public String toString() {
        return String.format("Seller: %s\n" +
                             "   Store: %s\n" +
                             "      Product: %s\n" +
                             "         Price: %.2f\n" +
                             "         Quantity: %d",
                             sellerEmail,
                             storeName,
                             productName,
                             price,
                             quantityBought);
    
    }
    public String toFormat() {
        return productName + "," + storeName + "," + sellerEmail + "," + price + "," + quantityBought + "," + customerEmail;
    }
}
