package com.ipos.pu.controller;

import com.ipos.pu.model.Order;
import com.ipos.pu.model.OrderStatus;
import com.ipos.pu.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> getOrdersForMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(orderService.getOrdersForMember(memberId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(orderService.getOrder(orderId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{orderId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long orderId,
                                          @RequestBody Map<String, String> body) {
        try {
            OrderStatus status = OrderStatus.valueOf(body.get("status"));
            Order updated = orderService.updateStatus(orderId, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
