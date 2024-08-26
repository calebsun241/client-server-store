import java.util.*;
import java.io.*;
import javax.swing.*;
/**
 * Project 5 -- Marketplace
 *
 * This class is responsible for reading, writing and sorting through all the data
 *
 * @authors Akash Chenthil, lab sec L15
 *          Kipling Liu, lab sec L15
 *          Leo Navarro, lab sec L15
 *          Yixiao Sun, lab sec L15
 *
 * @version April 29, 2023
 */
public class MarketPlace {
    public static final String END_MARK = "end";

    //Reads the data.txt file and records all available data into sellers, customers, and transactions
    public static void readFile(ArrayList<Seller> sellers, ArrayList<Customer> customers, ArrayList<Transaction> transactions, BufferedReader br) {
        try {
            // Reading sellers
            while (true) {
                String sellerStr = br.readLine();
                if (sellerStr.equals(END_MARK)) {
                    break;
                }

                // Parse the seller line
                String[] sellerLine = sellerStr.split(",");
                Seller currSeller = new Seller(sellerLine[0], sellerLine[1]);

                // Add the seller to sellers list
                sellers.add(currSeller);

                // Reading products
                while (true) {
                    String storeStr = br.readLine();
                    if (storeStr.equals(END_MARK)) {
                        break;
                    }
                    // Parse the store string, put it into the currSeller.stores ArrayList
                    String[] storeLine = storeStr.split(";");
                    Store currStore = new Store(storeLine[0]);

                    // Add the store to the seller
                    currSeller.addStore(currStore);

                    for (int i = 1; i < storeLine.length; i++) {
                        String[] productLine = storeLine[i].split(",");
                        Product currProduct = new Product(productLine[0], productLine[1], Integer.parseInt(productLine[2]), Double.parseDouble(productLine[3]));

                        // Add the product to the store
                        currStore.addProduct(currProduct);
                    }
                }
            }

            // Reading customers
            while (true) {
                String customerStr = br.readLine();
                if (customerStr.equals(END_MARK)) {
                    break;
                }

                // Parse customer line
                String[] customerLine = customerStr.split(",");
                Customer currCustomer = new Customer(customerLine[0], customerLine[1]);

                // Add customer to customers list
                customers.add(currCustomer);

                // Reading products in shopping cart
                while (true) {
                    String cartStr = br.readLine();
                    if (cartStr.equals(END_MARK)) {
                        break;
                    }

                    String[] cartLine = cartStr.split(";");
                    for (int i = 0; i < cartLine.length; i++) {
                        String[] productLine = cartLine[i].split(",");
                        Seller seller = (Seller) searchUsersByEmail(sellers, customers, productLine[2]);
                        if (seller == null) {
                            System.out.printf("Seller %s no longer exists!\n", productLine[2]);
                            continue;
                        }
                        Store store = seller.searchStoresByName(productLine[1]);
                        if (store == null) {
                            System.out.printf("Store %s no longer exists!\n", productLine[1]);
                            continue;
                        }
                        Product product = store.searchProductsByName(productLine[0]);
                        if (product == null) {
                            System.out.printf("Product %s no longer exists!\n", productLine[0]);
                            continue;
                        }

                        // Add product to shopping cart
                        currCustomer.getShoppingCart().add(new Transaction(product.getName(), store.getName(), seller.getEmail(),
                                product.getPrice(), Integer.parseInt(productLine[4]), currCustomer.getEmail()));
                    }
                }
            }

            // Reading transactions
            while (true) {
                String transStr = br.readLine();
                if (transStr.equals(END_MARK)) {
                    break;
                }
                String[] transLine = transStr.split(",");
                transactions.add(new Transaction(transLine[0], transLine[1], transLine[2],
                        Double.parseDouble(transLine[3]), Integer.parseInt(transLine[4]), transLine[5]));
                for (Customer cust : customers) {
                    if (cust.getEmail().equals(transLine[5])) {
                        cust.getTransactionHistory().add(transactions.get(transactions.size() - 1));
                        break;
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    public static User searchUsersByEmail(ArrayList<Seller> sellers, ArrayList<Customer> customers, String email) {
        ArrayList<User> users = allUsers(sellers, customers);
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                return u;
            }
        }
        return null;
    }

    public static Customer searchCustomersByEmail(ArrayList<Customer> customers, String email) {
        for (Customer c : customers) {
            if (c.getEmail().equals(email)) {
                return c;
            }
        }
        return null;
    }

    public static Seller searchSellersByEmail(ArrayList<Seller> sellers, String email) {
        for (Seller s : sellers) {
            if (s.getEmail().equals(email)) {
                return s;
            }
        }
        return null;
    }
      
    public static void writeFile(ArrayList<Seller> sellers, ArrayList<Customer> customers, ArrayList<Transaction> transactions, PrintWriter pw) {
        try {
            for (Seller seller : sellers) {
                pw.println(seller.toFormat());
                pw.println(END_MARK); // Ending an individual seller
            }
            pw.println(END_MARK); // Ending the list of sellers

            for (Customer customer : customers) {
                pw.println(customer.toFormat());
                pw.println(END_MARK); // Ending an individual customer
            }
            pw.println(END_MARK); // Ending the list of customers

            for (Transaction transaction : transactions) {
                pw.println(transaction.toFormat());
            }
            pw.println(END_MARK);

            pw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<User> allUsers(ArrayList<Seller> sellers, ArrayList<Customer> customers) {
        ArrayList<User> ret = new ArrayList<>();
        ret.addAll(sellers);
        ret.addAll(customers);
        return ret;
    }

    // Now, you need to add a prompt string in your getOption call.
    // If you don't, then the prompt will be blank.
    public static String getOption(String prompt, String[] options) {
        return options[getOptionNo(prompt, options)];
    }

    public static String getOption(String[] options) {
        return getOption("", options);
    }

    public static int getOptionNo(String prompt, String[] options) {
        int choice = -1;
        while (choice == -1) {
            choice = JOptionPane.showOptionDialog(null, prompt, "", 
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        }
        return choice;
    }

    public static String getOption(String prompt, ArrayList<String> options) {
        String[] tmp = new String[options.size()];
        tmp = options.toArray(tmp);
        return getOption(prompt, tmp);
    }

    public static int getOptionNo(String prompt, ArrayList<String> options) {
        String[] tmp = new String[options.size()];
        tmp = options.toArray(tmp);
        return getOptionNo(prompt, tmp);
    }
}
