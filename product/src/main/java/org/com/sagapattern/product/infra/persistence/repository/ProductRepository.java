package org.com.sagapattern.product.infra.persistence.repository;

import org.com.sagapattern.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndSku(Long productId, String sku);
}
