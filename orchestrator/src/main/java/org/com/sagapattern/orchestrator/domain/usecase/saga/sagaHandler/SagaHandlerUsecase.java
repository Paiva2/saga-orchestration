package org.com.sagapattern.orchestrator.domain.usecase.saga.sagaHandler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.sagapattern.orchestrator.application.config.constants.SagaConstants;
import org.com.sagapattern.orchestrator.domain.common.dto.SagaEvent;
import org.com.sagapattern.orchestrator.domain.common.exception.GenericException;
import org.com.sagapattern.orchestrator.domain.enums.ESagaPhase;
import org.com.sagapattern.orchestrator.domain.enums.ESagaSources;
import org.com.sagapattern.orchestrator.domain.enums.ETopics;
import org.com.sagapattern.orchestrator.domain.utils.JsonUtils;
import org.com.sagapattern.orchestrator.infra.saga.SagaProducer;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;

import static org.com.sagapattern.orchestrator.application.config.constants.SagaConstants.*;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
@Slf4j
@AllArgsConstructor
public class SagaHandlerUsecase {
    private final JsonUtils jsonUtils;

    private final SagaProducer sagaProducer;

    public void execute(String message) {
        SagaEvent event = parseEvent(message);
        String destinationTopic;

        try {
            if (event.getPhase().getPhase().equals(ESagaPhase.ORDER_STARTED.name())) {
                event.setSource(ESagaSources.ORCHESTRATOR.name());
                event.setPhase(ESagaPhase.ORDER_STARTED);

                if (isEmpty(event.getEventTransactionId())) {
                    event.setEventTransactionId(generateTxId());
                }

                destinationTopic = ETopics.PRODUCT_VALIDATION.getTopic();
            } else {
                String currentEventSource = event.getSource();
                String currentPhase = event.getPhase().getPhase();

                String[] destinationFlow = findDestinationFlow(currentEventSource, currentPhase);

                destinationTopic = destinationFlow[TOPIC_INDEX];
            }
        } catch (Exception e) {
            log.error("Error while trying to handle new event: {}", e.getMessage());
            event.setSource(ESagaSources.ORCHESTRATOR.name());
            event.setPhase(ESagaPhase.FAILED);

            // bom ver depois pq nao faz sentido dar ending fail do nada se falhar no meio do processo
            destinationTopic = ETopics.ORDER_ENDING_FAIL.getTopic();
        }

        sagaProducer.sendMessage(destinationTopic, event);
    }

    private SagaEvent parseEvent(String message) {
        return jsonUtils.fromJsonString(message);
    }

    private String[] findDestinationFlow(String currentSource, String currentPhase) {
        return Arrays.stream(SagaConstants.ORDER_ORCHESTRATOR_ORCHESTRATION).filter(flow ->
                flow[EVENT_SOURCE_INDEX].equals(currentSource) && flow[SAGA_PHASE_INDEX].equals(currentPhase)
            ).findFirst()
            .orElseThrow(() -> new GenericException("Flow not found!"));
    }

    public String generateTxId() {
        return MessageFormat.format("{0}_{1}", Calendar.getInstance().toInstant(), UUID.randomUUID().toString());
    }
}
