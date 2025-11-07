package org.com.sagapattern.order.infra.persistence;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.com.sagapattern.order.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndSku(Long id, String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "jakarta.persistence.lock.timeout", value = "8000") // 8s lock max
    })
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findByIdForUpdate(@Param("ids") List<Long> ids);
}
