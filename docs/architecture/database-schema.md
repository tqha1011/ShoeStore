# Database Architecture Documentation

## 1. Why PostgreSQL?

For the `ShoeStore` project, **PostgreSQL** was selected as the primary relational database management system (RDBMS). The decision is driven by the following key factors:

* **Advanced E-commerce Capabilities:** PostgreSQL offers robust support for complex queries, transactions, and concurrency control (MVCC), which are crucial for handling simultaneous cart checkouts and inventory updates in an e-commerce platform.
* **Seamless Entity Framework Core Integration:** The `Npgsql.EntityFrameworkCore.PostgreSQL` provider is highly optimized. We also utilized the `UseSnakeCaseNamingConvention()` extension to automatically map C# `PascalCase` properties to PostgreSQL's standard `snake_case` columns, keeping the database schema clean and standardized.
* **Security & Data Integrity:** PostgreSQL provides excellent data integrity constraints. Combined with our use of `Guid` for exposed IDs (Public IDs), we effectively mitigate Insecure Direct Object Reference (IDOR) vulnerabilities.
* **Open-Source & Enterprise-Ready:** It is a powerful, open-source database that provides enterprise-level features without licensing costs, making it the perfect choice for scalable modern applications.

---

## 2. Entity Relationship Diagram (ERD)

*(Below is the ERD illustrating the relationships between the core entities in the ShoeStore system.)*

<img width="1255" height="773" alt="image" src="https://github.com/user-attachments/assets/2cc3c94b-93bc-4b7a-bedc-92188a5d978f" />


---

## 3. Core Tables Description

The database schema is heavily normalized to reduce redundancy and maintain data integrity. The tables are logically grouped into three main domains:

### 3.1. Authentication & Identity Domain
* **`users`**: The central table for authentication and authorization. 
    * Stores `email`, hashed `password`, and `role` (Customer/Admin).
    * Uses a `public_id` (`UUID`) exposed to the frontend instead of the primary key to enhance security.
* **`user_refresh_tokens`**: Manages JWT refresh tokens for maintaining long-lived user sessions securely.
* **`user_restore_passwords`**: Tracks OTPs and expiration times for the "Forgot Password" flow.

### 3.2. Product Catalog Domain
To handle a diverse inventory of shoes (different colors and sizes), the product catalog implements a hierarchical structure:
* **`products`**: Contains the base information of a shoe model (e.g., Name, Description, Brand, Base Price).
* **`colors`**: A lookup table defining available colors.
* **`product_variants`**: Represents a specific color version of a `Product`. It links a `Product` to a `Color` and holds specific image URLs for that variant.
* **`product_sizes`**: Represents the physical stock. It links a `ProductVariant` to a specific shoe size and tracks the `QuantityInStock`.

### 3.3. Sales & Order Management Domain
* **`cart_items`**: Acts as a temporary holding area for users' desired items. It maps a `User` to a specific `ProductSize` along with the desired `Quantity`.
* **`invoices`**: Represents a finalized customer order. Tracks the `TotalAmount`, shipping details, and the overall `InvoiceStatus` (e.g., Pending, Processing, Shipped, Cancelled).
* **`invoice_details`**: The line items of an invoice. It freezes the `UnitPrice` at the time of purchase to ensure historical accuracy even if the base product price changes later.
* **`payments`**: Records the transaction details (Payment Method, Payment Status, Transaction ID) linked to a specific `Invoice`.

### 3.4. Promotion Domain
* **`vouchers`**: Stores discount campaigns (Discount percentage, Max discount amount, Expiration date).
* **`user_vouchers`**: A mapping table that tracks which user has collected or used which voucher, preventing multi-use abuse.
