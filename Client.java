import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
/**
 * Project 5 -- Marketplace
 *
 * Description of class
 *
 * @authors Akash Chenthil, lab sec L15
 *          Kipling Liu, lab sec L15
 *          Leo Navarro, lab sec L15
 *          Yixiao Sun, lab sec L15
 *
 * @version April 29, 2023
 */
public class Client {
    public static ArrayList<Seller> sellers = new ArrayList<>();
    public static ArrayList<Customer> customers = new ArrayList<>();
    public static ArrayList<Transaction> transactions = new ArrayList<>();
    public static Socket socket;
    public static PrintWriter pw;
    public static BufferedReader br;

    /* Temporary */
    public static Scanner scan;

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 4343);

        pw = new PrintWriter(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        /* Temporary */
        scan = new Scanner(System.in);

        User u = userPrompt();

        /*
        System.out.println("---DEBUG---");
        for (Seller seller : sellers) {
            for (Store store : seller.getStores()) {
                System.out.println(store.getProductsSold().toString());
            }
        }
        for (Customer c : customers) {
            System.out.println(c.getShoppingCart().toString());
            // System.out.println(c.getTransactionHistory().toString());
        }
        System.out.println(transactions.toString());
        System.out.println("---DEBUG---");
        */

        User user = u.conductBusiness();

        System.out.println("Finished conducting business!");
        pw.close();
        br.close();

        /* Temporary */
        scan.close();
    }

    public static void sendCommand(String command, String[] args) {
        String[] tmp = new String[args.length + 1];
        tmp[0] = command;
        for (int i = 1; i < tmp.length; i++) {
            tmp[i] = args[i-1];
        }
        pw.println(String.join(",", tmp));
        pw.flush();
    }
    /*
     * Helper functions
     */
    public static User searchUsersByEmail(String email) {
        return MarketPlace.searchUsersByEmail(sellers, customers, email);
    }

    public static Seller searchSellersByEmail(String email) {
        return MarketPlace.searchSellersByEmail(sellers, email);
    }
      
    public static Customer searchCustomersByEmail(String email) {
        return MarketPlace.searchCustomersByEmail(customers, email);
    }

    public static ArrayList<User> allUsers() {
        return MarketPlace.allUsers(sellers, customers);
    }

    public static void readFile(BufferedReader br) {
        MarketPlace.readFile(sellers, customers, transactions, br);
    }

    /* MOST IMPORTANT FUNCTIONS */
    public static void rereadData() {
        sellers.clear();
        customers.clear();
        transactions.clear();
        readFile(br);
    }

    /* Returns true if there was an error on the server side */
    public static boolean errorOnResponse() {
    try {
        String response = br.readLine();
        String[] tmp = response.split(":");
        if (tmp[0].equals("ERROR")) {
            JOptionPane.showMessageDialog(null, tmp[1], "", JOptionPane.ERROR_MESSAGE);
            return true;
        } else {
            System.out.println(response);
            return false;
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return true;
    }

    public static User userPrompt() {
        String choice = MarketPlace.getOption("", new String[] { "Create account", "Login" });
        if (choice.equals("Create account")) {
            return createAccount();
        }
        else if (choice.equals("Login")) {
            return login();
        }
        return null;
    }
        
    public static User createAccount() {
        while (true) {
            String newEmail = prompt("New email: ");
            String newPassword = prompt("New password: ");
            String choice = MarketPlace.getOption("What type of user are you?", new String[] { "Seller", "Customer" });
            
            sendCommand("createAccount", new String[] { newEmail, newPassword, choice });

            if (errorOnResponse()) {
                continue;
            }

            rereadData();
            return searchUsersByEmail(newEmail);
        }
    }

    public static String prompt(String s) {
        while (true) {
            String result = JOptionPane.showInputDialog(s);
            if (result != null && !result.equals("")) {
                return result;
            }
        }
    }

    public static User login() {
        while (true) {
            String email = prompt("Email: ");
            String password = prompt("Password: ");

            sendCommand("login", new String[] { email, password });
            
            if (errorOnResponse()) {
                continue;
            }
            
            rereadData();
            return searchUsersByEmail(email);
        }
    }

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
