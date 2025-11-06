package org.com.sagapattern.product.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pd_id")
    private Long id;

    @Column(name = "pd_sku")
    private String sku;

    @Column(name = "pd_price")
    private BigDecimal price;

    @Column(name = "pd_available")
    private Integer available;

    @Column(name = "pd_seller_name")
    private String sellerName; // would be another entity (Seller)

    @CreationTimestamp
    @Column(name = "pd_created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "pd_updated_at")
    private Date updatedAt;
}
