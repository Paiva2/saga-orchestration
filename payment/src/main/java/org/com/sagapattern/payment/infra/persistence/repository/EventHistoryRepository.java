package org.com.sagapattern.payment.infra.persistence.repository;

import org.com.sagapattern.payment.domain.entity.EventHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventHistoryRepository extends JpaRepository<EventHistory, Long> {
    Optional<EventHistory> findByEventTransactionIdAndStatus(String transactionId, String status);
}
