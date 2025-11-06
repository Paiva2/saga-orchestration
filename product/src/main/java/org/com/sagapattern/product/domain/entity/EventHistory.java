package org.com.sagapattern.product.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_events_history")
public class EventHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evh_id")
    private Long id;

    @Column(name = "evh_event_source")
    private String eventSource;

    @Column(name = "evh_event_transaction_id")
    private String eventTransactionId;

    @Column(name = "evh_success")
    private Boolean success;

    @CreationTimestamp
    @Column(name = "evh_created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "evh_updated_at")
    private Date updatedAt;
}
