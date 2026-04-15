package com.ipos.pu.controller;

import com.ipos.pu.service.CatalogueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class CatalogueController {

    private final CatalogueService catalogueService;

    public CatalogueController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        return ResponseEntity.ok(catalogueService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        return catalogueService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/products/ca/{caItemId}
    // Called by IPOS-CA when a stock item is deleted from stock management.
    // Removes the matching product from PU's catalogue so it no longer appears online.
    @DeleteMapping("/ca/{caItemId}")
    public ResponseEntity<?> deleteProductByCaItemId(@PathVariable Integer caItemId) {
        catalogueService.deleteProductByCaItemId(caItemId);
        System.out.println("[CA->PU] Product deleted for caItemId=" + caItemId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // POST /api/products/ca-sync
    // Called by IPOS-CA when a stock item is added or edited.
    // Updates the matching product's name and price in PU's catalogue.
    // Body: { "caItemId": 3, "name": "Analgin", "price": 2.40 }
    @PostMapping("/ca-sync")
    public ResponseEntity<?> syncProductFromCa(@RequestBody Map<String, Object> body) {
        Integer caItemId = ((Number) body.get("caItemId")).intValue();
        String  name     = (String) body.get("name");
        double  price    = ((Number) body.get("price")).doubleValue();
        catalogueService.syncProductFromCa(caItemId, name, price);
        System.out.println("[CA->PU] Stock synced for caItemId=" + caItemId);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
