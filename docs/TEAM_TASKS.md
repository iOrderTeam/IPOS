# IPOS-PU Team Task Brief
**Project:** InfoPharma Public Ordering System — Public Utilities (IPOS-PU)
**Demo Day:** 16 April 2026
**Final Submission:** 19 April 2026, 5pm
**Today:** 4 March 2026 — ~6 weeks remaining

---

## Hard Deadlines

| Date | Deadline |
|---|---|
| 15 March | Every person has their branch created and first model class committed |
| 22 March | All models + services done — REST API working and testable |
| 29 March | All UI screens working end-to-end |
| 5 April | Everything merged into `main`, app runs fully |
| 10 April | Full practice demo run as a team |
| **16 April** | **DEMO DAY — whole day, all members present** |
| **19 April 5pm** | **FINAL SUBMISSION — code + implementation report** |

---

## What the Demo Must Show (60% of your grade)

The markers will check:
- All functional requirements working
- The app runs off real data (they provide test scenarios)
- The three subsystems (IPOS-PU, IPOS-SA, IPOS-CA) interact — even if mocked
- GUI is consistent and usable
- Source code submitted — **no source code = zero marks for this section**

---

## Git Branch Rules

- **Never commit directly to `main`**
- Each person works on their own branch (named below)
- Push your branch every Friday regardless of how much is done
- Only merge to `main` when your feature compiles and works end-to-end

---

---

# Programmer 1 (You) — Catalogue, Cart & Orders
**Branch:** `feature/catalogue-cart-orders`

You own the core shopping experience. Everything else depends on this working.

---

## Week 1–2 — by 15 March: Product Catalogue

### Files to create:

**`src/main/java/com/ipos/pu/model/Product.java`**
- Fields: `id` (Long), `name` (String), `brand` (String), `description` (String), `price` (double), `stockQuantity` (int)
- Annotate with `@Entity`, `@Table(name = "products")`
- Generate all getters and setters (same style as `Member.java`)

**`src/main/java/com/ipos/pu/repository/ProductRepository.java`**
- Extends `JpaRepository<Product, Long>`
- Add one method: `List<Product> findByNameContainingIgnoreCase(String keyword);` (for search later)

**`src/main/java/com/ipos/pu/service/CatalogueService.java`**
- Annotate with `@Service`
- Inject `ProductRepository` via constructor
- Methods:
  - `List<Product> getAllProducts()` — returns all products
  - `Optional<Product> getProductById(Long id)` — returns one product

**`src/main/java/com/ipos/pu/controller/CatalogueController.java`**
- Annotate with `@RestController`, `@RequestMapping("/api/products")`
- `GET /api/products` → calls `getAllProducts()`
- `GET /api/products/{id}` → calls `getProductById(id)`

**`src/main/resources/com/ipos/pu/ui/catalogue.fxml`**
- A `BorderPane` with a `TableView` in the center showing product name, brand, price, stock
- Buttons: "Add to Cart", "Back"
- Controller: `com.ipos.pu.ui.controller.CatalogueController`

**`src/main/java/com/ipos/pu/ui/controller/CatalogueController.java`**
- Annotate with `@Component`
- Inject `CatalogueService` via constructor
- In `initialize()`: load all products into the TableView
- `onAddToCartClicked()`: get selected product, call `CartService.addToCart()`
- `onBackClicked()`: `SceneManager.switchTo("/com/ipos/pu/ui/main.fxml")`

---

## Week 2–3 — by 22 March: Shopping Cart

### Files to create:

**`src/main/java/com/ipos/pu/model/CartItem.java`**
- Fields: `id` (Long), `member` (@ManyToOne Member), `product` (@ManyToOne Product), `quantity` (int)
- Annotate with `@Entity`, `@Table(name = "cart_items")`

**`src/main/java/com/ipos/pu/repository/CartItemRepository.java`**
- Extends `JpaRepository<CartItem, Long>`
- Add: `List<CartItem> findByMember(Member member);`
- Add: `void deleteByMember(Member member);`

**`src/main/java/com/ipos/pu/service/CartService.java`**
- Annotate with `@Service`
- Inject `CartItemRepository`, `ProductRepository`, `MemberRepository` via constructor
- Methods:
  - `void addToCart(Long memberId, Long productId, int quantity)` — find member + product, save CartItem
  - `List<CartItem> getCart(Long memberId)` — return all CartItems for that member
  - `void clearCart(Long memberId)` — delete all CartItems for that member
  - `double getCartTotal(Long memberId)` — sum up `quantity * product.getPrice()` for all items

**`src/main/java/com/ipos/pu/controller/CartController.java`**
- `POST /api/cart/add` — takes memberId, productId, quantity
- `GET /api/cart/{memberId}` — returns cart items
- `DELETE /api/cart/{memberId}` — clears cart

**`src/main/resources/com/ipos/pu/ui/cart.fxml`**
- TableView showing product name, quantity, price per item, line total
- Label showing cart total at the bottom
- Buttons: "Place Order", "Back"

**`src/main/java/com/ipos/pu/ui/controller/CartController.java`** *(in `ui/controller` package)*
- Load cart items from `CartService` using `SessionManager.getCurrentMember().getId()`
- Show total
- `onPlaceOrderClicked()`: switch to `checkout.fxml`

---

## Week 3–4 — by 29 March: Orders & Payment

> **Coordinate with Programmer 3** — they are also creating Order-related files. Agree who creates `Order.java`, `OrderItem.java`, `OrderStatus.java`. Easiest: you create them, they use them.

### Files to create:

**`src/main/java/com/ipos/pu/model/OrderStatus.java`**
```java
public enum OrderStatus { RECEIVED, DISPATCHED, DELIVERED }
```

**`src/main/java/com/ipos/pu/model/Order.java`**
- Fields: `id` (Long), `member` (@ManyToOne), `status` (@Enumerated OrderStatus), `placedAt` (LocalDateTime), `totalAmount` (double), `paymentReference` (String)
- Annotate with `@Entity`, `@Table(name = "orders")`

**`src/main/java/com/ipos/pu/model/OrderItem.java`**
- Fields: `id` (Long), `order` (@ManyToOne), `product` (@ManyToOne), `quantity` (int), `priceAtTimeOfOrder` (double)
- Annotate with `@Entity`, `@Table(name = "order_items")`

**`src/main/java/com/ipos/pu/repository/OrderRepository.java`**
- `List<Order> findByMember(Member member);`
- `List<Order> findByPlacedAtBetween(LocalDateTime from, LocalDateTime to);`

**`src/main/java/com/ipos/pu/repository/OrderItemRepository.java`**
- `List<OrderItem> findByOrder(Order order);`

**`src/main/java/com/ipos/pu/service/OrderService.java`**
- Methods:
  - `Order placeOrder(Long memberId, String paymentReference)` — creates Order from cart, saves OrderItems, clears cart, sends confirmation email, **calls IPOS-CA mock** (see Integration section)
  - `List<Order> getOrdersForMember(Long memberId)`
  - `Order getOrder(Long orderId)`
  - `List<Order> getOrdersBetween(LocalDateTime from, LocalDateTime to)`
  - `Order updateStatus(Long orderId, OrderStatus newStatus)`

**`src/main/java/com/ipos/pu/controller/OrderController.java`**
- `POST /api/orders/place` — takes memberId, paymentReference
- `GET /api/orders/member/{memberId}`
- `GET /api/orders/{orderId}`
- `POST /api/orders/{orderId}/status` — takes status param

**`src/main/resources/com/ipos/pu/ui/checkout.fxml`**
- Fields: cardholder name, card number, expiry, CVV (display only — not real payment)
- Show order total
- Button: "Confirm & Pay"

**`src/main/java/com/ipos/pu/ui/controller/CheckoutController.java`**
- On confirm: generate a fake `paymentReference` (e.g. `UUID.randomUUID().toString()`)
- Call `orderService.placeOrder(memberId, paymentReference)`
- Show success message, switch to `track-orders.fxml`

---

## Week 4–5 — by 5 April: Dashboard & Integration Polish

### Update `main.fxml` and `MainController.java`

After login, the main screen should show buttons based on who is logged in:
- If logged in as customer: "View Catalogue", "My Cart", "Track My Orders", "View Promotions", "Logout"
- If logged in as admin: "Manage Campaigns", "Pending Applications", "Sales Report", "Logout"

Add to `MainController.java`:
- Check `SessionManager.isLoggedIn()` and `SessionManager.getCurrentMember().getMemberType()`
- Show/hide buttons accordingly

---

---

# Programmer 2 — Admin & Campaigns
**Branch:** `feature/admin-and-campaigns`

You own the admin approval flow and the promotional campaign system.

---

## Week 1–2 — by 15 March: Campaign Models

### Files to create:

**`src/main/java/com/ipos/pu/model/Campaign.java`**
- Fields: `id` (Long), `name` (String), `description` (String), `discountPercentage` (double), `startDate` (LocalDate), `endDate` (LocalDate), `hits` (int)
- Annotate with `@Entity`, `@Table(name = "campaigns")`
- Generate all getters and setters

**`src/main/java/com/ipos/pu/model/CampaignProduct.java`**
- Fields: `id` (Long), `campaign` (@ManyToOne Campaign), `product` (@ManyToOne Product)
- Annotate with `@Entity`, `@Table(name = "campaign_products")`
- Note: `Product` is created by Programmer 1 — wait for their branch or create a placeholder

**`src/main/java/com/ipos/pu/repository/CampaignRepository.java`**
- Extends `JpaRepository<Campaign, Long>`
- Add: `List<Campaign> findByStartDateBeforeAndEndDateAfter(LocalDate now1, LocalDate now2);`

**`src/main/java/com/ipos/pu/repository/CampaignProductRepository.java`**
- Extends `JpaRepository<CampaignProduct, Long>`
- Add: `List<CampaignProduct> findByCampaign(Campaign campaign);`

---

## Week 2–3 — by 22 March: Admin Service & API

### Files to create:

**`src/main/java/com/ipos/pu/service/AdminService.java`**
- Inject: `MemberRepository`, `CampaignRepository`, `EmailService` via constructor
- Methods:
  - `List<Member> getPendingApplications()` — filter members where status == PENDING
  - `void approveMember(Long memberId, String temporaryPassword)` — set status to ACTIVE, set passwordChangeRequired to true, encode and set password, send approval email
  - `void rejectMember(Long memberId)` — set status to INACTIVE, send rejection email
  - `Campaign createCampaign(String name, String description, double discountPercentage, LocalDate startDate, LocalDate endDate)` — validate startDate is before endDate, save, return
  - `void deleteCampaign(Long campaignId)` — delete by ID
  - `List<Campaign> getActiveCampaigns()` — use the repository query with `LocalDate.now()` for both params
  - `void incrementCampaignHits(Long campaignId)` — find campaign, hits++, save (UC12)

**`src/main/java/com/ipos/pu/controller/AdminController.java`** *(REST — in `controller` package)*
- `GET /api/admin/pending` → `getPendingApplications()`
- `POST /api/admin/approve/{memberId}` → `approveMember()` with `temporaryPassword` as request param
- `POST /api/admin/reject/{memberId}` → `rejectMember()`
- `POST /api/admin/campaigns` → `createCampaign()` — take JSON body with name, description, discountPercentage, startDate, endDate
- `DELETE /api/admin/campaigns/{id}` → `deleteCampaign()`
- `GET /api/admin/campaigns/active` → `getActiveCampaigns()`
- `POST /api/admin/campaigns/{id}/hit` → `incrementCampaignHits()`

---

## Week 3–4 — by 29 March: Admin & Campaign UI Screens

**`src/main/resources/com/ipos/pu/ui/admin.fxml`**
- `BorderPane` layout
- Top: Label "Admin Panel"
- Center: `VBox` with:
  - Label "Pending Applications"
  - `ListView fx:id="pendingList"` (height ~200)
  - `HBox` with buttons: "Approve Selected", "Reject Selected"
- Controller: `com.ipos.pu.ui.controller.AdminController`

**`src/main/java/com/ipos/pu/ui/controller/AdminController.java`** *(UI — in `ui/controller` package)*
- Annotate with `@Component`
- Inject `AdminService` via constructor
- `initialize()`: call `loadPending()`
- `loadPending()`: get pending list, display as `"ID - email - companyRegNumber"` in the ListView
- `onApproveClicked()`: get selected item, parse ID, call `adminService.approveMember(id, "Temp1234!")`, reload list
- `onRejectClicked()`: parse ID, call `adminService.rejectMember(id)`, reload list
- `onBackClicked()`: `SceneManager.switchTo("/com/ipos/pu/ui/main.fxml")`

**`src/main/resources/com/ipos/pu/ui/campaigns.fxml`**
- Top: Label "Manage Campaigns"
- Center: `VBox` with:
  - `TableView fx:id="campaignsTable"` with columns: Name, Discount%, Start Date, End Date, Hits
  - `HBox` form fields: TextField for name, description, discountPercentage, startDate (yyyy-MM-dd), endDate (yyyy-MM-dd)
  - Buttons: "Create Campaign", "Delete Selected", "Back"
- Controller: `com.ipos.pu.ui.controller.CampaignController`

**`src/main/java/com/ipos/pu/ui/controller/CampaignController.java`**
- Annotate with `@Component`
- Inject `AdminService` via constructor
- `initialize()`: load active campaigns into the table
- `onCreateClicked()`: read form fields, call `adminService.createCampaign(...)`, reload table
- `onDeleteClicked()`: get selected campaign ID, call `adminService.deleteCampaign(id)`, reload table

---

## Week 4–5 — by 5 April: IPOS-SA Integration Mock

For the demo, IPOS-SA is likely built by another team. You need to simulate the connection.

**`src/main/java/com/ipos/pu/service/IposSaService.java`**
- Annotate with `@Service`
- Method: `void submitCommercialApplication(Member member)`
  - For now: just log the data with `System.out.println("Submitting to IPOS-SA: " + member.getEmail())`
  - Later (Week 5): replace with a real HTTP call using `RestTemplate` to the IPOS-SA team's endpoint
- This method should be called from `MemberService.registerCommercial()` — add it there

---

---

# Programmer 3 — Order Tracking & Reports
**Branch:** `feature/orders-and-reports`

You own order status tracking and all reporting screens.

> **Important:** Programmer 1 is creating `Order.java`, `OrderItem.java`, `OrderStatus.java`, and `OrderService.java`. Do NOT duplicate these. Your job is to build the UI screens and the report logic on top of them.

---

## Week 1–2 — by 15 March: Confirm Models with Programmer 1

Talk to Programmer 1 and confirm they are creating:
- `model/Order.java`
- `model/OrderItem.java`
- `model/OrderStatus.java`
- `repository/OrderRepository.java`
- `repository/OrderItemRepository.java`
- `service/OrderService.java`

If they haven't started yet, create these yourself using the definitions in the Programmer 1 section above and tell them.

---

## Week 2–3 — by 22 March: Report Service & API

### Files to create:

**`src/main/java/com/ipos/pu/service/ReportService.java`**
- Annotate with `@Service`
- Inject `OrderRepository`, `CampaignRepository` via constructor
- Methods:
  - `Map<String, Object> generateSalesReport(LocalDateTime from, LocalDateTime to)`:
    - Get all orders between dates
    - Calculate total revenue (sum of `order.getTotalAmount()`)
    - Return a Map with keys: `"orders"` (the list), `"totalRevenue"` (the double), `"orderCount"` (int)
  - `Map<String, Object> generateCampaignReport(Long campaignId)`:
    - Get campaign by ID
    - Return campaign details: name, hits, discountPercentage, startDate, endDate
  - `Map<String, Object> generateEngagementReport()`:
    - Get all campaigns
    - For each campaign: return hits count and a conversion rate (for now just return hits — purchases link comes later)

**`src/main/java/com/ipos/pu/controller/ReportController.java`**
- `GET /api/reports/sales?from=...&to=...` → `generateSalesReport()`
- `GET /api/reports/campaign/{id}` → `generateCampaignReport(id)`
- `GET /api/reports/engagement` → `generateEngagementReport()`

---

## Week 3–4 — by 29 March: Tracking & Report UI Screens

**`src/main/resources/com/ipos/pu/ui/track-orders.fxml`**
- Top: Label "My Orders"
- Center: `TableView fx:id="ordersTable"` with columns: Order ID, Date, Total, Status
- Bottom: Button "Back"
- Controller: `com.ipos.pu.ui.controller.TrackOrdersController`

**`src/main/java/com/ipos/pu/ui/controller/TrackOrdersController.java`**
- Annotate with `@Component`
- Inject `OrderService` via constructor
- `initialize()`:
  - Set up TableColumn cell value factories (see Programmer 1 brief for example)
  - Get `memberId` from `SessionManager.getCurrentMember().getId()`
  - Load orders into the table
- `onBackClicked()`: `SceneManager.switchTo("/com/ipos/pu/ui/main.fxml")`

**`src/main/resources/com/ipos/pu/ui/sales-report.fxml`**
- Top: Label "Sales Report"
- Center: `VBox` with:
  - `HBox` with: Label "From:", `DatePicker fx:id="fromDate"`, Label "To:", `DatePicker fx:id="toDate"`, Button "Generate"
  - `TableView fx:id="reportTable"` with columns: Order ID, Date, Member Email, Amount
  - `Label fx:id="totalLabel"` showing "Total Revenue: £0.00"
- Bottom: Button "Back"
- Controller: `com.ipos.pu.ui.controller.SalesReportController`

**`src/main/java/com/ipos/pu/ui/controller/SalesReportController.java`**
- Annotate with `@Component`
- Inject `ReportService` via constructor
- `onGenerateClicked()`:
  - Read `fromDate` and `toDate` as `LocalDate`, convert to `LocalDateTime` (use `.atStartOfDay()`)
  - Call `reportService.generateSalesReport(from, to)`
  - Populate table and update `totalLabel`
- `onBackClicked()`: `SceneManager.switchTo("/com/ipos/pu/ui/main.fxml")`

**`src/main/resources/com/ipos/pu/ui/engagement-report.fxml`**
- Shows all campaigns with their hits count and a calculated conversion rate
- Same pattern as sales-report.fxml

**`src/main/java/com/ipos/pu/ui/controller/EngagementReportController.java`**
- Load all campaigns, display hits vs conversions

---

## Week 4–5 — by 5 April: IPOS-CA Integration Mock

For the demo, IPOS-CA is likely built by another team. You need to simulate the stock deduction.

**`src/main/java/com/ipos/pu/service/IposCaService.java`**
- Annotate with `@Service`
- Method: `void deductStock(Long productId, int quantity)`
  - For now: just log `System.out.println("Deducting " + quantity + " of product " + productId + " from IPOS-CA")`
  - Later (Week 5): replace with a real HTTP call using `RestTemplate` to the IPOS-CA team's endpoint
- This method should be called from `OrderService.placeOrder()` after the order is saved

---

---

# IPOS-SA and IPOS-CA Integration — When and How

These are subsystems built by **other teams** in your group. Here is how to handle them:

## The Plan

| Week | What to do |
|---|---|
| Now–Week 4 | Build mocks (just `System.out.println` or return fake data) |
| Week 5 (5 April) | Meet with the IPOS-SA team and IPOS-CA team, share your API contracts |
| Week 5–6 (5–10 April) | Replace mocks with real HTTP calls to their running apps |
| 10 April | Integration test — run all three subsystems together |

## What IPOS-PU sends TO other systems

| To | When | Data |
|---|---|---|
| IPOS-SA | Commercial member registers (UC3) | email, companyRegNumber, directorDetails, businessType, address |
| IPOS-CA | Order is placed (UC16) | orderId, list of {productId, quantity}, totalCost |

## What IPOS-PU receives FROM other systems

| From | When | Data |
|---|---|---|
| IPOS-SA | Admin approves/rejects a commercial member | approval status |
| IPOS-CA | Customer views catalogue (UC8) | stock levels per product |

## How to make the HTTP calls (when ready)

In `IposSaService.java` and `IposCaService.java`, replace the print statements with:

```java
// Add RestTemplate to your Spring config or inject it
RestTemplate restTemplate = new RestTemplate();

// Example: send commercial application to IPOS-SA
restTemplate.postForObject(
    "http://[IPOS-SA-IP]:8081/api/applications",
    applicationData,
    String.class
);

// Example: deduct stock from IPOS-CA
restTemplate.postForObject(
    "http://[IPOS-CA-IP]:8082/api/stock/deduct",
    stockUpdateData,
    String.class
);
```

Ask the other teams what their IP/port and endpoint paths are when you meet them in Week 5.

## What IPOS-PU provides TO other systems (UC24)

The email service is already built (`EmailService.java`). The other teams can call your endpoint:
- `POST /api/email/send` — body: `{ "to": "...", "subject": "...", "body": "..." }`

Programmer 2 should add this endpoint:

**`src/main/java/com/ipos/pu/controller/EmailApiController.java`**
```java
@RestController
@RequestMapping("/api/email")
public class EmailApiController {
    private final EmailService emailService;
    public EmailApiController(EmailService emailService) { this.emailService = emailService; }

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody Map<String, String> body) {
        emailService.sendEmail(body.get("to"), body.get("subject"), body.get("body"));
        return ResponseEntity.ok("Email sent.");
    }
}
```

---

# Implementation Report Checklist (due 19 April)

You need to submit this alongside your code. Start collecting evidence now.

- [ ] Screenshot of IntelliJ showing the project compiles with no errors
- [ ] Instructions for how to run the app (which class to run, what Java version, etc.)
- [ ] List all database tables created (members, products, orders, order_items, cart_items, campaigns, campaign_products)
- [ ] UML Component Diagram showing IPOS-PU components and connections to IPOS-SA and IPOS-CA
- [ ] UML Deployment Diagram showing which machine runs what
- [ ] jUnit test results — at least one test class per feature area
- [ ] Design Class Diagram (reverse engineered from your code — IntelliJ can generate this)

---

# Summary — Who Owns What

| Feature | Owner | Key Files |
|---|---|---|
| Product catalogue | P1 | `Product`, `CatalogueService`, `catalogue.fxml` |
| Shopping cart | P1 | `CartItem`, `CartService`, `cart.fxml` |
| Place order + payment | P1 | `Order`, `OrderItem`, `OrderService`, `checkout.fxml` |
| Admin approvals | P2 | `AdminService`, `admin.fxml` |
| Campaigns | P2 | `Campaign`, `CampaignProduct`, `campaigns.fxml` |
| IPOS-SA mock/integration | P2 | `IposSaService` |
| Order tracking screen | P3 | `TrackOrdersController`, `track-orders.fxml` |
| Sales report | P3 | `ReportService`, `sales-report.fxml` |
| Engagement report | P3 | `EngagementReportController`, `engagement-report.fxml` |
| IPOS-CA mock/integration | P3 | `IposCaService` |
| Main dashboard screen | P1 | `main.fxml`, `MainController` |

**Do NOT edit each other's files without asking first.**
