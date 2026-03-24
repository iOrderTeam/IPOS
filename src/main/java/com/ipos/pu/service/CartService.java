package com.ipos.pu.service;

import com.ipos.pu.model.CartItem;
import com.ipos.pu.model.Member;
import com.ipos.pu.model.Product;
import com.ipos.pu.repository.CartItemRepository;
import com.ipos.pu.repository.MemberRepository;
import com.ipos.pu.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       MemberRepository memberRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
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
                .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                .sum();
    }

    public boolean isNextOrderLoyalty(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        return member.getMemberType() == com.ipos.pu.model.MemberType.NON_COMMERCIAL
                && (member.getOrderCounter() + 1) % 10 == 0;
    }
}
