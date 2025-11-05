package org.com.sagapattern.order.infra.saga;

import lombok.RequiredArgsConstructor;
import org.com.sagapattern.order.domain.common.dto.SagaEvent;
import org.com.sagapattern.order.domain.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SagaHandler {

    @Value("${spring.kafka.topic.orchestrator}")
    private String orchestratorTopic;
    
    private final JsonUtils jsonUtils;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void startNewOrderSaga(SagaEvent event) {
        event.setEventTransactionId(generateTxId());
        kafkaTemplate.send(orchestratorTopic, jsonUtils.toJsonString(event));
    }

    private String generateTxId() {
        return MessageFormat.format("{0}_{1}", Calendar.getInstance().toInstant(), UUID.randomUUID().toString());
    }
}
