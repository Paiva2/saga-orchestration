package org.com.sagapattern.order.domain.usecase.createOrder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.sagapattern.order.domain.enums.EPaymentMethod;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderInput {
    @NotEmpty
    private List<ProductInput> products;

    @Email
    @NotEmpty
    private String endCustomerEmail;

    @NotNull
    private EPaymentMethod paymentMethod;

    @Min(0)
    private Integer installments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInput {
        @NotNull
        private Long productId;

        @Min(0)
        @NotNull
        private Integer quantity;

        @NotEmpty
        private String sku;
    }
}
