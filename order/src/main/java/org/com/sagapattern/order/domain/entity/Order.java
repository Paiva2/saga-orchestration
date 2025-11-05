package org.com.sagapattern.order.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.sagapattern.order.domain.enums.EOrderStatus;
import org.com.sagapattern.order.domain.enums.EPaymentMethod;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "or_id")
    private Long id;

    @Column(name = "or_payment_method")
    @Enumerated(EnumType.STRING)
    private EPaymentMethod paymentMethod;

    @Column(name = "or_status")
    @Enumerated(EnumType.STRING)
    private EOrderStatus status;

    @Column(name = "or_installments")
    private Integer installments;

    @Column(name = "or_order_total")
    private BigDecimal orderTotal;

    @Column(name = "or_items_quantity")
    private Integer itemsQuantity;

    @CreationTimestamp
    @Column(name = "or_created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "or_updated_at")
    private Date updatedAt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "order")
    private List<OrderProduct> products;
}
