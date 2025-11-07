package org.com.sagapattern.payment.infra.persistence.repository;

import org.com.sagapattern.payment.domain.entity.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {
    Optional<OrderPayment> findByOrderId(String orderId);
}
