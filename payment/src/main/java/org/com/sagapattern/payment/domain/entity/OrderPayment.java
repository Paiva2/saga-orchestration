package org.com.sagapattern.payment.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.sagapattern.payment.domain.enums.EOrderPaymentStatus;
import org.com.sagapattern.payment.domain.enums.EPaymentMethod;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_orders_payment")
public class OrderPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pyt_id")
    private Long id;

    @Column(name = "pyt_total_value")
    private BigDecimal totalValue;

    @Column(name = "pyt_total_items")
    private Integer totalItems;

    @Enumerated(EnumType.STRING)
    @Column(name = "pyt_payment_method")
    private EPaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "pyt_status")
    private EOrderPaymentStatus status;

    @Column(name = "pyt_installments")
    private Integer installments;

    @Column(name = "pyt_order_id")
    private String orderId;

    @CreationTimestamp
    @Column(name = "pyt_created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "pyt_updated_at")
    private Date updatedAt;
}
