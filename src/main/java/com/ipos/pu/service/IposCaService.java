package com.ipos.pu.service;

import com.ipos.pu.model.Order;
import com.ipos.pu.model.OrderItem;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
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

    // GET /stock — fetches current stock quantities from CA for all 5 CA-mapped products
    // returns a map of caItemId -> quantity
    // returns empty map if CA is offline so the catalogue still loads
    public Map<Integer, Integer> fetchStockLevels() {
        Map<Integer, Integer> result = new LinkedHashMap<>();
        try {
            RestTemplate rt = new RestTemplate();
            List<?> items = rt.getForObject(CA_BASE + "/stock", List.class);
            if (items == null) return result;

            for (Object obj : items) {
                if (obj instanceof Map<?, ?> m) {
                    Object idObj  = m.get("itemId");
                    Object qtyObj = m.get("quantity");
                    if (idObj != null && qtyObj != null) {
                        result.put(((Number) idObj).intValue(), ((Number) qtyObj).intValue());
                    }
                }
            }
            System.out.println("[PU->CA] Fetched stock levels for " + result.size() + " items");

        } catch (Exception e) {
            System.err.println("[PU->CA] /stock fetch failed (non-fatal): " + e.getMessage());
        }
        return result;
    }
}
