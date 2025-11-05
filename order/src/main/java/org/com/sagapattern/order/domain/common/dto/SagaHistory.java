package org.com.sagapattern.order.domain.common.dto;

import lombok.Builder;
import org.com.sagapattern.order.domain.enums.ESagaPhase;

import java.util.Date;

@Builder
public record SagaHistory(String source, String statusMessage, ESagaPhase phase, Date timestamp) {
}
