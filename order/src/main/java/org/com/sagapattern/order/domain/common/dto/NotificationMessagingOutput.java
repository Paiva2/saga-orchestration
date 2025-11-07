package org.com.sagapattern.order.domain.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.sagapattern.order.domain.entity.OrderProduct;
import org.com.sagapattern.order.domain.enums.EPaymentMethod;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessagingOutput {
    private Long orderId;
    private BigDecimal totalValue;
    private Integer totalItems;
    private EPaymentMethod paymentMethod;
    private Integer installments;
    private String customerEmail;
    private List<ProductInput> products;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInput {
        private Long productId;
        private Integer quantity;
        private String sku;

        public ProductInput(OrderProduct orderProduct) {
            this.productId = orderProduct.getProduct().getId();
            this.quantity = orderProduct.getProduct().getAvailable();
            this.sku = orderProduct.getProduct().getSku();
        }
    }
}
