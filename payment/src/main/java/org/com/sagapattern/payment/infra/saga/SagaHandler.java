package org.com.sagapattern.payment.infra.saga;

import lombok.RequiredArgsConstructor;
import org.com.sagapattern.payment.domain.common.dto.OrderSagaEvent;
import org.com.sagapattern.payment.domain.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaHandler {

    @Value("${spring.kafka.topic.orchestrator}")
    private String orchestratorTopic;

    private final JsonUtils jsonUtils;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendSagaMessage(OrderSagaEvent event) {
        kafkaTemplate.send(orchestratorTopic, jsonUtils.toJsonString(event));
    }
}