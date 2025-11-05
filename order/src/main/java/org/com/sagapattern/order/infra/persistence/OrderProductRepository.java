package org.com.sagapattern.order.infra.persistence;

import org.com.sagapattern.order.domain.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
}
