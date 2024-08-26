## How to run

### From IntelliJ

Run Server, then run Client.
For running multiple Client instances, see [here](https://stackoverflow.com/questions/41226555/how-do-i-run-the-same-application-twice-in-intellij)

Detailed instructions
1. Open Run -> Edit configurations
2. Press the `+` (plus), then press `Application`
3. Name the application `Client`
4. Set the `Main class` field to `Client`
5. Click on `Modify options`, and check `Allow multiple instances`
6. Press `OK`

Now you should be able to run the Client more than once with the green "Run" button.

### From the command-line on Unix devices (MacOS, Linux)

1. Navigate to the project directory
2. Type `./run Server` to compile and run the server
3. Type `./run Client` to compile and run the client

### From the command-line on Windows devices

You will not be able to run the script. Instead:

1. Navigate to the project directory
2. Manually compile all java files with `javac`
3. Type `java Server` to run the server
4. **IMPORTANT**: manually recompile all java files with `javac`
5. Type `java Client` to run the client

When you want to run a new client, you must always recompile all of the class files.
This is probably due to some bug in the JVM. If you do not do this, you may get errors like
`Client.pw is null`.

### Notes
- Server must always be run before Client.
- The server will run indefinitely. To exit, press `Ctrl-c` or whatever is used to send an interrupt signal.
- Output to the console is for debugging purposes and should be ignored. All user-facing output is done through GUIs.
- Persistent data is stored in `data.txt` in the respository. Do not modify this file manually.
- `data.txt` is prepopulated with data that is used to run the tests in `Tests.md`. Before you run tests, type `git restore data.txt` to ensure that `data.txt` is in its original state.

## Submission
Kipling Liu will submit the git repository to Vocareum and the report to Brightspace.
Chance will submit the video.

## Class Descriptions

### Client.java
- Driver for client code; calls out to Seller and Customer to perform most functionality
- Implements a function to send a command to Server

### Server.java
- Driver for server code
- Creates a new socket for each Client connection and processes commands sent through the socket
- Keeps track of application data in-memory; also periodically updates the data file

### User.java
- Parent class of Seller and Customer
- Contains attributes common to sellers and customers, such as an email
- Implements functions common to all users, such as changing password

### Seller.java
- Represents a seller as a list of the seller's stores
- Implements all of the functions that a seller can perform, such as adding stores or deleting products

### Customer.java
- Represents a customer as a shopping cart and a transaction history
- Implements all of the functions that a customer can perform, such as viewing the marketplace or adding an item to cart

Note: for User, Seller, and Customer, functions are split into the part that runs on the server side, and the part that runs on the client side. For example, `addStoreOption` will send a request to the Server to add a store with a particular name, and `addStoreOptionServer` processes that request. The code that connects these two methods is in Server.java in a big switch statement.

### Store.java
- Represents a store as a list of products and a name (the name of the store)

### Product.java
- Represents a product as a bunch of attributes: product name, description, quantity, and price

### Transaction.java
- Represents a transaction, which is used to represent an item in a customer's transaction history and to represent an item in a customer's shopping cart
- The reason we cannot just use Products is because Products can be deleted from stores

### ListEntry.java
- A wrapper around ArrayList that makes sorting easier

### MarketPlace
- Implements helper functions used on both the client and server side.
- Implements functions to parse and output the data file format
