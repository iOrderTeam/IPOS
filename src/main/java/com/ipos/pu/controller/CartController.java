package com.ipos.pu.controller;

import com.ipos.pu.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestParam Long memberId,
                                       @RequestParam Long productId,
                                       @RequestParam int quantity) {
        cartService.addToCart(memberId, productId, quantity);
        return ResponseEntity.ok("Item added to cart.");
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<?> getCart(@PathVariable Long memberId) {
        return ResponseEntity.ok(cartService.getCart(memberId));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<?> clearCart(@PathVariable Long memberId) {
        cartService.clearCart(memberId);
        return ResponseEntity.ok("Cart cleared.");
    }
}
