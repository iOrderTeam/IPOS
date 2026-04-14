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

    // pulls live stock quantities from CA and updates local product records
    // called on catalogue screen load — silently skips if CA is offline
    public void refreshStockFromCa() {
        Map<Integer, Integer> caStock = iposCaService.fetchStockLevels();
        if (caStock.isEmpty()) return;

        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            if (p.getCaItemId() == null) continue;
            Integer qty = caStock.get(p.getCaItemId());
            if (qty != null) {
                p.setStockQuantity(qty);
                productRepository.save(p);
            }
        }
    }
}
