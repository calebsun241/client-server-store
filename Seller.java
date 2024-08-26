// When implementing deletion of products, just delete the product from the stores ArrayList.
// A possible problem that can occur is that an entry in the transaction history will
// reference a product that has since been removed. To remedy this, any time a transaction is
// created, we should copy over all information that the transaction needs to contain.
// i.e., the Transaction class will have instance variables that are essentially duplicates of
// the instance variables in the Product class.

import javax.swing.*;
import java.util.*;
import java.io.*;
/**
 * Project 5 -- Seller
 *
 * The seller class keeps track of the seller objects and all of their functionalities, along with keeping
 * track of all stores and products belonging to the seller.The Seller class allows the seller to Add store, Edit store,
 * Delete store, Add product, View sales by store, Import product file, View customer carts, View dashboard, and access
 * User menu
 *
 * @authors Akash Chenthil, lab sec L15
 *          Kipling Liu, lab sec L15
 *          Leo Navarro, lab sec L15
 *          Yixiao Sun, lab sec L15
 *
 * @version April 29, 2023
 */
public class Seller extends User {
    private ArrayList<Store> stores;

    public Seller(String username, String password) {
        super(username, password);
        stores = new ArrayList<>();
    }

    public ArrayList<Store> getStores() {
        return stores;
    }
    public ArrayList<String> getStoreNames() {
        ArrayList<String> tmp = new ArrayList<>();
        for (Store store : stores) {
            tmp.add(store.getName());
        }
        return tmp;
    }
    public ArrayList<Product> getProductsSold() {
        ArrayList<Product> tmp = new ArrayList<>();
        for (Store s : stores) {
            tmp.addAll(s.getProductsSold());
        }
        return tmp;
    }
    public Store searchStoresByName(String name) {
        for (Store s : stores) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }
    public Product searchProductsByName(String name) {
        for (Store s : stores) {
            Product tmp = s.searchProductsByName(name);
            if (tmp != null) {
                return tmp;
            }
        }
        return null;
    }

    public void addStore(Store store) {
        stores.add(store);
    }

    public void removeStore(Store store) {
        stores.remove(store);
    }

    public String toFormat() {
        ArrayList<String> tmp = new ArrayList<>();
        for (Store s : stores) {
            tmp.add(s.toFormat());
        }
        String build = super.toFormat();
        if (tmp.size() > 0) {
            build += "\n" + String.join("\n", tmp);
        }
        return build;
    }

    // Helper function which allows user to choose a store
    public Store chooseStore() {
        return chooseStore("Which store?");
    }
    public Store chooseStore(String s) {
        ArrayList<String> storeNames = getStoreNames();
        String[] tmp = new String[storeNames.size()];
        tmp = storeNames.toArray(tmp);
        String storeChoice = Client.getOption(s, tmp);
        return searchStoresByName(storeChoice);
    }

    public Product chooseProduct(Store store) {
        // Get a list of products sold by the seller
        ArrayList<Product> products = store.getProductsSold();

        // Convert the list of products into an array
        String[] tmp = new String[products.size()];
        int i = 0;
        for (Product p : products) {
            tmp[i] = p.getName();
            i++;
        }
        int productChoice = Client.getOptionNo(String.format("Which product from %s?", store.getName()), tmp);
        return products.get(productChoice);
    }

    public void errPrint(String s) {
        errPrint(s, JOptionPane.ERROR_MESSAGE);
    }
    public void errPrint(String s, int type) {
        JOptionPane.showMessageDialog(null, s, "", type);
    }

    public static String prompt(String s) {
        return Client.prompt(s);
    }

    public void refreshOption() {
        // After every command, the client will read in all new data
        // The object must also update itself with the new data
        super.refreshOption();
        this.stores = ((Seller) Client.searchUsersByEmail(this.getEmail())).getStores();
    }

    // Checks if a store with the same name is already in stores
    public void addStoreOption() {
        refreshOption();

        System.out.println("Adding the store");
        String name = prompt("Name of store: ");
        Client.sendCommand("addStoreOption", new String[] { name });
        if (Client.errorOnResponse()) {
        }
    }
    public synchronized void addStoreOptionServer(String[] args) {
        String name = args[0];
        if (searchStoresByName(name) != null) {
            server.sendError(String.format("Store with name %s already exists!", name));
        } else {
            server.sendSuccess();
            addStore(new Store(name));
        }
    }

    // Checks if there are any stores
    // Checks if there is a name conflict
    public void editStoreOption() {
        refreshOption();

        /* Client checking */
        if (stores.size() == 0) {
            errPrint("No stores, please add one first!");
            return;
        }

        String f = chooseStore().getName();
        String t = prompt("New name of store: ");

        Client.sendCommand("editStoreOption", new String[] { f, t });
        
        if (Client.errorOnResponse()) {
        }
    }
    public synchronized void editStoreOptionServer(String[] args) {
        String f = args[0];
        String t = args[1];

        if (stores.size() == 0) {
            server.sendError("No stores, please add one first!");
        } else {
            Store store;
            if ((store = searchStoresByName(f)) == null) {
                server.sendError(String.format("Store with name %s no longer exists!", f));
            } else if (searchStoresByName(t) != null) {
                server.sendError(String.format("Store with name %s already exists!", t));
            } else {
                server.sendSuccess();
                store.setName(t);
            }
        }
    }

    // Checks if there are any stores
    public void deleteStoreOption() {
        refreshOption();

        /* Client checking */
        if (stores.size() == 0) {
            errPrint("No stores, please add one first!");
            return;
        }

        Store store = chooseStore();
        Client.sendCommand("deleteStoreOption", new String[] { store.getName() });

        if (Client.errorOnResponse()) {
        }

        removeStore(store);
    }
    public synchronized void deleteStoreOptionServer(String[] args) {
        String name = args[0];

        if (stores.size() == 0) {
            server.sendError("No stores, please add one first!");
        } else {
            Store store;
            if ((store = searchStoresByName(name)) == null) {
                server.sendError(String.format("Store with name %s no longer exists!", name));
            } else {
                server.sendSuccess();
                removeStore(store);
            }
        }
    }

    // Checks if there are any stores to be added to
    // Checks if the product is already in the store it is to be added to
    public void addProductOption() {
        refreshOption();
        
        /* Client checking */
        if (stores.size() == 0) {
            errPrint("No stores, please add one first!");
            return;
        }
        Store store = chooseStore();

        String productName = prompt("Name: ");
        String description = prompt("Description: ");

        /* Client checking */
        int quantity;
        while (true) {
            try {
                quantity = Integer.parseInt(prompt("Quantity: "));
                break;
            } catch (Exception e) {
                errPrint("Please enter an integer for quantity!");
            }
        }
        double price;
        while (true) {
            try {
                price = Double.parseDouble(prompt("Price: "));
                break;
            } catch (Exception e) {
                errPrint("Please enter a floating point number for price!");
            }
        }

        Client.sendCommand("addProductOption", new String[] { store.getName(), productName, description, String.valueOf(quantity), String.valueOf(price) });

        if (Client.errorOnResponse()) {
        }
    }
    public synchronized void addProductOptionServer(String[] args) {
        String storeName = args[0];
        String productName = args[1];
        String description = args[2];
        /* Should be guaranteed not to fail because of client checking */
        int quantity = Integer.parseInt(args[3]);
        double price = Double.parseDouble(args[4]);

        Store store;
        if ((store = searchStoresByName(storeName)) == null) {
            server.sendError(String.format("Store with name %s no longer exists!", storeName));
        } else if (store.searchProductsByName(productName) != null) {
            server.sendError(String.format("Product with name %s already exists!", productName));
        } else {
            server.sendSuccess();
            store.addProduct(new Product(productName, description, quantity, price));
        }
    }

    public void editProductOption() {
        refreshOption();

        /* Client checking */
        if (stores.size() == 0) {
            errPrint("No stores, please add one first!");
            return;
        }

        Store store = chooseStore();
        Product product = chooseProduct(store);

        String newName = prompt("New name of product: ");
        String description = prompt("New description: ");

        int quantity;
        while (true) {
            try {
                quantity = Integer.parseInt(prompt("New quantity: "));
                break;
            } catch (Exception e) {
                errPrint("Please enter an integer for quantity!");
            }
        }
        double price;
        while (true) {
            try {
                price = Double.parseDouble(prompt("New price: "));
                break;
            } catch (Exception e) {
                errPrint("Please enter a floating point number for price!");
            }
        }

        Client.sendCommand("editProductOption", new String[] { store.getName(), product.getName(), newName, description, String.valueOf(quantity), String.valueOf(price) });
        
        if (Client.errorOnResponse()) {
        }
    }
    public synchronized void editProductOptionServer(String[] args) {
        String storeName = args[0];
        String productName = args[1];
        String newName = args[2];
        String description = args[3];
        /* Should be guaranteed not to fail because of client checking */
        int quantity = Integer.parseInt(args[4]);
        double price = Double.parseDouble(args[5]);

        Store store;
        Product product;
        if ((store = searchStoresByName(storeName)) == null) {
            server.sendError(String.format("Store with name %s no longer exists!", storeName));
        } else if ((product = store.searchProductsByName(productName)) == null) {
            server.sendError(String.format("Product with name %s no longer exists!", productName));
        } else if (store.searchProductsByName(newName) != null) {
            server.sendError(String.format("Product with name %s already exits!", newName));
        } else {
            server.sendSuccess();
            product.setName(newName);
            product.setDescription(description);
            product.setQuantity(quantity);
            product.setPrice(price);
        }
    }

    public void deleteProductOption() {
        refreshOption();

        /* Client checking */
        if (stores.size() == 0) {
            errPrint("No stores, please add one first!");
            return;
        }

        Store store = chooseStore();
        Product product = chooseProduct(store);

        Client.sendCommand("deleteProductOption", new String[] { store.getName(), product.getName() });

        if (Client.errorOnResponse()) {
        }
    }
    public synchronized void deleteProductOptionServer(String[] args) {
        String storeName = args[0];
        String productName = args[1];
        
        Store store;
        Product product;
        if ((store = searchStoresByName(storeName)) == null) {
            server.sendError(String.format("Store with name %s no longer exists!", storeName));
        } else if ((product = store.searchProductsByName(productName)) == null) {
            server.sendError(String.format("Product with name %s no longer exists!", productName));
        } else {
            server.sendSuccess();
            store.removeProduct(product);
        }
    }

    // Checks if there are any stores to view
    // Checks if there are any transactions to view for the chosen store
    public void viewSalesByStoreOption() {
        refreshOption();

        if (stores.size() == 0) {
            errPrint("No stores, please add one first!");
        } else {
            Store store = chooseStore();
            String storeChoice = store.getName();

            boolean found = false;
        int count = 0;
            StringBuilder s = new StringBuilder();
            for (Transaction t : Client.transactions) {
                if (t.getSellerEmail().equals(this.getEmail()) && t.getStoreName().equals(storeChoice)) {
                    s.append(count + ". " + t + "\n");
                    found = true;
            count++;
                }
            }
            if (!found) {
                errPrint("No transactions associated with this store!");
            } else {
                errPrint(s.toString(), JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // Checks if there are any stores to add products to
    // Checks if the file exists
    // Checks if the file is correctly formatted
    // Checks if the file is empty
    // Checks if the selected store already contains the product to be added
    public void importFileOption() {
        refreshOption();

        /* Client checking */
        if (stores.size() == 0) {
            errPrint("No stores, please add one first!");
            return;
        }

        try {
            String fileName = prompt("Enter file path: ");
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(fileName));
            } catch (Exception e) {
                errPrint("Something went wrong reading the file!");
                return;
            }

            ArrayList<Product> prodList = new ArrayList<>();
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }

                String[] tmp = line.split(",");
                try {
                    prodList.add(new Product(tmp[0], tmp[1], Integer.parseInt(tmp[2]), Double.parseDouble(tmp[3])));
                } catch (Exception e) {
                    errPrint(String.format("There was a problem reading the following line: %s\nNothing was added\n", line));
                    return;
                }
            }

            if (prodList.size() == 0) {
                errPrint("Product list was empty!");
                return;
            }
            for (Product p : prodList) {
                Store store = chooseStore(String.format("Which store should I add %s to?\n", p.toFormat()));

                Client.sendCommand("addProductOption", new String[] { store.getName(), p.getName(), p.getDescription(), String.valueOf(p.getQuantity()), String.valueOf(p.getPrice()) });

                if (Client.errorOnResponse()) {
                }
            }

            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        Seller c = null;
        for (int i = 0; i < server.sellers.size(); i++) {
            if (server.sellers.get(i).getEmail().equals(getEmail())) {
                c = server.sellers.get(i);
            }
        }
        server.sellers.remove(c);
    }

    // Checks if there are any customers
    public void viewCustomerCartsOption() {
        refreshOption();

        if (Client.customers.size() == 0) {
            errPrint("No customers!");
            return;
        }
        while (true) {
        ArrayList<String> tmp = new ArrayList<>();
        for (Customer c : Client.customers) {
            tmp.add(c.getEmail());
        }
        tmp.add("Exit");
        String[] customerEmails = new String[tmp.size()];
        customerEmails = tmp.toArray(customerEmails);
        String customerEmail = Client.getOption("Which customer's cart would you like to view?", customerEmails);
        if (customerEmail.equals("Exit")) {
            return;
        }
        Customer c = (Customer) Client.searchUsersByEmail(customerEmail);
        
        // Only add valid transactions
        ArrayList<Transaction> tmp2 = new ArrayList<>(c.getShoppingCart());
        ArrayList<Transaction> shoppingCart = new ArrayList<>();
        ArrayList<Product> products = new ArrayList<>();
        for (Transaction t : tmp2) {
            Seller seller = (Seller) Client.searchUsersByEmail(t.getSellerEmail());
            if (seller == null) {
                continue;
            }
            Store store = seller.searchStoresByName(t.getStoreName());
            if (store == null) {
                continue;
            }
            Product product = store.searchProductsByName(t.getProductName());
            if (product == null) {
                continue;
            }
            shoppingCart.add(t);
            products.add(product);
        }
        if (shoppingCart.size() == 0) {
            errPrint("No items in cart!");
            return;
        }
        
        int total = 0;
        for (Transaction t : shoppingCart) {
           total += t.getQuantityBought();
        }
        StringBuilder s = new StringBuilder();
        s.append(String.format("There are %d items in %s's cart\n", total, c.getEmail()));
        s.append("Which item would you like to view more information on?");

        tmp = new ArrayList<>();
        for (Transaction t : shoppingCart) {
            tmp.add(t.getProductName());
        }
        tmp.add("Exit");
        int optionNo = Client.getOptionNo(s.toString(), tmp);
        if (optionNo == tmp.size() - 1) {
            continue;
        }
        Transaction selectedTransaction = shoppingCart.get(optionNo);
        Product selectedProduct = products.get(optionNo);
        errPrint(selectedProduct.toString(selectedTransaction.getSellerEmail(), selectedTransaction.getStoreName()), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void viewDashboardOption() {
        refreshOption();

        HashMap<String, Integer> m1 = new HashMap<>(); // customers to number of items purchased
        HashMap<ListEntry, Integer> m2 = new HashMap<>(); // product names to sales

        for (Transaction t : Client.transactions) {
            if (!t.getSellerEmail().equals(getEmail())) {
                continue;
            }
            String customerEmail = t.getCustomerEmail();
            ListEntry identifier = new ListEntry(t.getProductName(), t.getStoreName());

            if (!m1.containsKey(customerEmail)) {
                m1.put(customerEmail, t.getQuantityBought());
            } else {
                m1.put(customerEmail, m1.get(customerEmail) + t.getQuantityBought());
            }

            if (!m2.containsKey(identifier)) {
                m2.put(identifier, t.getQuantityBought());
            } else {
                m2.put(identifier, m2.get(identifier) + t.getQuantityBought());
            }
        }

        ArrayList<ListEntry> customerList = new ArrayList<ListEntry>();
        ArrayList<ListEntry> productList = new ArrayList<ListEntry>();
        for (Map.Entry<String, Integer> entry : m1.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            customerList.add(new ListEntry(key, value.toString()));
        }
        for (Map.Entry<ListEntry, Integer> entry : m2.entrySet()) {
            ListEntry key = entry.getKey();
            Integer value = entry.getValue();

            productList.add(new ListEntry(key.get(0), key.get(1), value.toString()));
        }

        while (true) {
        String listingChoice = Client.getOption("Which listing would you like to view?", new String[] { "Customers by number of items purchased", "Products by number of sales", "Exit" });
        if (listingChoice.equals("Exit")) {
            return;
        }
        if (listingChoice.equals("Customers by number of items purchased")) {
            StringBuilder s = new StringBuilder();
            for (ListEntry e : customerList) {
                s.append(String.format("Customer %s has purchased %s products.\n", e.get(0), e.get(1)));
            }
            s.append("Would you like to sort the stores?");
            String yesNo = Client.getOption(s.toString(), new String[] { "Yes", "No" });
            if (yesNo.equals("No")) {
                continue;
            }

            // Should sort
            int sortMethod = Client.getOptionNo("How would you like to sort them?", new String[] { "By customer name", "By number of items purchased" });
            boolean isNum = sortMethod == 1;
            Collections.sort(customerList, ListEntry.makeComparator(sortMethod, isNum));
            s.setLength(0);
            for (ListEntry e : customerList) {
                s.append(String.format("Customer %s has purchased %s products.\n", e.get(0), e.get(1)));
            }
            errPrint(s.toString(), JOptionPane.INFORMATION_MESSAGE);
        } else if (listingChoice.equals("Products by number of sales")) {
            StringBuilder s = new StringBuilder();
            for (ListEntry e : productList) {
                s.append(String.format("Product %s from store %s has %s sales.\n", e.get(0), e.get(1), e.get(2)));
            }
            s.append("Would you like to sort the products?");
            String yesNo = Client.getOption(s.toString(), new String[] { "Yes", "No" });
            if (yesNo.equals("No")) {
                continue;
            }

            //Should sort
            int sortMethod = Client.getOptionNo("How would you like to sort them?", new String[] { "By product name", "By store name", "By quantity purchased" });
            boolean isNum = sortMethod == 2;
            Collections.sort(productList, ListEntry.makeComparator(sortMethod, isNum));
            s.setLength(0);
            for (ListEntry e : productList) {
                s.append(String.format("Product %s from store %s has %s sales.\n", e.get(0), e.get(1), e.get(2)));
            }
            errPrint(s.toString(), JOptionPane.INFORMATION_MESSAGE);
        }
        }
    }

    public Seller conductBusiness() {
        String option;
        label:
        while (true) {
            option = Client.getOption("Welcome, " + this.getEmail(), new String[] { "Refresh", "Add store", "Edit store", "Delete store", "Add product", "Edit product", "Delete product", "View sales by store", "Import product file", "View customer carts", "View dashboard", "User menu", "Quit" });
            switch (option) {
                case "Quit":
                    break label;
                case "User menu":
                    label1:
                    while (true) {
                        option = Client.getOption("User menu", new String[]{"Change Email", "Change Password", "Delete Account", "Quit"});
                        switch (option) {
                            case "Quit":
                                break label1;
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
                        }
                    }
                    break;
                case "Refresh":
                    refreshOption();
                    break;
                case "Add store":
                    addStoreOption();
                    break;
                case "Edit store":
                    editStoreOption();
                    break;
                case "Delete store":
                    deleteStoreOption();
                    break;
                case "Add product":
                    addProductOption();
                    break;
                case "Edit product":
                    editProductOption();
                    break;
                case "Delete product":
                    deleteProductOption();
                    break;
                case "View sales by store":
                    viewSalesByStoreOption();
                    break;
                case "Import product file":
                    importFileOption();
                    break;
                case "View customer carts":
                    viewCustomerCartsOption();
                    break;
                case "View dashboard":
                    viewDashboardOption();
                    break;
            }
        }
        return null;
    }
}
