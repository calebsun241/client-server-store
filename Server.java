import java.io.*;
import java.net.*;
import java.util.*;
/**
 * Project 5 -- Server
 *
 * This class is the server that runs the data reading and writing, and interacts with all clients to pass the data
 * they need.
 *
 * @authors Akash Chenthil, lab sec L15
 *          Kipling Liu, lab sec L15
 *          Leo Navarro, lab sec L15
 *          Yixiao Sun, lab sec L15
 *
 * @version April 29, 2023
 */
public class Server implements Runnable {
    public static ArrayList<Seller> sellers = new ArrayList<>();
    public static ArrayList<Customer> customers = new ArrayList<>();
    public static ArrayList<Transaction> transactions = new ArrayList<>();
    Socket socket;
    BufferedReader br;
    PrintWriter pw;

    public Server(Socket socket) {
        this.socket = socket;
    }

    public void sendSuccess() {
        pw.println("SUCCESS");
        pw.flush();
    }

    public void sendError(String errorMsg) {
        pw.printf("ERROR:%s\n", errorMsg);
        pw.flush();
    }

    public void run() {
        try {
        System.out.println("New Client connected!");
        this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.pw = new PrintWriter(socket.getOutputStream());

        User u = null; // The user that belongs to this thread
        /* We are going to keep asking until we get a valid user */
        while (true) {
            /* This is the user data */
            String[] tmp = br.readLine().split(",");
            if (tmp[0].equals("createAccount")) {
                String newEmail = tmp[1];
                String newPassword = tmp[2];
                String choice = tmp[3];

                if (searchUsersByEmail(newEmail) != null) {
                    sendError("A user with this email already exists!");
                    continue;
                }
                sendSuccess();

                if (choice.equals("Seller")) {
                    u = new Seller(newEmail, newPassword);
                    sellers.add((Seller) u);
                } 
                else if (choice.equals("Customer")) {
                    u = new Customer(newEmail, newPassword);
                    customers.add((Customer) u);
                }
                writeFile(pw);
                break;
            } else {
                String email = tmp[1];
                String password = tmp[2];
                if ((u = searchUsersByEmail(email)) == null) {
                    sendError("No users with this email exist!");
                    continue;
                }
                if (!password.equals(u.getPassword())) {
                    sendError("Wrong password!");
                    continue;
                }
                sendSuccess();
                writeFile(pw);
                break;
            }
        }
        u.server = this;

        String line;
        while ((line = br.readLine()) != null) {
            String[] tmp = line.split(",");
            String command = tmp[0];
            String[] args = Arrays.copyOfRange(tmp, 1, tmp.length);
            switch (command) {
                case "refreshOption":
                    u.refreshOptionServer();
                    break;
                case "changeEmail":
                    u.changeEmailServer(args);
                    break;
                case "changePassword":
                    u.changePasswordServer(args);
                    break;
                case "addStoreOption":
                    ((Seller) u).addStoreOptionServer(args);
                    break;
                case "editStoreOption":
                    ((Seller) u).editStoreOptionServer(args);
                    break;
                case "deleteStoreOption":
                    ((Seller) u).deleteStoreOptionServer(args);
                    break;
                case "addProductOption":
                    ((Seller) u).addProductOptionServer(args);
                    break;
                case "editProductOption":
                    ((Seller) u).editProductOptionServer(args);
                    break;
                case "deleteProductOption":
                    ((Seller) u).deleteProductOptionServer(args);
                    break;
                case "serverDeleteAccount":
                    u.serverDeleteAccount(args);
                    break;
                case "checkoutAllItems":
                    ((Customer) u).checkoutAllItemsServer();
                    break;
                case "addToShoppingCart":
                    ((Customer) u).addToShoppingCartServer(args);
                    break;
                case "addToTransactionHistory":
                    ((Customer) u).addToTransactionHistoryServer(args);
                    break;
                case "removeItemFromCart":
                    ((Customer) u).removeItemFromCartServer(args);
                    break;
                case "removeItemFromSeller":
                    ((Customer) u).removeItemFromSellerServer(args);
            }
        }
        System.out.println("Connection closed!");
        writeFile(new PrintWriter("data.txt"));

        pw.close();
        br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        try {
        File f = new File("data.txt");
        if (f.createNewFile()) {
            try {
            PrintWriter tmp = new PrintWriter(new FileOutputStream(f));
            writeFile(tmp);
            tmp.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        BufferedReader br = new BufferedReader(new FileReader(f));
        readFile(br);
        br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        System.out.println("---DEBUG---");
        for (Seller seller : sellers) {
            for (Store store : seller.getStores()) {
                System.out.println(store.getName());
                for (Product p : store.getProductsSold()) {
                    System.out.println(p.getName());
                }
            }
        }
        for (Customer c : customers) {
            System.out.println(c.getShoppingCart().toString());
            // System.out.println(c.getTransactionHistory().toString());
        }
        System.out.println(transactions.toString());
        System.out.println("---DEBUG---");
        */

        ServerSocket serverSocket = new ServerSocket(4343);

        while (true) {
            Socket socket = serverSocket.accept();
            Server server = new Server(socket);
            new Thread(server).start();
        }
    }

    public static synchronized void writeFile(PrintWriter pw) {
        MarketPlace.writeFile(sellers, customers, transactions, pw);
    }

    public static synchronized void readFile(BufferedReader br) {
        MarketPlace.readFile(sellers, customers, transactions, br);
    }

    public static synchronized User searchUsersByEmail(String email) {
        return MarketPlace.searchUsersByEmail(sellers, customers, email);
    }

    public static synchronized Seller searchSellersByEmail(String email) {
        return MarketPlace.searchSellersByEmail(sellers, email);
    }
      
    public static synchronized Customer searchCustomersByEmail(String email) {
        return MarketPlace.searchCustomersByEmail(customers, email);
    }

    public static synchronized ArrayList<User> allUsers() {
        return MarketPlace.allUsers(sellers, customers);
    }
}
