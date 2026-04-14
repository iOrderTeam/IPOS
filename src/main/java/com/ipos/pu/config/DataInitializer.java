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
            // only the 5 products the merchant stocks in CA — caItemId matches CA's locked stock IDs
            // stock quantities are seeded as a fallback; refreshStockFromCa() overwrites them on load
            product("Paracetamol 500mg", "InfoPharma", "Box of 20 Caps. Unit cost per pack: £0.10",  0.10, 0, 1),
            product("Ibuprofen 200mg",   "InfoPharma", "Box of 20 Caps. Unit cost per pack: £3.20",  3.20, 0, 2),
            product("Amoxicillin 250mg", "InfoPharma", "Box of 10 Caps. Unit cost per pack: £8.75",  8.75, 0, 3),
            product("Cetirizine 10mg",   "InfoPharma", "Box of 10 Caps. Unit cost per pack: £4.10",  4.10, 0, 4),
            product("Omeprazole 20mg",   "InfoPharma", "Box of 20 caps. Unit cost per pack: £5.60",  5.60, 0, 5)
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
        Product paracetamol = savedProducts.get(0); // Paracetamol 500mg
        Product ibuprofen   = savedProducts.get(1); // Ibuprofen 200mg
        Product amoxicillin = savedProducts.get(2); // Amoxicillin 250mg
        Product cetirizine  = savedProducts.get(3); // Cetirizine 10mg
        Product omeprazole  = savedProducts.get(4); // Omeprazole 20mg

        // Order 1 - delivered
        Order order1 = new Order();
        order1.setMember(testMember);
        order1.setStatus(OrderStatus.DELIVERED);
        order1.setPlacedAt(LocalDateTime.now().minusDays(14));
        order1.setTotalAmount(36.95);
        order1.setPaymentReference("PAY-A1B2C3D4");
        order1.setDeliveryAddress("27 Sainsbury Close, London, E1 6TJ");
        order1 = orderRepository.save(order1);

        orderItemRepository.save(orderItem(order1, paracetamol, 5,  paracetamol.getPrice()));
        orderItemRepository.save(orderItem(order1, ibuprofen,   8,  ibuprofen.getPrice()));
        orderItemRepository.save(orderItem(order1, omeprazole,  2,  omeprazole.getPrice()));

        // Order 2 - dispatched
        Order order2 = new Order();
        order2.setMember(testMember);
        order2.setStatus(OrderStatus.DISPATCHED);
        order2.setPlacedAt(LocalDateTime.now().minusDays(5));
        order2.setTotalAmount(43.25);
        order2.setPaymentReference("PAY-E5F6G7H8");
        order2.setDeliveryAddress("27 Sainsbury Close, London, E1 6TJ");
        order2 = orderRepository.save(order2);

        orderItemRepository.save(orderItem(order2, amoxicillin, 4, amoxicillin.getPrice()));
        orderItemRepository.save(orderItem(order2, cetirizine,  3, cetirizine.getPrice()));
        orderItemRepository.save(orderItem(order2, paracetamol, 10, paracetamol.getPrice()));

        // Order 3 - received (most recent)
        Order order3 = new Order();
        order3.setMember(testMember);
        order3.setStatus(OrderStatus.RECEIVED);
        order3.setPlacedAt(LocalDateTime.now().minusDays(1));
        order3.setTotalAmount(22.30);
        order3.setPaymentReference("PAY-I9J0K1L2");
        order3.setDeliveryAddress("27 Sainsbury Close, London, E1 6TJ");
        order3 = orderRepository.save(order3);

        orderItemRepository.save(orderItem(order3, ibuprofen,  2, ibuprofen.getPrice()));
        orderItemRepository.save(orderItem(order3, omeprazole, 3, omeprazole.getPrice()));

        // Seed sample campaigns using the 5 merchant products
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
        campaignProductRepository.save(campaignProduct(campaign1, ibuprofen));
        campaignProductRepository.save(campaignProduct(campaign1, cetirizine));

        Campaign campaign2 = new Campaign();
        campaign2.setName("Prescription Essentials Promo");
        campaign2.setDescription("Save on prescription-strength medicines");
        campaign2.setDiscountPercentage(10.0);
        campaign2.setStartDate(LocalDate.now().minusDays(2));
        campaign2.setEndDate(LocalDate.now().plusDays(18));
        campaign2.setHits(0);
        campaign2.setActive(true);
        campaign2 = campaignRepository.save(campaign2);

        campaignProductRepository.save(campaignProduct(campaign2, amoxicillin));
        campaignProductRepository.save(campaignProduct(campaign2, omeprazole));
    }

    private Product product(String name, String brand, String description, double price, int stock, Integer caItemId) {
        Product p = new Product();
        p.setName(name);
        p.setBrand(brand);
        p.setDescription(description);
        p.setPrice(price);
        p.setStockQuantity(stock);
        p.setCaItemId(caItemId);
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
