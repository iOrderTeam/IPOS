package com.ipos.pu.service;

import com.ipos.pu.model.Product;
import com.ipos.pu.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CatalogueService {

    private final ProductRepository productRepository;
    private final IposCaService iposCaService;

    public CatalogueService(ProductRepository productRepository, IposCaService iposCaService) {
        this.productRepository = productRepository;
        this.iposCaService     = iposCaService;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> searchByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return productRepository.findAll();
        }
        return productRepository.findByNameContainingIgnoreCase(keyword.trim());
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // pulls live stock quantities and prices from CA and updates local product records
    // called on catalogue screen load — silently skips if CA is offline
    public void refreshStockFromCa() {
        List<Map<String, Object>> caStock = iposCaService.fetchStockDetails();
        if (caStock.isEmpty()) return;

        List<Product> products = productRepository.findAll();
        Map<Integer, Map<String, Object>> byId = new java.util.HashMap<>();
        for (Map<String, Object> item : caStock) {
            Object idObj = item.get("id");
            if (idObj != null) byId.put(((Number) idObj).intValue(), item);
        }

        for (Product p : products) {
            if (p.getCaItemId() == null) continue;
            Map<String, Object> caItem = byId.get(p.getCaItemId());
            if (caItem == null) continue;
            Object qty   = caItem.get("quantity");
            Object price = caItem.get("price");
            if (qty   != null) p.setStockQuantity(((Number) qty).intValue());
            if (price != null) p.setPrice(((Number) price).doubleValue());
            productRepository.save(p);
        }
    }

    // called when CA notifies us that a stock item was deleted
    // removes the matching product from PU's catalogue
    public void deleteProductByCaItemId(Integer caItemId) {
        productRepository.findByCaItemId(caItemId).ifPresent(productRepository::delete);
    }

    // called when CA notifies us that a stock item was added or edited
    // updates an existing product's name and price, or creates a new product entry
    public void syncProductFromCa(Integer caItemId, String name, double price) {
        Product p = productRepository.findByCaItemId(caItemId).orElseGet(Product::new);
        p.setCaItemId(caItemId);
        p.setName(name);
        p.setPrice(price);
        productRepository.save(p);
    }
}
