package org.com.sagapattern.order.infra.persistence;

import org.com.sagapattern.order.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndSku(Long id, String sku);
}
