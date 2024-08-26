# cs180-project-5
## Testing
### Notes
- These tests will only work with an unmodified `data.txt` because `data.txt` is pre-populated with data. Before you run tests, type `git restore data.txt` to ensure that `data.txt` is in its original state.
- Tests are in the order they should run, as later tests reference data created from prior test.
- For all steps where a text field is written to, the (OK) button, or enter key are hit unless specified otherwise.

### Test 1: Creating Seller
Steps:
1. Run Server and then Client
2. Click on the (Create account) button
3. Enter the email: "seller@gmail.com"
4. Enter the password: "12345"
5. Click the (Seller) Button
6. Click the (Add Store) Button
7. Enter the store name: "Walmart"
8. Click the (Add product) Button
9. Click the (Walmart) Button
10. Enter the product name: "Chair"
11. Enter the description: "For sitting"
12. Enter the quantity: "32"
13. Enter the price: "120"
14. Click the (Quit) Button

Expected result: The followed lines of text should be appended to the data.txt file:

    seller@gmail.com,12345
    Walmart;Chair,For sitting,32,120.0
    end

Test Status: Passed.

### Test 2: Creating Buyer
Steps:
1. Run Server and then Client
2. Click on the (Create account) button
3. Enter the email: "buyer@gmail.com"
4. Enter the password: "54321"
5. Click the (Customer) Button
6. Click the (View Marketplace) Button
7. Select [Choose Product] from the dropdown menu
8. Enter the number "2" to select the chair product
9. Click the (Yes) Button
10. Enter the quantity: "4"
11. Click the (OK) Button to exit 
12. Click the (Quit) Button

Expected result: The followed lines of text should be appended to the data.txt file:

    end
    buyer@gmail.com,54321
    Chair,Walmart,seller@gmail.com,120.0,4,buyer@gmail.com

Test Status: Passed.

### Test 3: Concurrency Test
Steps:
1. Ensure that you can run multiple clients (check README.md for instructions)
2. Run Server and then the first Client 
3. Click on the (Login) Button 
4. Enter the email: "buyer@gmail.com"
5. Enter the password: "54321"
6. Run the second Client 
7. Click on the (Login) Button 
8. Enter the email: "seller@gmail.com"
9. Enter the password: "12345"
10. In second Client, Click on the (view customer carts) Button
11. Click on (buyer@gmail.com) Button

Expected: The line "There are 4 items in buyer@gmail.com's cart" should be present
12. Click on the (Exit) Button Twice
13. Click on the (View sales by store) Button
14. Click on the (Walmart) Button

Expected: An Error Message Stating "No transactions associated with this store"
15. Click on the (View Shopping Cart) Button on the First Client
16. Select [Checkout] from the dropdown menu
17. Click on the (Transaction history) Button

Expected: Info on the Chair product purchased.
18. In second Client, Click on the (view customer carts) Button
19. Click on (buyer@gmail.com) Button

Expected: An Error Message Stating "No items in cart!"
20. Click on the (View sales by store) Button
21. Click on the (Walmart) Button

Expected: Info on the Chair product sold.
22. Click the (Quit) Button on both Clients.

Expected result: The followed lines of text should have moved to the bottom of the data.txt file:

    Chair,Walmart,seller@gmail.com,120.0,4,buyer@gmail.com

Test Status: Passed.

### Test 4: Product File
Steps:
1. Run Server and then Client
2. Click on the (Login) button
3. Enter the email: "seller@gmail.com"
4. Enter the password: "12345"
5. Click on the (Add Store) Button
6. Enter the name: "Staples"
7. Click on the (Import product file) Button
8. Enter the file name: "test4.csv"
9. Click on the (Staples) Button 4 times
10. Click on the (Delete store) Button
11. Click on the (Walmart) Button
12. Click on the (Delete Product) Button
13. Click on the (Staples) Button
14. Click on the (Tape) Button
15. Click on the (Edit product) Button
16. Click on the (Staples) Button
17. Click on the (Markers) Button
18. Enter the name: "Sharpies"
19. Enter the description: "Permanent marker"
20. Enter the quantity: "256"
21. Enter the price "3"
22. Click on the (Quit) Button

Expected Result: The followed lines of text should be in the data.txt file:

    seller@gmail.com,12345
    Staples;PostIts,Mini notepads,124,5.0;Pencils,Writes on paper with lead,400,1.0;Sharpies,permanent marker,256,3.0
    end

Test Status: Passed

### Test 5: Sorting, Exporting, and Dashboard
Steps:
1. Run Server and then Client
2. Click on the (Login) button
3. Enter the email: "buyer@gmail.com"
4. Enter the password: "54321"
5. Click on the (View Marketplace) Button
6. Select [Sort] from the dropdown menu
7. Select [By Quantity Available] from the dropdown menu
8. Select [Choose Product] from the dropdown menu
9. Enter the number "4" to select the pencils product
10. Click the (Yes) Button
11. Enter the quantity: "12"
12. Select [By Price] from the dropdown menu
13. Select [Choose Product] from the dropdown menu
14. Enter the number "1" to select the sharpies product
15. Click the (Yes) Button
16. Enter the quantity: "6"
17. Click the (Cancel) Button
18. Select [Exit] from the dropdown menu
19. Click on the (View ShoppingCart) Button
20. Select [Checkout] from the dropdown menu
21. Click on the (Export a File with Purchase History) Button
22. Click on the (View Dashboard) Button
23. Select [All Stores] from the dropdown menu

Expected: store1 has sold 2 products and Staples has sold 18 products
24. Click the (No) Button
25. Select [Exit] from the dropdown menu
26. Click on the (Quit) Button

Expected Result: there should be a buyerPurchaseHistory.csv with the following data:

    "seller@gmail.com","Walmart","Chair","$120.00","4"
    "seller@gmail.com","Staples","Pencils","$1.00","12"
    "seller@gmail.com","Staples","Sharpies","$3.00","6"

Test Status: Passed

### Test 6: Review and Deletion
Steps:
1. Run Server and then Client
2. Click on the (Login) button
3. Enter the email: "seller@gmail.com"
4. Enter the password: "12345"
5. Click on the (View Dashboard) Button
6. Click on the (Products by number of sales) Button

Expected: 4 chairs, 12 pencils, and 6 sharpies have been sold.
7. Click the (No) Button
8. Click on the (Exit) Button
9. Click on the (User menu) Button
10. Click on the (Delete Account) Button
11. Click the (Yes) Button
11. Run Client again
12. Click on the (Login) button
13. Enter the email: "buyer@gmail.com"
14. Enter the password: "54321"
15. Click on the (User menu) Button
16. Click on the (Delete Account) Button
17. Click the (Yes) Button

Expected Result: Buyer@gmail.com and seller@gmail.com are no longer in data.txt
Attempting to login again will prompt the error: "No users with this email exist!"
