package org.com.sagapattern.order.infra.messaging;

import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MessagingProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String msg) {
        kafkaTemplate.send(topic, msg);
    }
}
