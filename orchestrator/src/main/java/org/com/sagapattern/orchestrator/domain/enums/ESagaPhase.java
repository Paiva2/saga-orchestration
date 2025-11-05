package org.com.sagapattern.orchestrator.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ESagaPhase {
    ORDER_STARTED("ORDER_STARTED"),

    SUCCESS("SUCCESS"),
    ROLLBACK_PENDING("ROLLBACK_PENDING"),
    FAILED("FAILED"),
    RETRY("RETRY");

    private final String phase;
}
