package org.com.sagapattern.notification.application.entrypoint.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class MessagingController {
    @KafkaListener(
        topics = "${spring.kafka.topic.notification}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void execute(String message) {
        try {
            log.info("MessagingController#execute: message={}", message);
            // send e-mail, sms, etc
        } catch (Exception e) {
            log.error("Error: MessagingController#execute: message={}", message, e);
            throw new RuntimeException("Error while receiving new message on MessagingController!");
        }
    }

    @KafkaListener(
        topics = "${spring.kafka.topic.notification-dlq}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void executeDlq(String message) {
        try {
            log.info("MessagingController#executeDlq: message={}", message);
            // send to log db, retry, etc
        } catch (Exception e) {
            log.error("Error: MessagingController#executeDlq: message={}", message, e);
            throw new RuntimeException("Error while receiving new message on MessagingController!");
        }
    }
}
