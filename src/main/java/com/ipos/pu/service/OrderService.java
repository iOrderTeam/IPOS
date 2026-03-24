package com.ipos.pu.service;

import com.ipos.pu.model.*;
import com.ipos.pu.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MemberRepository memberRepository;
    private final CartService cartService;
    private final EmailService emailService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        MemberRepository memberRepository,
                        CartService cartService,
                        EmailService emailService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.memberRepository = memberRepository;
        this.cartService = cartService;
        this.emailService = emailService;
    }

    @Transactional
    public Order placeOrder(Long memberId, String paymentReference) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));

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
        Order savedOrder = orderRepository.save(order);

        member.setOrderCounter(nextCount);
        memberRepository.save(member);

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtTimeOfOrder(cartItem.getProduct().getPrice());
            orderItemRepository.save(orderItem);
        }

        cartService.clearCart(memberId);

        // Notify IPOS-CA (mock for now — real RestTemplate call added in Week 5)
        System.out.println("IPOS-CA: Deducting stock for order " + savedOrder.getId());

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
                "You can track your order status in the portal."
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

    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrder(orderId);
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}
