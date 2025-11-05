package org.com.sagapattern.orchestrator.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ETopics {
    ORCHESTRATOR("orchestrator"),
    ORDER_ENDING_SUCCESS("order-ending-success"),
    ORDER_ENDING_FAIL("order-ending-fail"),

    PRODUCT_VALIDATION("product-validation"),
    PRODUCT_VALIDATION_FAILED("product-validation-failed"),

    PAYMENT_VALIDATION("payment-validation"),
    PAYMENT_VALIDATION_FAILED("payment-validation-failed"),

    NOTIFICATION("notification"),
    NOTIFICATION_DLQ("notification-dlq");

    private final String topic;
}
