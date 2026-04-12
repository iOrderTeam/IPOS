package com.ipos.pu.service;

import com.ipos.pu.model.*;
import com.ipos.pu.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final CampaignProductRepository campaignProductRepository;

    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       MemberRepository memberRepository,
                       CampaignProductRepository campaignProductRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.campaignProductRepository = campaignProductRepository;
    }

    public void addToCart(Long memberId, Long productId, int quantity) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        CartItem item = new CartItem();
        item.setMember(member);
        item.setProduct(product);
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    public List<CartItem> getCart(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        return cartItemRepository.findByMember(member);
    }

    @Transactional
    public void clearCart(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        cartItemRepository.deleteByMember(member);
    }

    public double getCartTotal(Long memberId) {
        return getCart(memberId).stream()
                .mapToDouble(item -> item.getQuantity() * getDiscountedUnitPrice(item.getProduct()))
                .sum();
    }

    // Single source of truth for "what does the buyer pay per unit right now".
    // Used by both the cart total and the cart UI so the row prices and the
    // grand total can never disagree.
    public double getDiscountedUnitPrice(Product product) {
        double bestDiscount = getBestDiscountForProduct(product, java.time.LocalDate.now());
        if (bestDiscount > 0) {
            return product.getPrice() * (1 - bestDiscount / 100.0);
        }
        return product.getPrice();
    }

    private double getBestDiscountForProduct(Product product, java.time.LocalDate today) {
        List<CampaignProduct> cps = campaignProductRepository.findByProduct(product);
        double best = 0;
        for (CampaignProduct cp : cps) {
            Campaign c = cp.getCampaign();
            if (c.isActive()
                    && !today.isBefore(c.getStartDate())
                    && !today.isAfter(c.getEndDate())) {
                best = Math.max(best, cp.getEffectiveDiscount());
            }
        }
        return best;
    }

    @Transactional
    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    public int getCartItemCount(Long memberId) {
        return getCart(memberId).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public boolean isNextOrderLoyalty(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        return member.getMemberType() == com.ipos.pu.model.MemberType.NON_COMMERCIAL
                && (member.getOrderCounter() + 1) % 10 == 0;
    }
}
