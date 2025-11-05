package org.com.sagapattern.orchestrator.domain.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.sagapattern.orchestrator.domain.enums.ESagaPhase;

import java.util.LinkedList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaEvent {
    private String eventTransactionId;
    private String source;
    private Object payload;
    private ESagaPhase phase;
    private List<SagaHistory> history;

    public void addHistory(SagaHistory history) {
        if (this.history == null) {
            this.history = new LinkedList<>();
        }

        this.history.add(history);
    }
}