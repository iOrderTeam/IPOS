package com.ipos.pu.service;

import com.ipos.pu.model.*;
import com.ipos.pu.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MemberRepository memberRepository;
    private final CampaignProductRepository campaignProductRepository;
    private final PaymentRepository paymentRepository;
    private final CartService cartService;
    private final EmailService emailService;
    private final IposCaService iposCaService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        MemberRepository memberRepository,
                        CampaignProductRepository campaignProductRepository,
                        PaymentRepository paymentRepository,
                        CartService cartService,
                        EmailService emailService,
                        IposCaService iposCaService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.memberRepository = memberRepository;
        this.campaignProductRepository = campaignProductRepository;
        this.paymentRepository = paymentRepository;
        this.cartService = cartService;
        this.emailService = emailService;
        this.iposCaService = iposCaService;
    }

    @Transactional
    public Order placeOrder(Long memberId, String paymentReference,
                            String cardholderName, String maskedCardNumber, String expiryDate,
                            String deliveryAddress) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));

        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new IllegalArgumentException("Delivery address is required.");
        }

        List<CartItem> cartItems = cartService.getCart(memberId);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty.");
        }

        int nextCount = member.getOrderCounter() + 1;
        boolean loyaltyDiscount = member.getMemberType() == MemberType.NON_COMMERCIAL
                && nextCount % 10 == 0;

        double total = cartService.getCartTotal(memberId);
        if (loyaltyDiscount) {
            total = total * 0.90;
        }

        Order order = new Order();
        order.setMember(member);
        order.setStatus(OrderStatus.RECEIVED);
        order.setPlacedAt(LocalDateTime.now());
        order.setTotalAmount(total);
        order.setPaymentReference(paymentReference);
        order.setDeliveryAddress(deliveryAddress.trim());
        Order savedOrder = orderRepository.save(order);

        member.setOrderCounter(nextCount);
        memberRepository.save(member);

        List<OrderItem> savedItems = new java.util.ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtTimeOfOrder(cartItem.getProduct().getPrice());
            savedItems.add(orderItemRepository.save(orderItem));
        }

        cartService.clearCart(memberId);

        // transmit full sale to CA — CA deducts stock on its side
        iposCaService.passSaleToCa(savedOrder, savedItems);

        // Increment per-product campaign counters (CAMP_X_1 etc.)
        // Brief: "increased every time a customer includes Item_X_1 in an order"
        LocalDate today = LocalDate.now();
        for (CartItem cartItem : cartItems) {
            List<CampaignProduct> cps = campaignProductRepository.findByProduct(cartItem.getProduct());
            for (CampaignProduct cp : cps) {
                Campaign c = cp.getCampaign();
                if (c.isActive() && !today.isBefore(c.getStartDate()) && !today.isAfter(c.getEndDate())) {
                    cp.setHits(cp.getHits() + cartItem.getQuantity());
                    campaignProductRepository.save(cp);
                }
            }
        }

        // Record payment in payments table
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setMember(member);
        payment.setCardholderName(cardholderName);
        payment.setMaskedCardNumber(maskedCardNumber);
        payment.setExpiryDate(expiryDate);
        payment.setAmount(total);
        payment.setPaidAt(LocalDateTime.now());
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        String discountLine = loyaltyDiscount ? "10% loyalty discount applied!\n" : "";
        emailService.sendEmail(
                member.getEmail(),
                "Your IPOS-PU Order Confirmation",
                "Thank you for your order!\n" +
                "Order ID: " + savedOrder.getId() + "\n" +
                discountLine +
                "Total: £" + String.format("%.2f", total) + "\n" +
                "Status: RECEIVED\n" +
                "Payment Ref: " + paymentReference + "\n\n" +
                "Delivery Address:\n" + savedOrder.getDeliveryAddress() + "\n\n" +
                "Track your order: http://localhost:8080/track/" + savedOrder.getId() + "\n" +
                "(Or log in to the portal and open 'My Orders'.)"
        );

        return savedOrder;
    }

    public List<Order> getOrdersForMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        return orderRepository.findByMember(member);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));
    }

    public List<Order> getOrdersBetween(LocalDateTime from, LocalDateTime to) {
        return orderRepository.findByPlacedAtBetween(from, to);
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrder(orderId);
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        // Brief: "purchased" counters only increase when order is paid for (DELIVERED)
        if (newStatus == OrderStatus.DELIVERED) {
            LocalDate orderDate = order.getPlacedAt().toLocalDate();
            List<OrderItem> items = orderItemRepository.findByOrder(order);
            for (OrderItem oi : items) {
                List<CampaignProduct> cps = campaignProductRepository.findByProduct(oi.getProduct());
                for (CampaignProduct cp : cps) {
                    Campaign c = cp.getCampaign();
                    if (c.isActive()
                            && !orderDate.isBefore(c.getStartDate())
                            && !orderDate.isAfter(c.getEndDate())) {
                        cp.setPurchased(cp.getPurchased() + oi.getQuantity());
                        campaignProductRepository.save(cp);
                    }
                }
            }
        }

        return saved;
    }
}
