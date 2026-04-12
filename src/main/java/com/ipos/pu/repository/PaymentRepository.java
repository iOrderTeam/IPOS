package com.ipos.pu.repository;

import com.ipos.pu.model.Payment;
import com.ipos.pu.model.Order;
import com.ipos.pu.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
    List<Payment> findByMember(Member member);
}
