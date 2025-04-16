### Instructions to Run the Application

#### Prerequisites
1. **Java Development Kit (JDK)**: Ensure you have JDK 17 or higher installed.
    - Verify the installation by running:
      ```bash
      java -version
      ```

2. **Development Environment**: Use any IDE or text editor of your choice (e.g., IntelliJ IDEA, Eclipse, or Visual Studio Code) to open and manage the project.

#### Steps to Build and Run the Application
1. **Open the Project**:
    - Open the project in your preferred IDE or navigate to the project directory in your terminal.

2. **Build the Project**:
    - Use the Maven wrapper to build the project:
      ```bash
      ./mvnw clean install
      ```
    - Alternatively, use the Maven tool integrated into your IDE to clean and build the project.

3. **Run the Application**:
    - Run the application using the Maven wrapper:
      ```bash
      ./mvnw spring-boot:run
      ```
    - Alternatively, locate the main class (e.g., `OrderManagerApplication`) in your IDE and run it directly.

4. **Access the Application**:
   Once the application is running, you can access it at:
    - **Base URL**: `http://localhost:8080`
    - **H2 Console**: `http://localhost:8080/h2-console`

#### Testing the APIs
A Postman collection is provided to test the APIs. You can find the collection file at `external-files/OrderManagement.postman_collection.json`. Import this file into Postman to access pre-configured requests for the API endpoints.

#### Additional Notes
- Ensure the `application.properties` file is correctly configured for your environment.
- If you encounter issues, check the application logs for errors.

### Login to H2 Database
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:testdb  --> *setting manually from web page*
- User: sa
- Password: *empty*


### Sample Data for H2 Database

You can use the following SQL snippet to populate the H2 database with sample data:

```sql
-- Insert sample data into products
INSERT INTO products (product_id, name, description, price, inventory_count, category, created_at, updated_at) VALUES
(RANDOM_UUID(), 'Product A', 'Description for Product A', 19.99, 100, 'Category 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Product B', 'Description for Product B', 29.99, 50, 'Category 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Product C', 'Description for Product C', 39.99, 75, 'Category 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample data into orders
INSERT INTO orders (order_id, status, total_amount, shipping_address, payment_method, notes, created_at, updated_at) VALUES
(RANDOM_UUID(), 'Pending', 59.98, '123 Main St', 'Credit Card', 'Deliver before noon', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), 'Completed', 39.99, '456 Elm St', 'PayPal', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample data into order_items
INSERT INTO order_items (order_item_id, order_id, product_id, quantity, unit_price, subtotal, created_at, updated_at) VALUES
(RANDOM_UUID(), (SELECT order_id FROM orders LIMIT 1 OFFSET 0), (SELECT product_id FROM products LIMIT 1 OFFSET 0), 2, 19.99, 39.98, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(RANDOM_UUID(), (SELECT order_id FROM orders LIMIT 1 OFFSET 1), (SELECT product_id FROM products LIMIT 1 OFFSET 2), 1, 39.99, 39.99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
