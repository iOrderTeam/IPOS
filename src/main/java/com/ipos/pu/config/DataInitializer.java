package com.ipos.pu.config;

import com.ipos.pu.model.Product;
import com.ipos.pu.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) return;

        List<Product> products = List.of(
            // 100-series: box/Caps
            product("Paracetamol",        "InfoPharma", "Box of 20 Caps. Unit cost per pack: £0.10",   0.10,  10345),
            product("Aspirin",            "InfoPharma", "Box of 20 Caps. Unit cost per pack: £0.50",   0.50,  12453),
            product("Analgin",            "InfoPharma", "Box of 10 Caps. Unit cost per pack: £1.20",   1.20,   4235),
            product("Celebrex 100mg",     "InfoPharma", "Box of 10 Caps. Unit cost per pack: £10.00", 10.00,   3420),
            product("Celebrex 200mg",     "InfoPharma", "Box of 10 caps. Unit cost per pack: £18.50", 18.50,   1450),
            product("Retin-A Tretin 30g", "InfoPharma", "Box of 20 caps. Unit cost per pack: £25.00", 25.00,   2013),
            product("Lipitor TB 20mg",    "InfoPharma", "Box of 30 caps. Unit cost per pack: £15.50", 15.50,   1562),
            product("Claritin CR 60g",    "InfoPharma", "Box of 20 caps. Unit cost per pack: £19.50", 19.50,   2540),
            // 200-series: bottle/ml
            product("Iodine tincture",    "InfoPharma", "Bottle of 100 ml. Unit cost per pack: £0.30",  0.30,  2134),
            product("Rhynol",             "InfoPharma", "Bottle of 200 ml. Unit cost per pack: £2.50",  2.50,  1908),
            // 300-series: box/caps
            product("Ospen",              "InfoPharma", "Box of 20 caps. Unit cost per pack: £10.50",  10.50,   809),
            product("Amopen",             "InfoPharma", "Box of 30 caps. Unit cost per pack: £15.00",  15.00,  1340),
            // 400-series: box/caps
            product("Vitamin C",          "InfoPharma", "Box of 30 caps. Unit cost per pack: £1.20",   1.20,  3258),
            product("Vitamin B12",        "InfoPharma", "Box of 30 caps. Unit cost per pack: £1.30",   1.30,  2673)
        );

        productRepository.saveAll(products);
    }

    private Product product(String name, String brand, String description, double price, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setBrand(brand);
        p.setDescription(description);
        p.setPrice(price);
        p.setStockQuantity(stock);
        return p;
    }
}
