package org.com.sagapattern.payment.domain.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.sagapattern.payment.domain.enums.ESagaPhase;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderSagaEvent {
    private String eventTransactionId;
    private String source;
    private SagaEventPayload payload;
    private ESagaPhase phase;
    private List<SagaHistory> history;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SagaEventPayload {
        private Long orderId;
        private BigDecimal totalValue;
        private Integer totalItems;
        private String paymentMethod;
        private Integer installments;
        private List<ProductInput> products;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ProductInput {
            private Long productId;
            private Integer quantity;
            private String sku;
        }
    }

    public void addHistory(SagaHistory history) {
        if (this.history == null) {
            this.history = new LinkedList<>();
        }

        this.history.add(history);
    }
}