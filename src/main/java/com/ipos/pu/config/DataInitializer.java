package com.ipos.pu.config;

import com.ipos.pu.model.*;
import com.ipos.pu.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignProductRepository campaignProductRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(ProductRepository productRepository,
                           MemberRepository memberRepository,
                           OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           CampaignRepository campaignRepository,
                           CampaignProductRepository campaignProductRepository,
                           PasswordEncoder passwordEncoder) {
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.campaignRepository = campaignRepository;
        this.campaignProductRepository = campaignProductRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) return;

        List<Product> products = List.of(
            product("Paracetamol",        "InfoPharma", "Box of 20 Caps. Unit cost per pack: £0.10",   0.10,  10345),
            product("Aspirin",            "InfoPharma", "Box of 20 Caps. Unit cost per pack: £0.50",   0.50,  12453),
            product("Analgin",            "InfoPharma", "Box of 10 Caps. Unit cost per pack: £1.20",   1.20,   4235),
            product("Celebrex 100mg",     "InfoPharma", "Box of 10 Caps. Unit cost per pack: £10.00", 10.00,   3420),
            product("Celebrex 200mg",     "InfoPharma", "Box of 10 caps. Unit cost per pack: £18.50", 18.50,   1450),
            product("Retin-A Tretin 30g", "InfoPharma", "Box of 20 caps. Unit cost per pack: £25.00", 25.00,   2013),
            product("Lipitor TB 20mg",    "InfoPharma", "Box of 30 caps. Unit cost per pack: £15.50", 15.50,   1562),
            product("Claritin CR 60g",    "InfoPharma", "Box of 20 caps. Unit cost per pack: £19.50", 19.50,   2540),
            product("Iodine tincture",    "InfoPharma", "Bottle of 100 ml. Unit cost per pack: £0.30",  0.30,  2134),
            product("Rhynol",             "InfoPharma", "Bottle of 200 ml. Unit cost per pack: £2.50",  2.50,  1908),
            product("Ospen",              "InfoPharma", "Box of 20 caps. Unit cost per pack: £10.50",  10.50,   809),
            product("Amopen",             "InfoPharma", "Box of 30 caps. Unit cost per pack: £15.00",  15.00,  1340),
            product("Vitamin C",          "InfoPharma", "Box of 30 caps. Unit cost per pack: £1.20",   1.20,  3258),
            product("Vitamin B12",        "InfoPharma", "Box of 30 caps. Unit cost per pack: £1.30",   1.30,  2673)
        );

        List<Product> savedProducts = productRepository.saveAll(products);

        // Seed a test member
        Member testMember = new Member();
        testMember.setEmail("admin@ipospu.com");
        testMember.setPassword(passwordEncoder.encode("admin123"));
        testMember.setFirstName("Test");
        testMember.setLastName("User");
        testMember.setMemberType(MemberType.NON_COMMERCIAL);
        testMember.setStatus(MemberStatus.ACTIVE);
        testMember.setPasswordChangeRequired(false);
        testMember.setAdmin(true);
        testMember.setOrderCounter(3);
        testMember = memberRepository.save(testMember);

        // Seed sample orders so order tracking and reports have data
        Product paracetamol = savedProducts.get(0);
        Product aspirin = savedProducts.get(1);
        Product analgin = savedProducts.get(2);
        Product iodine = savedProducts.get(8);
        Product rhynol = savedProducts.get(9);

        // Order 1 - delivered
        Order order1 = new Order();
        order1.setMember(testMember);
        order1.setStatus(OrderStatus.DELIVERED);
        order1.setPlacedAt(LocalDateTime.now().minusDays(14));
        order1.setTotalAmount(26.50);
        order1.setPaymentReference("PAY-A1B2C3D4");
        order1 = orderRepository.save(order1);

        orderItemRepository.save(orderItem(order1, paracetamol, 5, paracetamol.getPrice()));
        orderItemRepository.save(orderItem(order1, aspirin, 10, aspirin.getPrice()));
        orderItemRepository.save(orderItem(order1, rhynol, 8, rhynol.getPrice()));

        // Order 2 - dispatched
        Order order2 = new Order();
        order2.setMember(testMember);
        order2.setStatus(OrderStatus.DISPATCHED);
        order2.setPlacedAt(LocalDateTime.now().minusDays(5));
        order2.setTotalAmount(38.40);
        order2.setPaymentReference("PAY-E5F6G7H8");
        order2 = orderRepository.save(order2);

        orderItemRepository.save(orderItem(order2, analgin, 12, analgin.getPrice()));
        orderItemRepository.save(orderItem(order2, iodine, 20, iodine.getPrice()));
        orderItemRepository.save(orderItem(order2, paracetamol, 30, paracetamol.getPrice()));

        // Order 3 - received (most recent)
        Order order3 = new Order();
        order3.setMember(testMember);
        order3.setStatus(OrderStatus.RECEIVED);
        order3.setPlacedAt(LocalDateTime.now().minusDays(1));
        order3.setTotalAmount(15.50);
        order3.setPaymentReference("PAY-I9J0K1L2");
        order3 = orderRepository.save(order3);

        orderItemRepository.save(orderItem(order3, aspirin, 15, aspirin.getPrice()));
        orderItemRepository.save(orderItem(order3, analgin, 5, analgin.getPrice()));

        // Seed sample campaigns with products
        Campaign campaign1 = new Campaign();
        campaign1.setName("Spring Health Sale");
        campaign1.setDescription("Discounts on essential medicines");
        campaign1.setDiscountPercentage(15.0);
        campaign1.setStartDate(LocalDate.now().minusDays(5));
        campaign1.setEndDate(LocalDate.now().plusDays(25));
        campaign1.setHits(0);
        campaign1.setActive(true);
        campaign1 = campaignRepository.save(campaign1);

        campaignProductRepository.save(campaignProduct(campaign1, paracetamol));
        campaignProductRepository.save(campaignProduct(campaign1, aspirin));
        campaignProductRepository.save(campaignProduct(campaign1, savedProducts.get(12))); // Vitamin C

        Campaign campaign2 = new Campaign();
        campaign2.setName("Premium Meds Promo");
        campaign2.setDescription("Save on premium medications");
        campaign2.setDiscountPercentage(10.0);
        campaign2.setStartDate(LocalDate.now().minusDays(2));
        campaign2.setEndDate(LocalDate.now().plusDays(18));
        campaign2.setHits(0);
        campaign2.setActive(true);
        campaign2 = campaignRepository.save(campaign2);

        campaignProductRepository.save(campaignProduct(campaign2, savedProducts.get(3))); // Celebrex 100mg
        campaignProductRepository.save(campaignProduct(campaign2, savedProducts.get(6))); // Lipitor
        campaignProductRepository.save(campaignProduct(campaign2, savedProducts.get(5))); // Retin-A
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

    private CampaignProduct campaignProduct(Campaign campaign, Product product) {
        CampaignProduct cp = new CampaignProduct();
        cp.setCampaign(campaign);
        cp.setProduct(product);
        cp.setHits(0);
        return cp;
    }

    private OrderItem orderItem(Order order, Product product, int quantity, double price) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPriceAtTimeOfOrder(price);
        return item;
    }
}
