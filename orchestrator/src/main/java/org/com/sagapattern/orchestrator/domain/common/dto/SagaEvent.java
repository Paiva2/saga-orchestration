package org.com.sagapattern.orchestrator.domain.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.sagapattern.orchestrator.domain.enums.ESagaPhase;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SagaEvent {
    private String eventTransactionId;
    private String source;
    private Object payload;
    private ESagaPhase phase;
    private List<SagaHistory> history;
}