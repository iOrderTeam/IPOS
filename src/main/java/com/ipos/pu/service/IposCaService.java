package com.ipos.pu.service;

import com.ipos.pu.model.Order;
import com.ipos.pu.model.OrderItem;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class IposCaService {

    public void deductStock(Long productId, int quantity) {
        System.out.println("=== IPOS-CA STOCK DEDUCTION ===");
        System.out.println("  Product ID: " + productId);
        System.out.println("  Quantity to deduct: " + quantity);
        System.out.println("===============================");
    }

    // Brief (p.20 IPOS-PU-SALES): successful orders via IPOS-PU should be
    // 'propagated' to the respective merchant (IPOS-CA). Pass the full order
    // along with the delivery address so CA can dispatch the goods.
    public void passSaleToCa(Order order, List<OrderItem> items) {
        System.out.println("=== IPOS-CA SALE PROPAGATION ===");
        System.out.println("  Order ID: " + order.getId());
        System.out.println("  Member: " + order.getMember().getEmail());
        System.out.println("  Total: \u00a3" + String.format("%.2f", order.getTotalAmount()));
        System.out.println("  Payment Ref: " + order.getPaymentReference());
        System.out.println("  Delivery Address:");
        for (String line : order.getDeliveryAddress().split("\\r?\\n")) {
            System.out.println("    " + line);
        }
        System.out.println("  Items:");
        for (OrderItem item : items) {
            System.out.println("    - " + item.getProduct().getName()
                    + " x" + item.getQuantity());
        }
        System.out.println("================================");
    }
}
