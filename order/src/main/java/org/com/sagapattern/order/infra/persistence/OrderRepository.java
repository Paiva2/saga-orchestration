package org.com.sagapattern.order.infra.persistence;

import org.com.sagapattern.order.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
