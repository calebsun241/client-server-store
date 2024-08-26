import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
/**
 * Project 5 -- Customer
 *
 * Contains and runs the functionality of the Customer Object. The main method directly points to the conductBusiness
 * method in Customer if the User is a customer, at which point Customer takes over and runs the program. The Customer
 * class allows the Customer to View Marketplace, View Shopping Cart, see Transaction history, access User menu,
 * Export a File with Purchase History, and View Dashboard.
 *
 * @authors Akash Chenthil, lab sec L15
 *          Kipling Liu, lab sec L15
 *          Leo Navarro, lab sec L15
 *          Yixiao Sun, lab sec L15
 *
 * @version April 29, 2023
 */
public class Customer extends User {
    private ArrayList<Transaction> shoppingCart;
    private ArrayList<Transaction> transactionHistory;

    public Customer(String email, String password) {
        super(email, password);
        shoppingCart = new ArrayList<>();
        transactionHistory = new ArrayList<>();
    }

    public ArrayList<Transaction> getShoppingCart() {
        return shoppingCart;
    }

    public ArrayList<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public void refreshOption() {
        // After every command, the client will read in all new data
        // The object must also update itself with the new data
        super.refreshOption();
        this.shoppingCart = Client.searchCustomersByEmail(this.getEmail()).getShoppingCart();
        this.transactionHistory = Client.searchCustomersByEmail(this.getEmail()).getTransactionHistory();
    }

    public void checkoutAllItems() {
        Client.sendCommand("checkoutAllItems", new String[0]);
        Client.errorOnResponse();
        refreshOption();
    }

    public void checkoutAllItemsServer() {
        String badProducts = "";
        for (Transaction t : shoppingCart) {
            try {
            Seller seller = server.searchSellersByEmail(t.getSellerEmail());
            Store store = seller.searchStoresByName(t.getStoreName());
            Product product = store.searchProductsByName(t.getProductName());

            // We know we have a valid product at this point
            server.transactions.add(t);
            } catch (Exception e) {
                badProducts += ", " + t.getProductName();
                // This product no longer exists; add it to error string
            }
        }
        if (badProducts.length() != 0) {
            // Send error message
            server.sendError(String.format("Product(s) %s are no longer available!", badProducts.substring(2)));
        } else {
            server.sendSuccess();
        }
        shoppingCart.clear();
    }

    public int deleteAccount() {
        int reply;
        try {
            reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete your account?",
                    "Delete Account", JOptionPane.YES_NO_OPTION);
            Client.sendCommand("serverDeleteAccount", new String[]{ this.getEmail() });
        } catch (NullPointerException npe) {
            return 0;
        }
        if (reply == 0) {
            return 1;
        }
        return 0;
    }

    public void serverDeleteAccount(String args[]) {
        String email = args[0];
        Customer c = null;
        for (int i = 0; i < Server.customers.size(); i++) {
            if (server.customers.get(i).getEmail().equals(getEmail())) {
                c = server.customers.get(i);
            }
        }
        server.customers.remove(c);
    }

    public void addToShoppingCart(Transaction i) {
        refreshOption();
        Client.sendCommand("addToShoppingCart", new String[] { i.getProductName(), i.getStoreName(),
                i.getSellerEmail(), i.getPrice() + "", i.getQuantityBought() + "", i.getCustomerEmail() });
        refreshOption();
    }

    public synchronized void addToShoppingCartServer(String[] args) {
        String productName = args[0];
        String storeName = args[1];
        String sellerEmail = args[2];
        double price = Double.parseDouble(args[3]);
        int quantity = Integer.parseInt(args[4]);
        String customerEmail = args[5];

        shoppingCart.add(new Transaction(productName, storeName, sellerEmail, price, quantity, customerEmail));
    }



    public void addToTransactionHistory(Transaction i) {
        refreshOption();
        Client.sendCommand("addToTransactionHistory", new String[] { i.getProductName(), i.getStoreName(),
                i.getSellerEmail(), i.getPrice() + "", i.getQuantityBought() + "", i.getCustomerEmail() });
        refreshOption();
    }

    public synchronized void addToTransactionHistoryServer(String[] args) {
        String productName = args[0];
        String storeName = args[1];
        String sellerEmail = args[2];
        double price = Double.parseDouble(args[3]);
        int quantity = Integer.parseInt(args[4]);
        String customerEmail = args[5];

        transactionHistory.add(new Transaction(productName, storeName, sellerEmail, price, quantity, customerEmail));
    }

    public void removeItemFromCart (int input) {
        refreshOption();
        Client.sendCommand("removeItemFromCart", new String[] {input + ""});
        refreshOption();
    }

    public synchronized void removeItemFromCartServer (String args[]) {
        int input = Integer.parseInt(args[0]);
        for (int j = 0; j < server.sellers.size(); j++) {
            if (server.sellers.get(j).getEmail().equals(shoppingCart.get(input).getSellerEmail())) {
                for (int k = 0; k < server.sellers.get(j).getStoreNames().size(); k++) {
                    if (server.sellers.get(j).getStoreNames().get(k).equals(shoppingCart.get(input)
                            .getStoreName())) {
                        for (int l = 0; l < server.sellers.get(j).getStores().get(k).getProductsSold()
                                .size(); l++) {
                            if (server.sellers.get(j).getStores().get(k).getProductsSold().get(l).getName()
                                    .equals(shoppingCart.get(input).getProductName())) {
                                server.sellers.get(j).getStores().get(k).getProductsSold().get(l)
                                        .setQuantity(server.sellers.get(j).getStores().get(k)
                                                .getProductsSold().get(l).getQuantity() +
                                                shoppingCart.get(input).getQuantityBought());
                            }
                        }
                    }
                }
            }
        }
        shoppingCart.remove(input);
    }

    public void removeItemFromSeller(Seller seller, Store store, Product product, String option) {
        refreshOption();
        Client.sendCommand("removeItemFromSeller", new String[] {seller.getEmail(), store.getName(),
                product.getName(), option});
        refreshOption();
    }

    public synchronized void removeItemFromSellerServer(String[] args) {
        Seller seller = MarketPlace.searchSellersByEmail(server.sellers, args[0]);
        Store store = seller.searchStoresByName(args[1]);
        Product product = store.searchProductsByName(args[2]);
        String option = args[3];
        for (int i = 0; i < server.sellers.size(); i++) {
            if (seller.getEmail().equals(server.sellers.get(i).getEmail())) {
                server.sellers.get(i).searchStoresByName(store.getName())
                        .searchProductsByName(product.getName()).setQuantity(server.sellers
                                .get(i).searchStoresByName(store.getName())
                                .searchProductsByName(product.getName()).getQuantity()
                                - Integer.parseInt(option));
                break;
            }
        }
    }

    public String toFormat() {
        ArrayList<String> tmp = new ArrayList<>();
        for (Transaction i : shoppingCart) {
            tmp.add(i.toFormat());
        }
        String build = super.toFormat();
        if (tmp.size() > 0) {
            build += "\n" + String.join(";", tmp);
        }
        return build;
    }

    public void viewCart() {
        StringBuilder shownStuff;
        int cartSize;
        label:
        while (true) {
            refreshOption();
            shownStuff = new StringBuilder();
            cartSize = shoppingCart.size();
            if (cartSize == 0) {
                try {
                    JOptionPane.showMessageDialog(null, "You have no items in your cart!", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                } catch (NullPointerException npe) {
                    return;
                }
                return;
            } else {
                for (Transaction t : shoppingCart) {
                    shownStuff.append(String.format("Seller: %s\n\tStore: %s\n\t\tProduct: %s\n\t\t\tPrice: %.2f \n\t\t\t" +
                                    "Quantity: %d\n", t.getSellerEmail(), t.getStoreName(), t.getProductName(), t.getPrice(),
                            t.getQuantityBought()));
                }
            }
            String[] selections = { "Remove Item", "Checkout", "Exit" };
            String result;
            try {
                result = (String) JOptionPane.showInputDialog(null,
                        shownStuff,
                        "MarketPlace",
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        selections,
                        "Checkout");
                if (result == null) {
                    result = "Quit";
                }
            } catch (NullPointerException npe) {
                result = "Quit";
            }

            int input;
            shownStuff = new StringBuilder();
            switch (result) {
                case "Remove Item":
                    shownStuff.append("Which item would you like to remove?\n(Enter the corresponding number)\n");
                    for (int i = 0; i < shoppingCart.size(); i++) {
                        shownStuff.append(i + ". Seller: " + shoppingCart.get(i).getSellerEmail()
                                + "\n   Store: " + shoppingCart.get(i).getStoreName()
                                + "\n      Product: " + shoppingCart.get(i).getProductName()
                                + "\n         Price: $" + String.format("%.2f", shoppingCart.get(i).getPrice())
                                + "\n         Quantity: " + shoppingCart.get(i).getQuantityBought() + "\n\n");
                    }
                    while (true) {
                        try {
                            input = Integer.parseInt(JOptionPane.showInputDialog(null, shownStuff,
                                    "Remove Item", JOptionPane.QUESTION_MESSAGE));
                            if (input >= cartSize || input < 0) {
                                JOptionPane.showMessageDialog(null, "Enter a valid option!", "ERROR",
                                        JOptionPane.ERROR_MESSAGE);
                            } else {
                                break;
                            }
                        } catch (Exception nfe) {
                            JOptionPane.showMessageDialog(null, "Enter a valid option!", "ERROR",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    removeItemFromCart(input);
                    break;
                case "Checkout":
                    checkoutAllItems();
                    break label;
                case "Exit":
                    break label;
            }
        }
    }

    public void history() {
        refreshOption();
        StringBuilder shownStuff = new StringBuilder();
        if (transactionHistory.size() == 0) {
            JOptionPane.showMessageDialog(null, "You have no transactions!", "ERROR",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (int i = 0; i < transactionHistory.size(); i++) {
            shownStuff.append(i + ". Seller: " + transactionHistory.get(i).getSellerEmail()
                    + "\n   Store: " + transactionHistory.get(i).getStoreName()
                    + "\n      Product: " + transactionHistory.get(i).getProductName()
                    + "\n         Price: $" + String.format("%.2f", transactionHistory.get(i).getPrice())
                    + "\n         Quantity: " + transactionHistory.get(i).getQuantityBought() + "\n");
        }
        JOptionPane.showMessageDialog(null, shownStuff,
                "Transaction History", JOptionPane.QUESTION_MESSAGE);
    }

    public void viewMarketplace() {
        String option;
        StringBuilder shownStuff;
        label:
        while (true) {
            refreshOption();
            shownStuff = new StringBuilder();
            ArrayList<Seller> shownSellers = new ArrayList<>();
            ArrayList<Store> shownStores = new ArrayList<>();
            ArrayList<Product> shownProducts = new ArrayList<>();
            ArrayList<Seller> initialSellers;
            ArrayList<Store> initialStores;
            ArrayList<Product> initialProducts;
            Seller heldSeller;
            Store heldStore;
            Product heldProduct;
            int counter = 0;
            for (int i = 0; i < Client.sellers.size(); i++) {
                for (int j = 0; j < Client.sellers.get(i).getStores().size(); j++) {
                    for (int k = 0; k < Client.sellers.get(i).getStores().get(j).getProductsSold().size(); k++) {
                        shownStuff.append(counter + ". Seller: " + Client.sellers.get(i).getEmail()
                                + "\n   Store: " + Client.sellers.get(i).getStoreNames().get(j)
                                + "\n      Product: " + Client.sellers.get(i).getStores().get(j).getProductsSold()
                                .get(k).getName()
                                + "\n         Price: $" + String.format("%.2f", Client.sellers.get(i).getStores().get(j)
                                .getProductsSold().get(k).getPrice())
                                + "\n         Quantity: " + Client.sellers.get(i).getStores().get(j)
                                .getProductsSold().get(k).getQuantity() + "\n");
                        shownSellers.add(Client.sellers.get(i));
                        shownStores.add(Client.sellers.get(i).getStores().get(j));
                        shownProducts.add(Client.sellers.get(i).getStores().get(j).getProductsSold().get(k));
                        counter++;
                    }
                }
            }
            try {
                option = (String) JOptionPane.showInputDialog(null,
                        shownStuff,
                        "Marketplace",
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new String[] {"Search", "Sort", "Choose Product", "Exit"},
                        "Exit");
                if (option == null) {
                    return;
                }
            } catch (NullPointerException npe) {
                return;
            }
            switch (option) {
                case "Search":
                    counter = 0;
                    shownSellers = new ArrayList<>();
                    shownProducts = new ArrayList<>();
                    shownStores = new ArrayList<>();
                    boolean exists = false;
                    String search;
                    try {
                        search = JOptionPane.showInputDialog(null, "Enter your search term:",
                                "Search", JOptionPane.QUESTION_MESSAGE);
                    } catch (NullPointerException npe) {
                        search = null;
                    }
                    shownStuff = new StringBuilder();
                    for (Seller seller : Client.sellers) {
                        for (Store store : seller.getStores()) {
                            for (Product product : store.getProductsSold()) {
                                if (product.getName().toLowerCase().contains(search.toLowerCase()) ||
                                        product.getDescription().toLowerCase().contains(search.toLowerCase()) ||
                                        store.getName().toLowerCase().contains(search.toLowerCase())) {
                                    shownStuff.append(counter + ". Seller: " + seller.getEmail()
                                            + "\n   Store: " + store.getName()
                                            + "\n      Product: " + product.getName()
                                            + "\n         Price: $" + String.format("%.2f", product.getPrice())
                                            + "\n         Quantity: " + product.getQuantity() + "\n");
                                    shownSellers.add(seller);
                                    shownStores.add(store);
                                    shownProducts.add(product);
                                    counter++;
                                    exists = true;
                                }
                            }
                        }
                    }
                    if (!exists && search != null) {
                        JOptionPane.showMessageDialog(null, "No items match your search string!",
                                "ERROR", JOptionPane.ERROR_MESSAGE);
                    } else if (search != null) {
                        option = (String) JOptionPane.showInputDialog(null,
                                shownStuff,
                                "Marketplace",
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                new String[]{"Choose Product", "Exit"},
                                "Exit");
                        if (option.equals("Choose Product")) {
                            while (true) {
                                try {
                                    shownStuff.insert(0, "Which product would you like to learn more about?\n(Enter the corresponding number)\n");
                                    option = JOptionPane.showInputDialog(null, shownStuff,
                                            "Marketplace", JOptionPane.QUESTION_MESSAGE);
                                    Integer.parseInt(option);
                                    shownStuff = new StringBuilder();
                                    shownStuff.append("Would you like to add this product to your cart?\n\n" +
                                            "Seller: " + shownSellers.get(Integer.parseInt(option)).getEmail()
                                            + "\n   Store: " + shownStores.get(Integer.parseInt(option)).getName()
                                            + "\n      Product: " + shownProducts.get(Integer.parseInt(option)).getName()
                                            + "\n         Description: " + shownProducts.get(Integer.parseInt(option)).getDescription()
                                            + "\n         Price: $" + String.format("%.2f", shownProducts.get(Integer.parseInt(option)).getPrice())
                                            + "\n         Quantity: " + shownProducts.get(Integer.parseInt(option)).getQuantity() + "\n");
                                    break;
                                } catch (Exception nfe) {
                                    JOptionPane.showMessageDialog(null, "Enter a valid option!", "ERROR",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            }
                            int reply = JOptionPane.showConfirmDialog(null, shownStuff,
                                    "Add Item", JOptionPane.YES_NO_OPTION);
                            Seller seller = shownSellers.get(Integer.parseInt(option));
                            seller = (Seller) Client.searchUsersByEmail(seller.getEmail());
                            Store store = shownStores.get(Integer.parseInt(option));
                            Product product = shownProducts.get(Integer.parseInt(option));
                            if (reply == 0) {
                                shownStuff.delete(0, 48);
                                shownStuff.insert(0, "How many would you like to purchase?\n\n");
                                while (true) {
                                    try {
                                        option = JOptionPane.showInputDialog(null, shownStuff,
                                                "Marketplace", JOptionPane.QUESTION_MESSAGE);
                                        int amt = Integer.parseInt(option);
                                        if (amt <= seller.searchStoresByName(store.getName())
                                                .searchProductsByName(product.getName()).getQuantity() &&
                                                amt > 0) {
                                            addToShoppingCart(new Transaction(product.getName(), store.getName(), seller.getEmail(),
                                                    product.getPrice(), Integer.parseInt(option), getEmail()));
                                            removeItemFromSeller(seller, store, product, option);
                                            break;
                                        } else {
                                            JOptionPane.showMessageDialog(null, "Please enter a valid amount!", "ERROR",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                    } catch (Exception nfe) {
                                        JOptionPane.showMessageDialog(null, "Enter a valid option!", "ERROR",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            }
                        } else if (option.equals("Exit")) {
                            break label;
                        }
                    }
                    break;
                case "Sort":
                    while (true) {
                        counter = 0;
                        initialSellers = shownSellers;
                        initialStores = shownStores;
                        initialProducts = shownProducts;
                        shownSellers = new ArrayList<>();
                        shownProducts = new ArrayList<>();
                        shownStores = new ArrayList<>();
                        option = (String) JOptionPane.showInputDialog(null,
                                "How would you like to sort the products?",
                                "Marketplace",
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                new String[]{"By Price", "By Quantity Available"},
                                "By Price");
                        if (option == null) {
                            break;
                        }
                        if (option.equals("By Price")) {
                            while (initialProducts.size() > 0) {
                                heldSeller = initialSellers.get(0);
                                heldProduct = initialProducts.get(0);
                                heldStore = initialStores.get(0);
                                for (int i = 0; i < initialProducts.size(); i++) {
                                    if (initialProducts.get(i).getPrice() < heldProduct.getPrice()) {
                                        heldSeller = initialSellers.get(i);
                                        heldStore = initialStores.get(i);
                                        heldProduct = initialProducts.get(i);
                                    }
                                }
                                for (int i = 0; i < initialProducts.size(); i++) {
                                    if (heldSeller.getEmail().equals(initialSellers.get(i).getEmail()) &&
                                            heldStore.equals(initialStores.get(i)) &&
                                            heldProduct.equals(initialProducts.get(i))) {
                                        initialSellers.remove(i);
                                        initialProducts.remove(i);
                                        initialStores.remove(i);
                                        break;
                                    }
                                }
                                shownStores.add(heldStore);
                                shownSellers.add(heldSeller);
                                shownProducts.add(heldProduct);
                            }
                        } else if (option.equals("By Quantity Available")) {
                            while (initialProducts.size() > 0) {
                                heldSeller = initialSellers.get(0);
                                heldProduct = initialProducts.get(0);
                                heldStore = initialStores.get(0);
                                for (int i = 0; i < initialProducts.size(); i++) {
                                    if (initialProducts.get(i).getQuantity() < heldProduct.getQuantity()) {
                                        heldSeller = initialSellers.get(i);
                                        heldStore = initialStores.get(i);
                                        heldProduct = initialProducts.get(i);
                                    }
                                }
                                for (int i = 0; i < initialProducts.size(); i++) {
                                    if (heldSeller.getEmail().equals(initialSellers.get(i).getEmail()) &&
                                            heldStore.equals(initialStores.get(i)) &&
                                            heldProduct.equals(initialProducts.get(i))) {
                                        initialSellers.remove(i);
                                        initialProducts.remove(i);
                                        initialStores.remove(i);
                                        break;
                                    }
                                }
                                shownStores.add(heldStore);
                                shownSellers.add(heldSeller);
                                shownProducts.add(heldProduct);
                            }
                        }
                        shownStuff = new StringBuilder();
                        for (int i = 0; i < shownStores.size(); i++) {
                            shownStuff.append(counter + ". Seller: " + shownSellers.get(i).getEmail()
                                    + "\n   Store: " + shownStores.get(i).getName()
                                    + "\n      Product: " + shownProducts.get(i).getName()
                                    + "\n         Description: " + shownProducts.get(i).getDescription()
                                    + "\n         Price: $" + String.format("%.2f", shownProducts.get(i).getPrice())
                                    + "\n         Quantity: " + shownProducts.get(i).getQuantity() + "\n");
                            counter++;
                        }
                        option = (String) JOptionPane.showInputDialog(null,
                                shownStuff,
                                "Marketplace",
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                new String[]{"Choose Product", "Exit"},
                                "Exit");
                        if (option.equals("Choose Product")) {
                            while (true) {
                                try {
                                    shownStuff.insert(0, "Which product would you like to learn more about?\n(Enter the corresponding number)\n");
                                    option = JOptionPane.showInputDialog(null, shownStuff,
                                            "Marketplace", JOptionPane.QUESTION_MESSAGE);
                                    int checker = Integer.parseInt(option);
                                    shownStuff = new StringBuilder();
                                    shownStuff.append("Would you like to add this product to your cart?\n\n" +
                                            "Seller: " + shownSellers.get(Integer.parseInt(option)).getEmail()
                                            + "\n   Store: " + shownStores.get(Integer.parseInt(option)).getName()
                                            + "\n      Product: " + shownProducts.get(Integer.parseInt(option)).getName()
                                            + "\n         Description: " + shownProducts.get(Integer.parseInt(option)).getDescription()
                                            + "\n         Price: $" + String.format("%.2f", shownProducts.get(Integer.parseInt(option)).getPrice())
                                            + "\n         Quantity: " + shownProducts.get(Integer.parseInt(option)).getQuantity() + "\n");
                                    break;
                                } catch (Exception nfe) {
                                    JOptionPane.showMessageDialog(null, "Enter a valid option!", "ERROR",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            }
                            int reply = JOptionPane.showConfirmDialog(null, shownStuff,
                                    "Add Item", JOptionPane.YES_NO_OPTION);
                            Seller seller = shownSellers.get(Integer.parseInt(option));
                            seller = (Seller) Client.searchUsersByEmail(seller.getEmail());
                            Store store = shownStores.get(Integer.parseInt(option));
                            Product product = shownProducts.get(Integer.parseInt(option));
                            if (reply == 0) {
                                shownStuff.delete(0, 48);
                                shownStuff.insert(0, "How many would you like to purchase?\n\n");
                                while (true) {
                                    try {
                                        option = JOptionPane.showInputDialog(null, shownStuff,
                                                "Marketplace", JOptionPane.QUESTION_MESSAGE);
                                        if (Integer.parseInt(option) <= seller.searchStoresByName(store.getName())
                                                .searchProductsByName(product.getName()).getQuantity() &&
                                                Integer.parseInt(option) > 0) {
                                            addToShoppingCart(new Transaction(product.getName(), store.getName(), seller.getEmail(),
                                                    product.getPrice(), Integer.parseInt(option), getEmail()));
                                            removeItemFromSeller(seller, store, product, option);
                                            break;
                                        } else {
                                            JOptionPane.showMessageDialog(null, "Please enter a valid amount!", "ERROR",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                    } catch (Exception nfe) {
                                        JOptionPane.showMessageDialog(null, "Enter a valid option!", "ERROR",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            }
                        } else if (option.equals("Exit")) {
                            break;
                        }
                    }
                    break;
                case "Choose Product":
                    while (true) {
                        try {
                            shownStuff.insert(0, "Which product would you like to learn more about?\n(Enter the corresponding number)\n");
                            option = JOptionPane.showInputDialog(null, shownStuff,
                                    "Marketplace", JOptionPane.QUESTION_MESSAGE);
                            Integer.parseInt(option);
                            shownStuff = new StringBuilder();
                            shownStuff.append("Would you like to add this product to your cart?\n\n" +
                                    "Seller: " + shownSellers.get(Integer.parseInt(option)).getEmail()
                                    + "\n   Store: " + shownStores.get(Integer.parseInt(option)).getName()
                                    + "\n      Product: " + shownProducts.get(Integer.parseInt(option)).getName()
                                    + "\n         Description: " + shownProducts.get(Integer.parseInt(option)).getDescription()
                                    + "\n         Price: $" + String.format("%.2f", shownProducts.get(Integer.parseInt(option)).getPrice())
                                    + "\n         Quantity: " + shownProducts.get(Integer.parseInt(option)).getQuantity() + "\n");
                            break;
                        } catch (Exception nfe) {
                            JOptionPane.showMessageDialog(null, "Enter a valid option!", "ERROR",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    int reply = JOptionPane.showConfirmDialog(null, shownStuff,
                            "Add Item", JOptionPane.YES_NO_OPTION);
                    Seller seller = shownSellers.get(Integer.parseInt(option));
                    seller = (Seller) Client.searchUsersByEmail(seller.getEmail());
                    Store store = shownStores.get(Integer.parseInt(option));
                    Product product = shownProducts.get(Integer.parseInt(option));
                    if (reply == 0) {
                        shownStuff.delete(0, 48);
                        shownStuff.insert(0, "How many would you like to purchase?\n\n");
                        while (true) {
                            try {
                                option = JOptionPane.showInputDialog(null, shownStuff,
                                        "Marketplace", JOptionPane.QUESTION_MESSAGE);
                                if (Integer.parseInt(option) <= seller.searchStoresByName(store.getName())
                                        .searchProductsByName(product.getName()).getQuantity() &&
                                        Integer.parseInt(option) > 0) {
                                    addToShoppingCart(new Transaction(product.getName(), store.getName(), seller.getEmail(),
                                            product.getPrice(), Integer.parseInt(option), getEmail()));
                                    removeItemFromSeller(seller, store, product, option);
                                    break;
                                } else {
                                    JOptionPane.showMessageDialog(null, "Please enter a valid amount!", "ERROR",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (Exception nfe) {
                                JOptionPane.showMessageDialog(null, "Enter a valid option!", "ERROR",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                    break;
                case "Exit":
                    break label;
            }
        }
    }

    public void exportCustomerFile() {
        File f = new File(getEmail().split("@")[0] + "PurchaseHistory.csv");
        try {
            if (f.exists()) {
                int reply = JOptionPane.showConfirmDialog(null,
                        "Would you like to overwrite the previous file with the same name?",
                        "Overwrite File", JOptionPane.YES_NO_OPTION);
                if (reply == 0) {
                    f.delete();
                    f.createNewFile();

                    JOptionPane.showMessageDialog(null, "File Overwritten!", "File Status",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(null, "File Created!", "File Status",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            FileWriter fw = new FileWriter(f);
            String[][] transArr = new String[transactionHistory.size()][5];
            String fullText = "";
            for (int i = 0; i < transactionHistory.size(); i++) {
                transArr[i][0] = transactionHistory.get(i).getSellerEmail();
                transArr[i][1] = transactionHistory.get(i).getStoreName();
                transArr[i][2] = transactionHistory.get(i).getProductName();
                transArr[i][3] = String.format("$%.2f", transactionHistory.get(i).getPrice());
                transArr[i][4] = transactionHistory.get(i).getQuantityBought() + "";
            }
            for (String[] dataPoint : transArr) {
                for (int i = 0; i < dataPoint.length; i++) {
                    fullText = fullText + "\"";
                    fullText = fullText + dataPoint[i].replaceAll("\"", "\"\"");
                    fullText = fullText + "\"";
                    if (i != dataPoint.length - 1) {
                        fullText = fullText + ",";
                    }
                }
                fullText = fullText + "\n";
            }
            fw.write(fullText);
            fw.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong!", "ERROR",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    public void viewDashboard() {
        StringBuilder shownStuff;
        String option;
        while (true) {
            if (Client.transactions.size() == 0) {
                JOptionPane.showMessageDialog(null, "No transactions have occurred yet!", "ERROR",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            ArrayList<String> storeInfo = new ArrayList<>();
            ArrayList<Integer> storeSales = new ArrayList<>();
            option = (String) JOptionPane.showInputDialog(null,
                    "Do you want to see statistics for all stores or just the purchases that you made?",
                    "Dashboard",
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[] {"All Stores", "My Purchases", "Exit"},
                    "Exit");
            if (option == null) {
                option = "Exit";
            }
            switch (option) {
                case "All Stores" -> {
                    shownStuff = new StringBuilder();
                    for (Seller seller : Client.sellers) {
                        for (Store store : seller.getStores()) {
                            storeInfo.add(store.getName() + "," + seller.getEmail());
                        }
                    }
                    for (String s : storeInfo) {
                        storeSales.add(0);
                    }
                    for (Transaction t : Client.transactions) {
                        for (String s : storeInfo) {
                            if (s.split(",")[0].equals(t.getStoreName()) &&
                                    s.split(",")[1].equals(t.getSellerEmail())) {
                                storeSales.set(storeInfo.indexOf(s), storeSales.get(storeInfo.indexOf(s)) +
                                        t.getQuantityBought());
                                break;
                            }
                        }
                    }
                    for (int i = 0; i < storeInfo.size(); i++) {
                        shownStuff.append("Store " + storeInfo.get(i).split(",")[0] + " by Seller " +
                                storeInfo.get(i).split(",")[1] + " has sold " + storeSales.get(i) + " total products.\n");
                    }
                    int reply = JOptionPane.showConfirmDialog(null, "Would you like to sort the stores?\n\n" + shownStuff,
                            "Dashboard", JOptionPane.YES_NO_OPTION);
                    if (reply == 0) {
                        option = (String) JOptionPane.showInputDialog(null,
                                "How would you like to sort them?\n\n" + shownStuff,
                                "Dashboard",
                                JOptionPane.INFORMATION_MESSAGE,
                                null, /* icon */
                                new String[]{"By Products Sold", "By Store Name", "By Seller Email"},
                                "By Products Sold");
                        if (option.equals("By Products Sold")) {
                            String heldStore;
                            int heldSale;
                            int index;
                            ArrayList<String> initialStores = storeInfo;
                            ArrayList<Integer> initialSales = storeSales;
                            ArrayList<String> sortedStores = new ArrayList<>();
                            ArrayList<Integer> sortedSales = new ArrayList<>();
                            while (initialSales.size() > 0) {
                                heldStore = initialStores.get(0);
                                heldSale = initialSales.get(0);
                                index = 0;
                                for (int i = 0; i < initialStores.size(); i++) {
                                    if (heldSale > initialSales.get(i)) {
                                        heldSale = initialSales.get(i);
                                        heldStore = initialStores.get(i);
                                        index = i;
                                    }
                                }
                                sortedStores.add(heldStore);
                                sortedSales.add(heldSale);
                                initialSales.remove(index);
                                initialStores.remove(index);
                            }
                            shownStuff = new StringBuilder();
                            for (int i = 0; i < sortedStores.size(); i++) {
                                System.out.println("Store " + sortedStores.get(i).split(",")[0] + " by Seller " +
                                        sortedStores.get(i).split(",")[1] + " has sold " + sortedSales.get(i) +
                                        " total products.\n");
                                shownStuff.append("Store " + sortedStores.get(i).split(",")[0] + " by Seller " +
                                        sortedStores.get(i).split(",")[1] + " has sold " + sortedSales.get(i) +
                                        " total products.\n");
                            }
                            JOptionPane.showMessageDialog(null, shownStuff, "Dashboard",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else if (option.equals("By Store Name")) {
                            String heldStore;
                            int heldSale;
                            int index;
                            ArrayList<String> initialStores = storeInfo;
                            ArrayList<Integer> initialSales = storeSales;
                            ArrayList<String> sortedStores = new ArrayList<>();
                            ArrayList<Integer> sortedSales = new ArrayList<>();
                            while (initialSales.size() > 0) {
                                heldStore = initialStores.get(0);
                                heldSale = initialSales.get(0);
                                index = 0;
                                for (int i = 0; i < initialStores.size(); i++) {
                                    if (heldStore.compareTo(initialStores.get(i)) > 0) {
                                        heldSale = initialSales.get(i);
                                        heldStore = initialStores.get(i);
                                        index = i;
                                    }
                                }
                                sortedStores.add(heldStore);
                                sortedSales.add(heldSale);
                                initialSales.remove(index);
                                initialStores.remove(index);
                            }
                            shownStuff = new StringBuilder();
                            for (int i = 0; i < sortedStores.size(); i++) {
                                shownStuff.append("Store " + sortedStores.get(i).split(",")[0] + " by Seller " +
                                        sortedStores.get(i).split(",")[1] + " has sold " + sortedSales.get(i) +
                                        " total products.\n");
                            }
                            JOptionPane.showMessageDialog(null, shownStuff, "Dashboard",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            String heldStore;
                            int heldSale;
                            int index;
                            ArrayList<String> initialStores = storeInfo;
                            ArrayList<Integer> initialSales = storeSales;
                            ArrayList<String> sortedStores = new ArrayList<>();
                            ArrayList<Integer> sortedSales = new ArrayList<>();
                            while (initialSales.size() > 0) {
                                heldStore = initialStores.get(0);
                                heldSale = initialSales.get(0);
                                index = 0;
                                for (int i = 0; i < initialStores.size(); i++) {
                                    if (heldStore.split(",")[1].compareTo(initialStores.get(i).split(",")[1]) > 0) {
                                        heldSale = initialSales.get(i);
                                        heldStore = initialStores.get(i);
                                        index = i;
                                    }
                                }
                                sortedStores.add(heldStore);
                                sortedSales.add(heldSale);
                                initialSales.remove(index);
                                initialStores.remove(index);
                            }
                            shownStuff = new StringBuilder();
                            for (int i = 0; i < sortedStores.size(); i++) {
                                shownStuff.append("Store " + sortedStores.get(i).split(",")[0] + " by Seller " +
                                        sortedStores.get(i).split(",")[1] + " has sold " + sortedSales.get(i) +
                                        " total products.\n");
                            }
                            JOptionPane.showMessageDialog(null, shownStuff, "Dashboard",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
                case "My Purchases" -> {
                    for (Seller seller : Client.sellers) {
                        for (Store store : seller.getStores()) {
                            storeInfo.add(store.getName() + "," + seller.getEmail());
                        }
                    }
                    for (String s : storeInfo) {
                        storeSales.add(0);
                    }
                    for (Transaction t : Client.transactions) {
                        for (String s : storeInfo) {
                            if (s.split(",")[0].equals(t.getStoreName()) &&
                                    s.split(",")[1].equals(t.getSellerEmail()) &&
                                    t.getCustomerEmail().equals(getEmail())) {
                                storeSales.set(storeInfo.indexOf(s), storeSales.get(storeInfo.indexOf(s)) +
                                        t.getQuantityBought());
                                break;
                            }
                        }
                    }
                    shownStuff = new StringBuilder();
                    for (int i = 0; i < storeInfo.size(); i++) {
                        shownStuff.append("Store " + storeInfo.get(i).split(",")[0] + " by Seller " +
                                storeInfo.get(i).split(",")[1] + " has sold " + storeSales.get(i) + " total products.\n");
                    }
                    int reply = JOptionPane.showConfirmDialog(null, "Would you like to sort the stores?\n\n"
                                    + shownStuff, "Dashboard", JOptionPane.YES_NO_OPTION);
                    if (reply == 0) {
                        option = (String) JOptionPane.showInputDialog(null,
                                "How would you like to sort them?\n\n" + shownStuff,
                                "Dashboard",
                                JOptionPane.INFORMATION_MESSAGE,
                                null, /* icon */
                                new String[]{"By Products Sold", "By Store Name", "By Seller Email"},
                                "By Products Sold");
                        if (option.equals("By Products Sold")) {
                            String heldStore;
                            int heldSale;
                            int index;
                            ArrayList<String> initialStores = storeInfo;
                            ArrayList<Integer> initialSales = storeSales;
                            ArrayList<String> sortedStores = new ArrayList<>();
                            ArrayList<Integer> sortedSales = new ArrayList<>();
                            while (initialSales.size() > 0) {
                                heldStore = initialStores.get(0);
                                heldSale = initialSales.get(0);
                                index = 0;
                                for (int i = 0; i < initialStores.size(); i++) {
                                    if (heldSale > initialSales.get(i)) {
                                        heldSale = initialSales.get(i);
                                        heldStore = initialStores.get(i);
                                        index = i;
                                    }
                                }
                                sortedStores.add(heldStore);
                                sortedSales.add(heldSale);
                                initialSales.remove(index);
                                initialStores.remove(index);
                            }
                            shownStuff = new StringBuilder();
                            for (int i = 0; i < sortedStores.size(); i++) {
                                shownStuff.append("Store " + sortedStores.get(i).split(",")[0] + " by Seller " +
                                        sortedStores.get(i).split(",")[1] + " has sold " + sortedSales.get(i) +
                                        " total products.\n");
                            }
                            JOptionPane.showMessageDialog(null, shownStuff, "Dashboard",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else if (option.equals("By Store Name")) {
                            String heldStore;
                            int heldSale;
                            int index;
                            ArrayList<String> initialStores = storeInfo;
                            ArrayList<Integer> initialSales = storeSales;
                            ArrayList<String> sortedStores = new ArrayList<>();
                            ArrayList<Integer> sortedSales = new ArrayList<>();
                            while (initialSales.size() > 0) {
                                heldStore = initialStores.get(0);
                                heldSale = initialSales.get(0);
                                index = 0;
                                for (int i = 0; i < initialStores.size(); i++) {
                                    if (heldStore.compareTo(initialStores.get(i)) > 0) {
                                        heldSale = initialSales.get(i);
                                        heldStore = initialStores.get(i);
                                        index = i;
                                    }
                                }
                                sortedStores.add(heldStore);
                                sortedSales.add(heldSale);
                                initialSales.remove(index);
                                initialStores.remove(index);
                            }
                            shownStuff = new StringBuilder();
                            for (int i = 0; i < sortedStores.size(); i++) {
                                shownStuff.append("Store " + sortedStores.get(i).split(",")[0] + " by Seller " +
                                        sortedStores.get(i).split(",")[1] + " has sold " + sortedSales.get(i) +
                                        " total products.\n");
                            }
                            JOptionPane.showMessageDialog(null, shownStuff, "Dashboard",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            String heldStore;
                            int heldSale;
                            int index;
                            ArrayList<String> initialStores = storeInfo;
                            ArrayList<Integer> initialSales = storeSales;
                            ArrayList<String> sortedStores = new ArrayList<>();
                            ArrayList<Integer> sortedSales = new ArrayList<>();
                            while (initialSales.size() > 0) {
                                heldStore = initialStores.get(0);
                                heldSale = initialSales.get(0);
                                index = 0;
                                for (int i = 0; i < initialStores.size(); i++) {
                                    if (heldStore.split(",")[1].compareTo(initialStores.get(i).split(",")[1]) > 0) {
                                        heldSale = initialSales.get(i);
                                        heldStore = initialStores.get(i);
                                        index = i;
                                    }
                                }
                                sortedStores.add(heldStore);
                                sortedSales.add(heldSale);
                                initialSales.remove(index);
                                initialStores.remove(index);
                            }
                            shownStuff = new StringBuilder();
                            for (int i = 0; i < sortedStores.size(); i++) {
                                shownStuff.append("Store " + sortedStores.get(i).split(",")[0] + " by Seller " +
                                        sortedStores.get(i).split(",")[1] + " has sold " + sortedSales.get(i) +
                                        " total products.\n");
                            }
                            JOptionPane.showMessageDialog(null, shownStuff, "Dashboard",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
                case "Exit" -> {
                    return;
                }
            }

        }
    }

    public Customer conductBusiness() {
        String option;
        while (true) {
            option = Client.getOption("", new String[] { "Refresh", "View Marketplace", "View Shopping Cart",
                    "Transaction history", "User menu", "Export a File with Purchase History", "View Dashboard", "Quit" });
            if (option == null) {
                option = "Refresh";
            }
            switch (option) {
                case "Quit" -> {
                    return null;
                }
                case "User menu" -> {
                    label:
                    while (true) {
                        option = Client.getOption("", new String[]{"Change Email", "Change Password", "Delete Account", "Exit"});
                        if (option == null) {
                            option = "";
                        }
                        switch (option) {
                            case "Exit":
                                break label;
                            case "Change Email":
                                changeEmail();
                                break;
                            case "Change Password":
                                changePassword();
                                break;
                            case "Delete Account":
                                if (deleteAccount() == 1) {
                                    return null;
                                }
                                break;
                        }
                    }
                }
                case "Export a File with Purchase History" -> exportCustomerFile();
                case "View Shopping Cart" -> viewCart();
                case "Transaction history" -> history();
                case "View Marketplace" -> viewMarketplace();
                case "View Dashboard" -> viewDashboard();
                case "Refresh" -> refreshOption();
            }
        }
    }
}
