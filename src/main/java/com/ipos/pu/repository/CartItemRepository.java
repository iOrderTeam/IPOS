package com.ipos.pu.repository;

import com.ipos.pu.model.CartItem;
import com.ipos.pu.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByMember(Member member);
    void deleteByMember(Member member);
}
