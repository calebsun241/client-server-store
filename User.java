import javax.swing.*;
// TODO: allow changing of email, password, and deletion of account through a user menu
/**
 * Project 5 -- User
 *
 * Is the parent class of Seller and Customer. Contains the email and password fields for the users. Also contains
 * functionality to change the user's email and password.
 *
 * @authors Akash Chenthil, lab sec L15
 *          Kipling Liu, lab sec L15
 *          Leo Navarro, lab sec L15
 *          Yixiao Sun, lab sec L15
 *
 * @version April 29, 2023
 */
public class User {
    private String email;
    private String password;
    public Server server;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String toFormat() {
        return email + "," + password;
    }

    public static String prompt(String s) {
        return Client.prompt(s);
    }

    public void changeEmail() {
        String newEmail = prompt("Enter new email: ");

        Client.sendCommand("changeEmail", new String[] { newEmail });

        if (Client.errorOnResponse()) {
        } else {
            this.email = newEmail;
        }

        refreshOption();
    }
    public synchronized void changeEmailServer(String[] args) {
        if (server.searchUsersByEmail(args[0]) != null) {
            server.sendError("This email is already in use");
        }
        server.sendSuccess();
        this.email = args[0];
    }

    public void changePassword() {
        String newPassword = prompt("Enter new password: ");
        Client.sendCommand("changePassword", new String[] { newPassword });
    }
    public synchronized void changePasswordServer(String[] args) {
        this.password = args[0];
    }

    public User conductBusiness() {
        return null;
    }

    public void refreshOption() {
        System.out.println("Refreshing...");
        Client.sendCommand("refreshOption", new String[] { });
        Client.rereadData();
    }
    public synchronized void refreshOptionServer() {
        System.out.println("Refreshing...");
        server.writeFile(server.pw);
    }

    public synchronized void serverDeleteAccount(String[] args) {
    }
}
