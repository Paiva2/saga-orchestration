package org.com.sagapattern.orchestrator.infra.saga;

import lombok.AllArgsConstructor;
import org.com.sagapattern.orchestrator.domain.common.dto.SagaEvent;
import org.com.sagapattern.orchestrator.domain.utils.JsonUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SagaProducer {
    private final JsonUtils jsonUtils;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String destinationTopic, SagaEvent event) {
        kafkaTemplate.send(destinationTopic, jsonUtils.toJsonString(event));
    }
}
