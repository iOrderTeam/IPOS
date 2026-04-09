package com.ipos.pu.service;

import com.ipos.pu.model.Product;
import com.ipos.pu.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CatalogueService {

    private final ProductRepository productRepository;

    public CatalogueService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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
}
