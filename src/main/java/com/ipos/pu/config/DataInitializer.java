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

        // ── Products ────────────────────────────────────────────────────────────
        // caItemId maps to CA's stock IDs. All 14 products from scenarios.md.
        // Stock quantities seeded as 0 — refreshStockFromCa() overwrites on load.
        List<Product> products = productRepository.saveAll(List.of(
            product("Paracetamol",          "InfoPharma", "Box of 20 Caps",   0.20,  121, 1),
            product("Aspirin",              "InfoPharma", "Box of 20 Caps",   1.00,  201, 2),
            product("Analgin",              "InfoPharma", "Box of 10 Caps",   2.40,   25, 3),
            product("Celebrex 100mg",       "InfoPharma", "Box of 10 Caps",  20.00,   43, 4),
            product("Celebrex 200mg",       "InfoPharma", "Box of 10 Caps",  37.00,   35, 5),
            product("Retin-A Tretin 30g",   "InfoPharma", "Box of 20 Caps",  50.00,   28, 6),
            product("Lipitor TB 20mg",      "InfoPharma", "Box of 30 Caps",  31.00,   10, 7),
            product("Claritin CR 60g",      "InfoPharma", "Box of 20 Caps",  39.00,   21, 8),
            product("Iodine Tincture",      "InfoPharma", "Bottle of 100ml",  0.60,   35, 9),
            product("Rhynol",               "InfoPharma", "Bottle of 200ml",  5.00,   14, 10),
            product("Ospen",                "InfoPharma", "Box of 20 Caps",  21.00,   78, 11),
            product("Amopen",               "InfoPharma", "Box of 30 Caps",  30.00,   90, 12),
            product("Vitamin C",            "InfoPharma", "Box of 30 Caps",   2.40,   22, 13),
            product("Vitamin B12",          "InfoPharma", "Box of 30 Caps",   2.60,   43, 14)
        ));

        // convenience references by position
        Product paracetamol  = products.get(0);
        Product aspirin      = products.get(1);
        Product analgin      = products.get(2);
        Product celebrex100  = products.get(3);
        Product celebrex200  = products.get(4);
        Product retinA       = products.get(5);
        Product lipitor      = products.get(6);
        Product claritin     = products.get(7);
        Product iodine       = products.get(8);
        Product rhynol       = products.get(9);
        Product ospen        = products.get(10);
        Product amopen       = products.get(11);
        Product vitaminC     = products.get(12);
        Product vitaminB12   = products.get(13);

        // ── Admin accounts ───────────────────────────────────────────────────────
        // sysdba / masterkey — matches scenarios.md exactly
        Member sysdba = new Member();
        sysdba.setEmail("sysdba");
        sysdba.setPassword(passwordEncoder.encode("masterkey"));
        sysdba.setFirstName("System");
        sysdba.setLastName("Admin");
        sysdba.setMemberType(MemberType.NON_COMMERCIAL);
        sysdba.setStatus(MemberStatus.ACTIVE);
        sysdba.setPasswordChangeRequired(false);
        sysdba.setAdmin(true);
        sysdba.setOrderCounter(0);
        memberRepository.save(sysdba);

        // manager / GetPU_it_done
        Member manager = new Member();
        manager.setEmail("manager");
        manager.setPassword(passwordEncoder.encode("GetPU_it_done"));
        manager.setFirstName("PU");
        manager.setLastName("Manager");
        manager.setMemberType(MemberType.NON_COMMERCIAL);
        manager.setStatus(MemberStatus.ACTIVE);
        manager.setPasswordChangeRequired(false);
        manager.setAdmin(true);
        manager.setOrderCounter(0);
        memberRepository.save(manager);

        // ── Non-commercial members ───────────────────────────────────────────────
        // PU0001 — orderCounter=8 so their next purchase is 9th, one after is 10th (loyalty discount)
        Member pu0001 = new Member();
        pu0001.setEmail("cool@example.com");
        pu0001.setPassword(passwordEncoder.encode("12ss_56_SS"));
        pu0001.setFirstName("Peter");
        pu0001.setLastName("Popov");
        pu0001.setMemberType(MemberType.NON_COMMERCIAL);
        pu0001.setStatus(MemberStatus.ACTIVE);
        pu0001.setPasswordChangeRequired(false);
        pu0001.setAdmin(false);
        pu0001.setOrderCounter(8); // 8 past purchases — 10th order gets loyalty discount
        pu0001 = memberRepository.save(pu0001);

        // PU0002
        Member pu0002 = new Member();
        pu0002.setEmail("cool1@example.com");
        pu0002.setPassword(passwordEncoder.encode("34pp_78_LL"));
        pu0002.setFirstName("Jane");
        pu0002.setLastName("Smith");
        pu0002.setMemberType(MemberType.NON_COMMERCIAL);
        pu0002.setStatus(MemberStatus.ACTIVE);
        pu0002.setPasswordChangeRequired(false);
        pu0002.setAdmin(false);
        pu0002.setOrderCounter(0);
        memberRepository.save(pu0002);

        // ── Commercial member ────────────────────────────────────────────────────
        // PU0003 — PENDING status, awaiting SA approval
        Member pu0003 = new Member();
        pu0003.setEmail("pondPharma@example.com");
        pu0003.setPassword(passwordEncoder.encode("Pond1234!"));
        pu0003.setFirstName("Pond");
        pu0003.setLastName("Pharmacy");
        pu0003.setMemberType(MemberType.COMMERCIAL);
        pu0003.setStatus(MemberStatus.PENDING);
        pu0003.setPasswordChangeRequired(false);
        pu0003.setAdmin(false);
        pu0003.setOrderCounter(0);
        pu0003.setCompanyName("Pond Pharmacy");
        pu0003.setCompanyRegistrationNumber("UK10003429CompH");
        pu0003.setBusinessType("Pharmacy");
        pu0003.setAddress("Chislehurst\n25, High Street\nBR7 5BN");
        pu0003.setDirectorDetails("Director of Pond Pharmacy Ltd");
        memberRepository.save(pu0003);

        // ── Past orders for PU0001 ───────────────────────────────────────────────
        // 8 historical orders to match the orderCounter — gives reports real data
        // and means PU0001's next order in the demo is #9, the one after is #10 (discount)
        for (int i = 1; i <= 8; i++) {
            Order o = new Order();
            o.setMember(pu0001);
            o.setStatus(OrderStatus.DELIVERED);
            o.setPlacedAt(LocalDateTime.of(2026, 3, i, 10, 0));
            o.setTotalAmount(i % 2 == 0 ? 21.00 : 5.00);
            o.setPaymentReference("PAY-HIST-00" + i);
            o.setDeliveryAddress("1 Liverpool Street, London EC2V 8NS");
            o = orderRepository.save(o);
            // alternate products to make reports look varied
            Product p = i % 2 == 0 ? ospen : aspirin;
            orderItemRepository.save(orderItem(o, p, 1, p.getPrice()));
        }

        // ── Campaigns from scenarios.md ──────────────────────────────────────────
        // March Promotion — scenario 17
        // Per-product discounts stored as discountOverride on CampaignProduct
        Campaign marchPromo = new Campaign();
        marchPromo.setName("March Promotion");
        marchPromo.setDescription("Spring discounts on selected medicines");
        marchPromo.setDiscountPercentage(0); // default unused — each product has its own rate
        marchPromo.setStartDate(LocalDate.of(2026, 3, 15));
        marchPromo.setEndDate(LocalDate.of(2026, 4, 20));
        marchPromo.setHits(0);
        marchPromo.setActive(true);
        marchPromo = campaignRepository.save(marchPromo);

        campaignProductRepository.save(campaignProduct(marchPromo, aspirin,     5.0));
        campaignProductRepository.save(campaignProduct(marchPromo, analgin,    10.0));
        campaignProductRepository.save(campaignProduct(marchPromo, celebrex100,10.0));
        campaignProductRepository.save(campaignProduct(marchPromo, retinA,     20.0));

        // April Promotion — scenario 18 (already expired but seeded for history)
        Campaign aprilPromo = new Campaign();
        aprilPromo.setName("April Promotion");
        aprilPromo.setDescription("April discounts on selected medicines");
        aprilPromo.setDiscountPercentage(0);
        aprilPromo.setStartDate(LocalDate.of(2026, 4, 5));
        aprilPromo.setEndDate(LocalDate.of(2026, 4, 10));
        aprilPromo.setHits(0);
        aprilPromo.setActive(true);
        aprilPromo = campaignRepository.save(aprilPromo);

        campaignProductRepository.save(campaignProduct(aprilPromo, ospen,    20.0));
        campaignProductRepository.save(campaignProduct(aprilPromo, vitaminC, 10.0));
    }

    private Product product(String name, String brand, String description,
                            double price, int stock, Integer caItemId) {
        Product p = new Product();
        p.setName(name);
        p.setBrand(brand);
        p.setDescription(description);
        p.setPrice(price);
        p.setStockQuantity(stock);
        p.setCaItemId(caItemId);
        return p;
    }

    private CampaignProduct campaignProduct(Campaign campaign, Product product, double discountOverride) {
        CampaignProduct cp = new CampaignProduct();
        cp.setCampaign(campaign);
        cp.setProduct(product);
        cp.setHits(0);
        cp.setPurchased(0);
        cp.setDiscountOverride(discountOverride);
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
