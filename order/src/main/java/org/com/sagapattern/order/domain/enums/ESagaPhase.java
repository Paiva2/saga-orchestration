package org.com.sagapattern.order.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ESagaPhase {
    ORDER_STARTED("ORDER_STARTED"),

    SUCCESS("SUCCESS"),
    ROLLBACK_PENDING("ROLLBACK_PENDING"),
    FAILED("FAILED"),
    RETRY("RETRY"),

    ORDER_ENDING_SUCCESS("ORDER_ENDING_SUCCESS"),
    ORDER_ENDING_FAIL("ORDER_ENDING_FAIL");

    private final String phase;
}
