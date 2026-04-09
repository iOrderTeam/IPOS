package com.ipos.pu.repository;

import com.ipos.pu.model.Member;
import com.ipos.pu.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByMember(Member member);
    List<Order> findByPlacedAtBetween(LocalDateTime from, LocalDateTime to);
}
