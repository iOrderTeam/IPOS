# IPOS-PU Development Log
**Developer:** Seyer Raji
**Branch:** `feature/catalogue-cart-orders`
**Role:** Catalogue, Cart & Orders

---

## Overview

This log documents the development of the IPOS-PU subsystem, tracing each file created back to the requirements in the Student's Brief and the Requirements & High-Level Design Document.

The Student's Brief defines IPOS-PU as:

> "An online platform (IPOS - PU) to be used by a merchant for direct sales of pharmaceutical goods to the public. The platform will allow the merchants to run advertisement/promotion campaigns with new products with discounts."

The Requirements Document (Introduction, Section 2) further states:

> "The IPOS-PU subsystem will provide an online portal through which a merchant can sell pharmaceutical goods directly to members of the public, and will support customer registration, online ordering, payment processing, order tracking, and promotional campaigns."

Seyer is responsible for the **IPOS-PU-SALES** package, which the Student's Brief defines as:

> "IPOS-PU-SALES: The package will allow customers to view the catalogue of goods offered by the respective merchant. Filtering of the catalogue should be provided (e.g., to constrain the list to goods to those that match a keyword, e.g. "paracetamol") to narrow down the list of products of interest to the customer. The customer will be able select from the catalogue the products they require and place them in a shopping cart, confirm the order and checkout paying for them using a credit/debit card. A successful purchase will lead to an email being sent to the customer with details about how to track the order (from being received, to being dispatched and then delivered). The platform should maintain the status of the order until it is eventually delivered. Successful orders via IPOS-PU should be "propagated" to the respective merchant (i.e. IPOS-CA): the merchant's inventory will be affected (deducting the amount of ordered goods)."

---

## Phase 1 — Product Catalogue (Target: 15 March)

### 1. `src/main/java/com/ipos/pu/model/Product.java`
**Created:** 11 March 2026

**What it is:** A JPA entity class that maps to a `products` table in the database. It defines the data structure for a pharmaceutical product: name, brand, description, price, and stock quantity.

**Why it was created:** The Student's Brief states that IPOS-PU-SALES must allow customers to:

> "view the catalogue of goods offered by the respective merchant"

For a catalogue to exist, the system must first have a data model representing a product. This file is the foundation — without it, no product data can be stored or retrieved. It also directly supports **Use Case 8 (ViewCatalogue)** from the Requirements Document:

> "Allows the customer to browse the full list of pharmaceutical products offered by the merchant on the IPOS-PU online portal. Option to filter results and add items to their shopping cart."
>
> Postconditions: "The system displays the catalogue containing Item IDs, descriptions, unit prices, and current availability."

The fields `name`, `brand`, `description`, `price`, and `stockQuantity` directly correspond to these required display attributes.

**How it fits into the overall IPOS architecture:** The `Product` model is shared across subsystems. Manasar's IPOS-CA integration will read product stock to deduct inventory after orders are placed. The brief states:

> "Successful orders via IPOS-PU should be 'propagated' to the respective merchant (i.e. IPOS-CA): the merchant's inventory will be affected (deducting the amount of ordered goods)."

---

### 2. `src/main/java/com/ipos/pu/repository/ProductRepository.java`
**Created:** 11 March 2026

**What it is:** A Spring Data JPA repository interface. It provides automatic database query methods for the `Product` entity, including a keyword search method `findByNameContainingIgnoreCase`.

**Why it was created:** The Student's Brief specifies that catalogue filtering must be supported:

> "Filtering of the catalogue should be provided (e.g., to constrain the list to goods to those that match a keyword, e.g. 'paracetamol') to narrow down the list of products of interest to the customer."

This is also captured as a dedicated use case in the Requirements Document — **Use Case 9 (FilterCatalogue)**:

> "Allows the customer to narrow down the list of products in the catalogue through specific keywords such as 'paracetamol', Item ID, or the brand of the product."

The `findByNameContainingIgnoreCase(String keyword)` method directly implements this requirement — it allows the service layer to search products by name keyword without writing manual SQL.

**How it fits into the overall IPOS architecture:** This repository is the data access layer sitting between the database and the service layer. It follows the standard layered architecture pattern used throughout IPOS-PU (Model → Repository → Service → Controller → UI).

---

### 3. `src/main/java/com/ipos/pu/service/CatalogueService.java`
**Created:** 11 March 2026

**What it is:** A Spring `@Service` class that sits between the repository and the controller. It contains the business logic for retrieving products — fetching all products or fetching one by ID.

**Why it was created:** The Student's Brief requires that:

> "The customer will be able select from the catalogue the products they require and place them in a shopping cart."

Before a customer can select a product, the system must be able to retrieve and present the full product list. `CatalogueService` handles this responsibility — it is the layer that decides *what* data to return and *how*, keeping that logic separate from both the database queries (repository) and the user interface (controller).

This supports **Use Case 8 (ViewCatalogue)**:

> "Allows the customer to browse the full list of pharmaceutical products offered by the merchant on the IPOS-PU online portal."

**How it fits into the overall IPOS architecture:** The service layer is the core of IPOS-PU's business logic. Following the layered architecture pattern, controllers and UI controllers never talk directly to the database — they always go through the service. This makes the system easier to test and maintain.

---

### 4. `src/main/java/com/ipos/pu/controller/CatalogueController.java`
**Created:** 11 March 2026

**What it is:** A Spring `@RestController` that exposes HTTP API endpoints for products. It provides two endpoints — `GET /api/products` to retrieve all products, and `GET /api/products/{id}` to retrieve a single product by ID.

**Why it was created:** The Requirements Document states IPOS-PU must expose its functionality to other subsystems. The brief describes the need for cross-subsystem communication:

> "It will integrate with the merchant's local point-of-sale and inventory (IPOS-CA) so that successful online purchases decrement the merchant's stock, and with the central catalogue and account services (IPOS-SA) for product data, pricing, and merchant membership verification."

The REST controller is the interface through which other subsystems (IPOS-CA, IPOS-SA) can access product data over HTTP. It also supports the desktop UI by providing a clean API layer, consistent with the layered architecture pattern across the whole IPOS system.

**How it fits into the overall IPOS architecture:** This is the outermost layer of the backend stack (Model → Repository → Service → **REST Controller**). It translates HTTP requests into service calls and returns HTTP responses, keeping the transport layer completely separate from business logic.

---

### 5. `src/main/resources/com/ipos/pu/ui/catalogue.fxml`
**Created:** 11 March 2026

**What it is:** The JavaFX screen layout file for the product catalogue. It defines a table showing product name, brand, price and stock, with an "Add to Cart" button and a "Back" button.

**Why it was created:** The Student's Brief requires a visual catalogue that customers can interact with:

> "The customer will be able select from the catalogue the products they require and place them in a shopping cart."

The FXML file is the visual implementation of **Use Case 8 (ViewCatalogue)**, whose postcondition states:

> "The system displays the catalogue containing Item IDs, descriptions, unit prices, and current availability."

The table columns (Name, Brand, Price, In Stock) directly satisfy these display requirements. The "Add to Cart" button connects directly to **Use Case 13 (AddToCart)**:

> "Allows the customer to select products and adds quantities from the catalogue to prepare an order."

**How it fits into the overall IPOS architecture:** FXML files are the view layer of IPOS-PU's desktop UI. They define what the user sees, while the UI controller (below) defines what happens when the user interacts with the screen.

---

### 6. `src/main/java/com/ipos/pu/ui/controller/CatalogueController.java`
**Created:** 11 March 2026

**What it is:** The JavaFX UI controller for the catalogue screen. It populates the product table from `CatalogueService`, handles the "Add to Cart" button by calling `CartService`, and navigates back to the main menu.

**Why it was created:** The UI controller bridges the visual layout (FXML) and the backend services. It implements the customer-facing interaction required by the brief:

> "The customer will be able select from the catalogue the products they require and place them in a shopping cart."

It also handles the alternative flow defined in **Use Case 13 (AddToCart)**:

> "InsufficientStockAvailability"

— by displaying a message if no product is selected before clicking "Add to Cart".

**How it fits into the overall IPOS architecture:** This completes the full stack for the catalogue feature: `Product` (data) → `ProductRepository` (database) → `CatalogueService` (logic) → `CatalogueController` REST API (external access) → `catalogue.fxml` + UI `CatalogueController` (user interface).

---

### 7. `src/main/resources/com/ipos/pu/ui/main.fxml` (updated)
**Updated:** 13 March 2026

**What it is:** The main home screen layout. Updated to include two sets of buttons — guest buttons (Login, Register) and logged-in buttons (View Catalogue, My Cart, Track My Orders, Logout) — which show and hide based on login state.

**Why it was created:** The Student's Brief requires that after login, the customer can access the shopping features:

> "IPOS-PU-SALES: The package will allow customers to view the catalogue of goods offered by the respective merchant... The customer will be able select from the catalogue the products they require and place them in a shopping cart."

Without a post-login dashboard, customers have no way to navigate to the catalogue or cart after authenticating. This update directly enables the flow: Login → Dashboard → Catalogue.

**How it fits into the overall IPOS architecture:** The main screen is the central navigation hub of IPOS-PU. It connects all of Seyer's screens (catalogue, cart, orders) and Basit's screens (promotions) into a single entry point.

---

### 8. `src/main/java/com/ipos/pu/ui/controller/MainController.java` (updated)
**Updated:** 13 March 2026

**What it is:** The controller for the main screen. Updated to show/hide buttons based on `SessionManager.isLoggedIn()`, and to handle navigation to all logged-in screens.

**Why it was created:** The brief requires the system to maintain a session and restrict access to features:

> "Use Case 6 (Login) — Postconditions: User is authenticated and granted access. Session established."

The `MainController` reads the session state on every load and adjusts the UI accordingly — guests see Login/Register, logged-in members see the full navigation menu. This is the 5 April milestone delivered early to unblock testing of the catalogue screen.

**How it fits into the overall IPOS architecture:** This controller coordinates navigation across all three of Seyer's feature areas (catalogue, cart, orders) and integrates with Basit's promotions screen via the "View Promotions" button to be added when `campaigns.fxml` is ready. The 15 March milestone is now fully complete.

---

## Phase 2 — Shopping Cart (Target: 22 March)

### 9. `src/main/java/com/ipos/pu/model/CartItem.java`
**Created:** 19 March 2026

**What it is:** A JPA entity class that maps to a `cart_items` table. It represents a single line in a customer's cart — linking a `Member`, a `Product`, and a `quantity`.

**Why it was created:** The Student's Brief requires:

> "The customer will be able select from the catalogue the products they require and place them in a shopping cart."

For the system to persist a customer's selections between screens, each selected product must be stored as a `CartItem` in the database. This entity is the data model that enables that. It supports **Use Case 13 (AddToCart)**:

> "Allows the customer to select products and adds quantities from the catalogue to prepare an order."

The `@ManyToOne` relationship to both `Member` and `Product` reflects that many cart items can belong to one member, and a product can appear in many carts — a standard e-commerce data model.

**How it fits into the overall IPOS architecture:** `CartItem` is the bridge between the catalogue and the order. When a customer places an order, `OrderService` reads the `CartItem` records for that member, converts them into `OrderItem` records, and clears the cart.

---

### 10. `src/main/java/com/ipos/pu/repository/CartItemRepository.java`
**Created:** 19 March 2026

**What it is:** A Spring Data JPA repository for `CartItem`. Provides `findByMember()` to retrieve a customer's cart, and `deleteByMember()` to clear it after an order is placed.

**Why it was created:** The brief requires the system to support the full cart lifecycle — adding items, viewing the cart, and clearing it on checkout:

> "confirm the order and checkout paying for them using a credit/debit card."

`deleteByMember()` is specifically needed by `CartService.clearCart()`, which is called by `OrderService.placeOrder()` after a successful order is saved — ensuring the cart is emptied after purchase.

**How it fits into the overall IPOS architecture:** Follows the same repository pattern as `ProductRepository` and `MemberRepository`. The `@Transactional` annotation on `clearCart()` in the service layer ensures the delete is atomic.

---

### 11. `src/main/java/com/ipos/pu/service/CartService.java`
**Created:** 19 March 2026

**What it is:** A Spring `@Service` containing all cart business logic: `addToCart()`, `getCart()`, `clearCart()`, and `getCartTotal()`.

**Why it was created:** The Student's Brief requires the full shopping cart flow:

> "The customer will be able select from the catalogue the products they require and place them in a shopping cart, confirm the order and checkout paying for them using a credit/debit card."

Each method maps to a specific requirement:
- `addToCart()` — implements **Use Case 13 (AddToCart)**
- `getCart()` / `getCartTotal()` — implements **Use Case 14 (ViewShoppingCart)**: *"Allows the customer to view the contents of their shopping cart and the total cost of all items."*
- `clearCart()` — called internally by `OrderService` after a successful order, completing the purchase flow

**How it fits into the overall IPOS architecture:** `CartService` is a dependency of both the UI cart controller and `OrderService`. When an order is placed, `OrderService` calls `cartService.getCart()` to read the items, then `cartService.clearCart()` to empty it — making the cart the direct input to the order.

---

### 12. `src/main/java/com/ipos/pu/controller/CartController.java`
**Created:** 19 March 2026

**What it is:** A Spring `@RestController` exposing three HTTP endpoints: `POST /api/cart/add`, `GET /api/cart/{memberId}`, and `DELETE /api/cart/{memberId}`.

**Why it was created:** Consistent with the REST API pattern established in Phase 1, every feature in IPOS-PU exposes its functionality over HTTP. This allows the cart to be tested independently via tools such as Postman, and provides an integration point for other subsystems if needed.

**How it fits into the overall IPOS architecture:** Follows the same layered pattern as `CatalogueController` — the REST controller is a thin layer that delegates all logic to `CartService`.

---

### 13. `src/main/resources/com/ipos/pu/ui/cart.fxml`
**Created:** 19 March 2026

**What it is:** The JavaFX screen layout for the shopping cart. Displays a table of cart items (product name, quantity, unit price, line total), a total label, and "Place Order" / "Back" buttons.

**Why it was created:** The Student's Brief requires a visual cart that customers can review before placing an order:

> "confirm the order and checkout paying for them using a credit/debit card."

Before confirmation, the customer must be able to see what is in their cart and the total cost. This screen satisfies **Use Case 14 (ViewShoppingCart)**:

> "Allows the customer to view the contents of their shopping cart and the total cost of all items."

The "Place Order" button navigates to `checkout.fxml`, directly enabling the checkout flow defined in **Use Case 15 (Checkout)**.

**How it fits into the overall IPOS architecture:** `cart.fxml` sits between the catalogue and checkout in the customer journey: Catalogue → **Cart** → Checkout → Order Confirmation.

---

### 14. `src/main/java/com/ipos/pu/ui/controller/CartController.java`
**Created:** 19 March 2026

**What it is:** The JavaFX UI controller for the cart screen. Populates the cart table from `CartService` using the current member's session, calculates and displays the total, and handles navigation to checkout or back to the catalogue.

**Why it was created:** The UI controller wires the visual layout to the backend service, implementing the interaction flow required by the brief. It reads `SessionManager.getCurrentMember().getId()` to scope the cart to the logged-in customer — ensuring customers only see their own items.

**Note:** This class uses `@Component("cartUiController")` to avoid a Spring bean name conflict with the REST `CartController` in the `controller` package, which Spring would otherwise treat as a duplicate bean name.

**How it fits into the overall IPOS architecture:** Completes the full cart stack: `CartItem` (data) → `CartItemRepository` (database) → `CartService` (logic) → `CartController` REST API (external access) → `cart.fxml` + UI `CartController` (user interface).

---

### UI Navigation Update — Catalogue screen
**Updated:** 19 March 2026

**Files updated:** `catalogue.fxml`, `ui/controller/CatalogueController.java`, `main.fxml`, `ui/controller/MainController.java`

**What changed:**
- `catalogue.fxml`: Added a "My Cart" button to the bottom-right of the catalogue screen, opposite the "Add to Cart" and "Back" buttons on the left. A `Region` with `HBox.hgrow="ALWAYS"` pushes the "My Cart" button to the far right.
- `CatalogueController.java`: Added `onCartClicked()` handler navigating to `cart.fxml`. Also fully wired `CartService` into the controller, replacing the placeholder comment from Phase 1.
- `main.fxml` / `MainController.java`: Removed the "My Cart" button from the main dashboard. Cart is now accessed exclusively via the catalogue screen, which is the natural point in the customer journey where cart access is needed.

**Why:** Placing the cart button in the catalogue screen reflects the natural customer journey — a customer adds items and then proceeds to their cart from the same screen. Having it on the main dashboard as well created redundancy without UX benefit.

---
