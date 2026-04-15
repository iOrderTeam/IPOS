package com.ipos.pu.service;

import com.ipos.pu.model.Order;
import com.ipos.pu.model.OrderItem;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class IposCaService {

    private static final String CA_BASE = "http://localhost:8081";

    // POST /online-sale — called after a successful order is placed on PU
    // sends the full sale to CA so it can deduct stock from its local inventory
    // delivery address is included so CA can dispatch the goods
    // non-fatal: if CA is offline the order still completes on PU
    public void passSaleToCa(Order order, List<OrderItem> items) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("puOrderId",       "PU-" + order.getId());
            payload.put("receivedDate",    LocalDate.now().toString());
            payload.put("customerEmail",   order.getMember().getEmail());
            payload.put("deliveryAddress", order.getDeliveryAddress());

            List<Map<String, Object>> saleItems = new ArrayList<>();
            for (OrderItem item : items) {
                Integer caItemId = item.getProduct().getCaItemId();
                if (caItemId == null) continue; // product not stocked by CA — skip
                Map<String, Object> si = new LinkedHashMap<>();
                si.put("itemId",   caItemId);
                si.put("quantity", item.getQuantity());
                saleItems.add(si);
            }

            if (saleItems.isEmpty()) {
                System.out.println("[PU->CA] No CA-mapped items in order " + order.getId() + " — skipping /online-sale");
                return;
            }

            payload.put("items", saleItems);

            RestTemplate rt = new RestTemplate();
            rt.postForObject(CA_BASE + "/online-sale", payload, String.class);
            System.out.println("[PU->CA] Sale transmitted for order PU-" + order.getId());

        } catch (Exception e) {
            System.err.println("[PU->CA] /online-sale failed (non-fatal): " + e.getMessage());
        }
    }

    // GET /stock — fetches current stock details (id, name, quantity, price) from CA
    // returns empty list if CA is offline so the catalogue still loads
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchStockDetails() {
        try {
            RestTemplate rt = new RestTemplate();
            List<?> raw = rt.getForObject(CA_BASE + "/stock", List.class);
            if (raw == null) return Collections.emptyList();
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object obj : raw) {
                if (obj instanceof Map<?, ?> m) result.add((Map<String, Object>) m);
            }
            System.out.println("[PU->CA] Fetched stock details for " + result.size() + " items");
            return result;
        } catch (Exception e) {
            System.err.println("[PU->CA] /stock fetch failed (non-fatal): " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // DELETE /stock/{caItemId} — called when CA deletes a stock item
    // non-fatal: if CA is offline PU catalogue is unaffected until next sync
    public void notifyProductDeleted(Integer caItemId) {
        try {
            RestTemplate rt = new RestTemplate();
            rt.delete(CA_BASE + "/stock/" + caItemId);
            System.out.println("[CA->PU] Product deletion notified for caItemId=" + caItemId);
        } catch (Exception e) {
            System.err.println("[CA->PU] /stock delete notify failed (non-fatal): " + e.getMessage());
        }
    }

    // POST /stock/sync — called when CA adds or edits a stock item
    // non-fatal: if PU is offline the catalogue drifts until next refreshStockFromCa()
    public void notifyStockUpdated(Integer caItemId, String name, double price) {
        try {
            RestTemplate rt = new RestTemplate();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("caItemId", caItemId);
            payload.put("name",     name);
            payload.put("price",    price);
            rt.postForObject(CA_BASE + "/stock/sync", payload, String.class);
            System.out.println("[CA->PU] Stock sync notified for caItemId=" + caItemId);
        } catch (Exception e) {
            System.err.println("[CA->PU] /stock sync notify failed (non-fatal): " + e.getMessage());
        }
    }
}
