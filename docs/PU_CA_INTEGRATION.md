# IPOS-CA Integration Changes Required
## PU ↔ CA Communication — Instructions for Team 2 (CA)

**Prepared by:** IPOS-PU Team 3  
**Date:** April 2026  
**PU port:** 8082 — ensure `server.port=8082` is set in PU's `application.properties`

---

## Overview

This document lists every change CA must make to complete the live PU ↔ CA integration.  
PU has already implemented all matching endpoints on its side.

**Part A** covers the changes that are ready to implement now — PU has the matching code in place.  
**Part B** covers pending work that requires CA to build new functionality before PU can wire up its side.

---

## Part A — Changes Ready to Implement Now

| # | What | Where |
|---|------|-------|
| 1 | Create `HttpPuAdapter.java` — live HTTP calls to PU | New file |
| 2 | Add two methods to `IPuStockUpdater` interface | `src/integration/IPuStockUpdater.java` |
| 3 | Add stub implementations to `MockPuAdapter` | `src/integration/MockPuAdapter.java` |
| 4 | Call notify methods from `StockService` on add/edit/delete | `src/service/StockService.java` |
| 5 | Wire `HttpPuAdapter` in `Main.java` for HTTP mode | `src/app/Main.java` |
| 6 | Extend `/stock` response to include `price` and `itemCode` | `src/integration/CaApiServer.java` |

---

## Change 1 — Create `HttpPuAdapter.java`

Create the file `src/integration/HttpPuAdapter.java` with the full contents below.  
This is the live implementation of `IPuStockUpdater`. It replaces `MockPuAdapter` when CA is started with `-Dipos.http=true`.

```java
package integration;

import model.CardDetails;
import model.OnlineSale;
import model.StockItem;
import service.OnlineSaleService;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class HttpPuAdapter implements IPuStockUpdater {

    private static final String PU_BASE = "http://localhost:8082";

    private final OnlineSaleService onlineSaleService;

    public HttpPuAdapter(OnlineSaleService onlineSaleService) {
        this.onlineSaleService = onlineSaleService;
    }

    @Override
    public boolean applyOnlineSale(OnlineSale sale) {
        return onlineSaleService.processOnlineSale(sale);
    }

    @Override
    public CardClearanceResult clearCardPayment(CardDetails card, double amount) {
        try {
            String body = "{"
                + "\"cardholderName\":\"\","
                + "\"firstFourDigits\":\"" + card.getFirstFourDigits() + "\","
                + "\"lastFourDigits\":\""  + card.getLastFourDigits()  + "\","
                + "\"expiryDate\":\""      + card.getExpiryDate()      + "\","
                + "\"amount\":"            + String.format("%.2f", amount)
                + "}";

            String response = post(PU_BASE + "/api/payments/ca-clearance", body);

            boolean approved = response.contains("\"approved\":true");
            String txRef = null;
            if (approved) {
                int start = response.indexOf("\"transactionRef\":\"");
                if (start >= 0) {
                    start += "\"transactionRef\":\"".length();
                    int end = response.indexOf("\"", start);
                    if (end > start) txRef = response.substring(start, end);
                }
            }
            String message = approved ? "Payment approved" : "Card declined by payment processor";
            System.out.println("[CA->PU] Card clearance: " + (approved ? "APPROVED" : "DECLINED")
                    + " £" + String.format("%.2f", amount));
            return new CardClearanceResult(approved, txRef, message);

        } catch (Exception e) {
            System.err.println("[CA->PU] /api/payments/ca-clearance failed (non-fatal): " + e.getMessage());
            if ("0000".equals(card.getFirstFourDigits())) {
                return new CardClearanceResult(false, null, "Card declined by payment processor");
            }
            return new CardClearanceResult(true, "PU-OFFLINE-FALLBACK", "Payment approved (PU offline)");
        }
    }

    @Override
    public void notifyProductDeleted(int caItemId) {
        try {
            delete(PU_BASE + "/api/products/ca/" + caItemId);
            System.out.println("[CA->PU] Deletion notified for caItemId=" + caItemId);
        } catch (Exception e) {
            System.err.println("[CA->PU] /api/products/ca delete failed (non-fatal): " + e.getMessage());
        }
    }

    @Override
    public void notifyStockUpdated(StockItem item) {
        try {
            String body = "{"
                + "\"caItemId\":"  + item.getItemId()  + ","
                + "\"name\":\""    + item.getName().replace("\"", "\\\"") + "\","
                + "\"price\":"     + String.format("%.2f", item.getPriceIncVat())
                + "}";
            post(PU_BASE + "/api/products/ca-sync", body);
            System.out.println("[CA->PU] Stock sync notified for caItemId=" + item.getItemId());
        } catch (Exception e) {
            System.err.println("[CA->PU] /api/products/ca-sync failed (non-fatal): " + e.getMessage());
        }
    }

    private String post(String urlStr, String jsonBody) throws Exception {
        HttpURLConnection conn = open(urlStr, "POST");
        byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
        }
        return readResponse(conn);
    }

    private void delete(String urlStr) throws Exception {
        HttpURLConnection conn = open(urlStr, "DELETE");
        conn.getResponseCode();
        conn.disconnect();
    }

    private HttpURLConnection open(String urlStr, String method) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(!method.equals("DELETE") && !method.equals("GET"));
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(5000);
        return conn;
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        int code = conn.getResponseCode();
        var stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        if (stream == null) return "";
        try (Scanner sc = new Scanner(stream, StandardCharsets.UTF_8)) {
            return sc.useDelimiter("\\A").hasNext() ? sc.next() : "";
        } finally {
            conn.disconnect();
        }
    }
}
```

---

## Change 2 — Add two methods to `IPuStockUpdater`

**File:** `src/integration/IPuStockUpdater.java`

Add `import model.StockItem;` at the top and add these two method signatures to the interface:

```java
// notify PU that a stock item has been deleted from CA
// PU removes the product from its catalogue so it no longer appears online
void notifyProductDeleted(int caItemId);

// notify PU that a stock item has been added or edited in CA
// PU upserts the product in its catalogue with the updated name and price
void notifyStockUpdated(StockItem item);
```

Full updated interface for reference:

```java
package integration;

import model.CardDetails;
import model.OnlineSale;
import model.StockItem;

public interface IPuStockUpdater {

    boolean applyOnlineSale(OnlineSale sale);

    CardClearanceResult clearCardPayment(CardDetails card, double amount);

    void notifyProductDeleted(int caItemId);

    void notifyStockUpdated(StockItem item);
}
```

---

## Change 3 — Add stubs to `MockPuAdapter`

**File:** `src/integration/MockPuAdapter.java`

Add `import model.StockItem;` at the top. Then add these two methods to the class (anywhere before `logOnlineSale`):

```java
@Override
public void notifyProductDeleted(int caItemId) {
    System.out.println("[MockPuAdapter] notifyProductDeleted caItemId=" + caItemId + " (no-op in mock mode)");
}

@Override
public void notifyStockUpdated(StockItem item) {
    System.out.println("[MockPuAdapter] notifyStockUpdated caItemId=" + item.getItemId() + " (no-op in mock mode)");
}
```

---

## Change 4 — Call notify methods from `StockService`

**File:** `src/service/StockService.java`

Add `import app.AppContext;` at the top.

Replace `addStockItem` and `removeStockItem` as follows:

```java
// add a brand new item to the catalogue and notify PU so it appears online
public void addStockItem(StockItem item) throws StockException {
    if (item == null) {
        throw new StockException(
            StockException.Reason.NULL_ITEM,
            "stock item cannot be null"
        );
    }
    stockRepository.save(item);
    if (AppContext.getPuAdapter() != null) {
        AppContext.getPuAdapter().notifyStockUpdated(item);
    }
}

// removes item from catalogue and notifies PU so it disappears from the online shop
public void removeStockItem(int itemId) throws StockException {
    validateItemId(itemId);
    getStockItem(itemId); // confirms item exists before deletion
    stockRepository.delete(itemId);
    if (AppContext.getPuAdapter() != null) {
        AppContext.getPuAdapter().notifyProductDeleted(itemId);
    }
}
```

**Note:** You also need to call `notifyStockUpdated` from your edit/update method if one exists in your `StockService` or `StockManagementUI`. The same pattern applies: after `stockRepository.update(...)`, call `AppContext.getPuAdapter().notifyStockUpdated(updatedItem)`.

---

## Change 5 — Wire `HttpPuAdapter` in `Main.java`

**File:** `src/app/Main.java`

Add the import:
```java
import integration.HttpPuAdapter;
```

Then replace the service wiring block so that `HttpPuAdapter` is used in HTTP mode and `MockPuAdapter` in mock mode:

```java
// Before:
StockService      stockSvc   = new StockService(new StockRepositoryImpl());
OnlineSaleService onlineSvc  = new OnlineSaleService(stockSvc);
IPuStockUpdater   puAdapter  = new MockPuAdapter(onlineSvc);

ISaGateway gateway;
if (httpMode) {
    HttpSaGateway httpGateway = new HttpSaGateway();
    httpGateway.login();
    gateway = httpGateway;
    ...
} else {
    gateway = new MockSaGateway();
    ...
}

// After:
StockService      stockSvc   = new StockService(new StockRepositoryImpl());
OnlineSaleService onlineSvc  = new OnlineSaleService(stockSvc);

ISaGateway gateway;
IPuStockUpdater puAdapter;
if (httpMode) {
    HttpSaGateway httpGateway = new HttpSaGateway();
    httpGateway.login();
    gateway   = httpGateway;
    puAdapter = new HttpPuAdapter(onlineSvc);
    System.out.println("[Main] HTTP mode — connected to SA on port 8080, PU on port 8082");
} else {
    gateway   = new MockSaGateway();
    puAdapter = new MockPuAdapter(onlineSvc);
    System.out.println("[Main] Mock mode — SA and PU are simulated locally");
}
```

---

## Change 6 — Extend `/stock` response with `price` and `itemCode`

**File:** `src/integration/CaApiServer.java`

In the `handleGetStock` method, extend the JSON built for each item to include `price` and `itemCode`:

```java
// Before:
sb.append("{\"id\":").append(item.getItemId())
  .append(",\"name\":\"").append(item.getName().replace("\"", "\\\"")).append("\"")
  .append(",\"quantity\":").append(item.getQuantity())
  .append("}");

// After:
sb.append("{\"id\":").append(item.getItemId())
  .append(",\"name\":\"").append(item.getName().replace("\"", "\\\"")).append("\"")
  .append(",\"quantity\":").append(item.getQuantity())
  .append(",\"price\":").append(String.format("%.2f", item.getPriceIncVat()))
  .append(",\"itemCode\":\"").append(item.getItemCode().replace("\"", "\\\"")).append("\"")
  .append("}");
```

This lets PU sync live prices from CA on every catalogue load, rather than relying on its own hardcoded seed prices.

---

## PU Endpoints CA Can Now Call

Once PU is running on port 8082, these endpoints are live:

| Endpoint | Method | Called when | Body |
|----------|--------|-------------|------|
| `/api/payments/ca-clearance` | POST | Customer pays by card at pharmacy | `{ "firstFourDigits", "lastFourDigits", "expiryDate", "amount" }` |
| `/api/products/ca/{caItemId}` | DELETE | Stock item deleted in CA | — |
| `/api/products/ca-sync` | POST | Stock item added or edited in CA | `{ "caItemId", "name", "price" }` |

All three are non-fatal — if PU is offline the CA operation completes locally and PU drifts until the next restart.

---

## What PU Already Handles (No CA Action Needed)

| Endpoint on CA | Called by | Status |
|----------------|-----------|--------|
| `POST /online-sale` | PU after checkout | Already working |
| `GET /stock` | PU on catalogue load | Already working (now extended with `price`) |
| `POST /order-update` | SA on order status change | Already working |

---

## Verify the Integration

Once both systems are running with `-Dipos.http=true` / `server.port=8082`:

```bash
# 1. Test card payment clearance (CA -> PU)
curl -X POST http://localhost:8082/api/payments/ca-clearance \
  -H "Content-Type: application/json" \
  -d '{"cardholderName":"Test","firstFourDigits":"1234","lastFourDigits":"5678","expiryDate":"12/28","amount":25.00}'
# Expected: {"approved":true,"transactionRef":"PU-TX-XXXXXXXX","message":"Payment approved"}

# 2. Test product deletion sync (CA -> PU)
curl -X DELETE http://localhost:8082/api/products/ca/14
# Expected: {"ok":true}

# 3. Test stock add/edit sync (CA -> PU)
curl -X POST http://localhost:8082/api/products/ca-sync \
  -H "Content-Type: application/json" \
  -d '{"caItemId":14,"name":"Vitamin B12","price":2.60}'
# Expected: {"ok":true}

# 4. Test stock fetch (PU -> CA — verifies price field is present)
curl http://localhost:8081/stock
# Expected: array with id, name, quantity, price, itemCode fields per item
```

---

## Startup Order (Reminder)

1. MySQL
2. SA: `mvn spring-boot:run` (port 8080)
3. PU: `mvn spring-boot:run` (port 8082)
4. CA: `java -Dipos.http=true -cp "out:lib/*" app.Main`

---

---

## Part B — Pending Work (CA Must Build First)

The following features are required by the spec but are **not yet implemented in CA**. PU has the matching endpoints ready and waiting. Once CA builds its side, the wiring is straightforward.

---

### B1 — Online Order Lifecycle Management

**Spec requirement (IPOS-CA-Sales):**
> *"Maintain orders received via the PU portal — order status will be changed from 'accepted' to 'ready for shipment', then to 'shipped' and finally 'delivered'."*

**Current state:**  
When PU sends `POST /online-sale`, CA deducts stock and logs the event to the `online_sales` table. That is where it ends. The `online_sales` table has no `status` column, no `delivery_address` column, and there is no screen in CA to view or manage these orders. The delivery address is present in PU's payload but CA ignores it entirely.

**What CA needs to build:**

#### B1a — Extend the `online_sales` database table

Add two columns. Wrap each in try/catch (same pattern already used elsewhere in `DatabaseManager.java` for safe migrations):

```sql
ALTER TABLE online_sales ADD COLUMN delivery_address TEXT NOT NULL DEFAULT '';
ALTER TABLE online_sales ADD COLUMN status TEXT NOT NULL DEFAULT 'RECEIVED';
```

Status values must be: `RECEIVED`, `READY_FOR_SHIPMENT`, `DISPATCHED`, `DELIVERED`

#### B1b — Extend the `OnlineSale` model

**File:** `src/model/OnlineSale.java`

Add a `deliveryAddress` field:

```java
private final String deliveryAddress;

// update constructor to accept it:
public OnlineSale(String puOrderId, LocalDate receivedDate,
                  String customerEmail, String deliveryAddress,
                  List<OnlineSaleItem> items) {
    ...
    this.deliveryAddress = deliveryAddress != null ? deliveryAddress : "";
}

public String getDeliveryAddress() { return deliveryAddress; }
```

#### B1c — Parse `deliveryAddress` in `CaApiServer`

**File:** `src/integration/CaApiServer.java`, method `handleOnlineSale`

The field is already sent by PU in every `/online-sale` payload. CA just needs to read it:

```java
// PU sends this — already in the payload, just not being read:
String deliveryAddress = json.has("deliveryAddress")
    ? json.get("deliveryAddress").getAsString() : "";
```

Pass it into the `OnlineSale` constructor and store it when logging to the database.

#### B1d — Extend `MockPuAdapter.logOnlineSale` (and wherever online sales are persisted)

**File:** `src/integration/MockPuAdapter.java`

Update the INSERT to include the new columns:

```java
String saleSql = """
    INSERT INTO online_sales (pu_order_id, received_date, customer_email, fully_applied, delivery_address, status)
    VALUES (?, ?, ?, ?, ?, 'RECEIVED')
""";
// add stmt.setString(5, sale.getDeliveryAddress()); before executeUpdate()
```

#### B1e — Build an Online Orders screen in CA

CA needs a UI screen (visible to the pharmacist and manager roles) that:

- Lists all rows from `online_sales` with columns: PU Order ID, Date, Customer Email, Delivery Address, Status
- Allows the user to progress the status through: `RECEIVED` → `READY_FOR_SHIPMENT` → `DISPATCHED` → `DELIVERED`
- On each status change, calls back to PU so the member can track their order (see B1f below)

This screen follows the same pattern as `WholesaleOrderUI.java` — a table with a status update button.

#### B1f — Push status updates back to PU

When CA progresses an online order's status, PU needs to know so the member's "My Orders" screen reflects it.

**PU endpoint (already implemented and live):**

```
POST http://localhost:8082/api/orders/{puOrderId}/status
Content-Type: application/json
Body: { "status": "DISPATCHED" }
```

`puOrderId` is the numeric part of the `pu_order_id` stored in `online_sales` (e.g. `"PU-42"` → path param `42`).

**PU status values CA must use:**

| CA status | PU status to send |
|-----------|------------------|
| `RECEIVED` | `RECEIVED` |
| `READY_FOR_SHIPMENT` | `RECEIVED` (no direct PU equivalent — leave as RECEIVED until dispatched) |
| `DISPATCHED` | `DISPATCHED` |
| `DELIVERED` | `DELIVERED` |

The call should be non-fatal — if PU is offline the CA status update still saves locally.

**Example implementation in `HttpPuAdapter`** (add this method):

```java
public void notifyOrderStatusUpdate(String puOrderId, String caStatus) {
    try {
        // extract numeric id from "PU-42" -> "42"
        String numericId = puOrderId.startsWith("PU-") ? puOrderId.substring(3) : puOrderId;

        String puStatus = switch (caStatus) {
            case "DISPATCHED" -> "DISPATCHED";
            case "DELIVERED"  -> "DELIVERED";
            default           -> "RECEIVED";
        };

        String body = "{\"status\":\"" + puStatus + "\"}";
        post(PU_BASE + "/api/orders/" + numericId + "/status", body);
        System.out.println("[CA->PU] Order status update: " + puOrderId + " -> " + puStatus);
    } catch (Exception e) {
        System.err.println("[CA->PU] order status update failed (non-fatal): " + e.getMessage());
    }
}
```

Also add `notifyOrderStatusUpdate` to `IPuStockUpdater` and a no-op stub in `MockPuAdapter`.

---

### B2 — Summary of Data Flow Once B1 is Complete

```
PU customer places order
    ↓
PU: POST /online-sale → CA (stock deducted, order saved as RECEIVED with delivery address)
    ↓
CA pharmacist opens Online Orders screen
    ↓
CA pharmacist marks order READY_FOR_SHIPMENT (internal only)
    ↓
CA pharmacist marks order DISPATCHED
    → CA: POST http://localhost:8082/api/orders/{id}/status  { "status": "DISPATCHED" }
    → PU member's "My Orders" screen now shows DISPATCHED
    ↓
CA pharmacist marks order DELIVERED
    → CA: POST http://localhost:8082/api/orders/{id}/status  { "status": "DELIVERED" }
    → PU member's "My Orders" screen now shows DELIVERED
```

---

### B3 — Files CA needs to create or modify for Part B

| File | Change |
|------|--------|
| `src/db/DatabaseManager.java` | Add `delivery_address` and `status` columns to `online_sales` via migration |
| `src/model/OnlineSale.java` | Add `deliveryAddress` field and update constructor |
| `src/integration/CaApiServer.java` | Parse `deliveryAddress` from `/online-sale` payload |
| `src/integration/MockPuAdapter.java` | Update `logOnlineSale` INSERT; add `notifyOrderStatusUpdate` stub |
| `src/integration/IPuStockUpdater.java` | Add `notifyOrderStatusUpdate(String puOrderId, String status)` |
| `src/integration/HttpPuAdapter.java` | Implement `notifyOrderStatusUpdate` with HTTP POST to PU |
| `src/service/OnlineSaleService.java` | Store `deliveryAddress` when persisting sale |
| `src/ui/OnlineOrdersUI.java` | **New screen** — list and manage incoming PU orders with status progression |

---

### B4 — Verify once B1 is complete

```bash
# Simulate PU sending an online sale with a delivery address
curl -X POST http://localhost:8081/online-sale \
  -H "Content-Type: application/json" \
  -d '{
    "puOrderId": "PU-99",
    "receivedDate": "2026-04-15",
    "customerEmail": "test@example.com",
    "deliveryAddress": "25 High Street, London BR7 5BN",
    "items": [{"itemId": 1, "quantity": 2}]
  }'
# Expected: {"accepted":true,"fullyApplied":true}
# CA online_sales row should have delivery_address and status='RECEIVED'

# Simulate CA pushing a status update back to PU
curl -X POST http://localhost:8082/api/orders/99/status \
  -H "Content-Type: application/json" \
  -d '{"status": "DISPATCHED"}'
# Expected: the PU order with id=99 now has status DISPATCHED
# Member's "My Orders" screen reflects the change
```
