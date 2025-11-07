package org.com.sagapattern.order.domain.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.sagapattern.order.domain.enums.ESagaPhase;

import java.util.LinkedList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SagaEvent {
    private String eventTransactionId;
    private String source;
    private OrderSagaOutput payload;
    private ESagaPhase phase;
    private List<SagaHistory> history;

    public void addHistory(SagaHistory history) {
        if (this.history == null) {
            this.history = new LinkedList<>();
        }

        this.history.add(history);
    }
}
