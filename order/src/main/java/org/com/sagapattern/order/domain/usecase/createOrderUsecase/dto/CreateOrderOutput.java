package org.com.sagapattern.order.domain.usecase.createOrderUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.sagapattern.order.domain.enums.EPaymentMethod;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderOutput {
    private Long orderId;
    private BigDecimal totalValue;
    private Integer totalItems;
    private EPaymentMethod paymentMethod;
    private Integer installments;
    private List<ProductInput> products;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInput {
        private Long productId;
        private Integer quantity;
        private String sku;
    }
}
