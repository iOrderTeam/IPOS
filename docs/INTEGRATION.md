# IPOS-PU — Integration & Progress Overview

## What is this project?

IPOS-PU is one part of a three-part system called IPOS (InfoPharma Ordering System). It is a desktop app that lets members of the public browse pharmaceutical products, add them to a cart, and place orders online.

There are three separate apps in total, each built by a different team:

| System | Built by | What it does |
|---|---|---|
| **IPOS-SA** | Team A | Central InfoPharma server — holds the master product catalogue and global stock levels |
| **IPOS-CA** | Team B | Merchant's desktop app — manages the local pharmacy's stock and sales |
| **IPOS-PU** | Team C (us) | Public ordering portal — what customers use to browse and buy products online |

All three run as separate desktop applications and talk to each other over HTTP.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Desktop UI | JavaFX 21 (FXML screens) |
| Backend framework | Spring Boot 3.5 |
| Database | H2 (embedded, file-based — no install needed) |
| ORM | Spring Data JPA / Hibernate |
| REST API | Spring Web (RestController) |
| Build tool | Maven (via `mvnw` wrapper) |
| Version control | Git / GitHub |

---

## What We Have Built (Team C — IPOS-PU)

### Done
- **Member registration** — non-commercial (email only) and commercial (company details)
- **Login** — with forced password change on first login
- **Product catalogue screen** — browse all products, filter by keyword search
- **Shopping cart** — add products, view totals, remove items
- **Checkout** — enter card details (stored in DB, not real payment), place order
- **Order confirmation** — email record stored in database on every order
- **Order tracking screen** — members can view all their past orders and statuses
- **Loyalty discount** — non-commercial members get 10% off every 10th order automatically
- **REST API** — all features accessible via HTTP endpoints (for other teams to call)

### What Basit (P2) is building
- Admin approval screen for commercial member applications
- Promotion/advertising campaigns screen
- Integration mock with IPOS-SA

### What Manasar (P3) is building
- Sales report screen
- Campaign engagement report screen
- Integration mock with IPOS-CA (stock deduction)

---

## What We Need From Other Teams

### From Team A (IPOS-SA)

| What we need | Why | When |
|---|---|---|
| Their product catalogue data | Our catalogue screen is empty without it — IPOS-SA is the authoritative source for all pharmaceutical products and pricing | By demo day |
| Their API endpoint for commercial member applications | When a commercial member registers on IPOS-PU, we need to pass their details to IPOS-SA for approval checks | Week 5 (by 5 April) |
| Their IP address and port number | So we can point our HTTP calls at their running app | Week 5 |

**What we currently do instead (mock):**
- The products table in our database is empty — no products to browse yet
- Commercial member registration currently just saves locally; the call to IPOS-SA is a `System.out.println` placeholder in `IposSaService.java`

---

### From Team B (IPOS-CA)

| What we need | Why | When |
|---|---|---|
| Their stock deduction API endpoint | When a customer places an order on IPOS-PU, IPOS-CA needs to know so the merchant's local stock goes down | Week 5 (by 5 April) |
| Their IP address and port number | So we can point our HTTP calls at their running app | Week 5 |
| Stock availability per product | Ideally we show live stock levels from the merchant's system on our catalogue screen | Week 5 |

**What we currently do instead (mock):**
- After an order is placed, `OrderService.placeOrder()` prints `"IPOS-CA: Deducting stock for order X"` to the console instead of making a real HTTP call

---

## What Other Teams Need From Us

### Team A (IPOS-SA) needs:
- Our **email service endpoint** — `POST /api/email/send` — so they can send emails (e.g. approval notifications) through IPOS-PU's email system
- Our **`Member`** data structure so they know what fields to expect when we send them a commercial registration

### Team B (IPOS-CA) needs:
- Our **order data structure** — specifically the list of `{ productId, quantity }` items we send when an order is placed
- They also need `Order.java`, `OrderItem.java`, `OrderStatus.java` from our codebase to understand the data format

### Manasar (P3 — our own team) needs:
- `Order.java`, `OrderItem.java`, `OrderStatus.java`, `OrderService.java` — **these are now on the branch, Manasar can pull them**

---

## How the Wiring Will Work (Week 5)

When we meet the other teams (by 5 April), we swap the following:

1. **Their IP and port** — we update two service files:
   - `IposSaService.java` — replace `System.out.println` with a `RestTemplate.postForObject(...)` call to IPOS-SA's endpoint
   - `IposCaService.java` (Manasar's file) — replace `System.out.println` with a `RestTemplate.postForObject(...)` call to IPOS-CA's endpoint

2. **Our IP and port** — we tell them:
   - IPOS-PU runs on `http://[our machine IP]:8080`
   - Email endpoint: `POST http://[our IP]:8080/api/email/send`

3. **Database access option** — the brief also allows teams to share at the database table level (direct DB access) instead of HTTP calls. If the other teams prefer this, we can agree to point at the same H2 file or switch to a shared database. This is something to agree on at the Week 5 meeting.

---

## Key Dates Remaining

| Date | What must happen |
|---|---|
| 29 March | All UI screens working end-to-end (Seyer's deadline) |
| 5 April | Everything merged into `main`, app runs fully — meet with other teams to swap API details |
| 10 April | Full practice demo run with all three systems running together |
| **16 April** | **Demo Day** |
| **19 April 5pm** | **Final submission — code + implementation report** |
