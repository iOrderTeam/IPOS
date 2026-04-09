package com.ipos.pu.service;

import org.springframework.stereotype.Service;

@Service
public class IposCaService {

    public void deductStock(Long productId, int quantity) {
        System.out.println("=== IPOS-CA STOCK DEDUCTION ===");
        System.out.println("  Product ID: " + productId);
        System.out.println("  Quantity to deduct: " + quantity);
        System.out.println("===============================");

        // TODO Week 5 — replace above with:
        // RestTemplate restTemplate = new RestTemplate();
        // Map<String, Object> payload = new HashMap<>();
        // payload.put("productId", productId);
        // payload.put("quantity", quantity);
        // restTemplate.postForObject(
        //     "http://[IPOS-CA-IP]:8082/api/stock/deduct",
        //     payload, String.class);
    }
}
