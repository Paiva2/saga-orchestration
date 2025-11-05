package org.com.sagapattern.orchestrator.application.entrypoint.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.sagapattern.orchestrator.domain.usecase.saga.sagaHandler.SagaHandlerUsecase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SagaController {
    private final SagaHandlerUsecase sagaHandlerUsecase;

    @KafkaListener(
        topics = "${spring.kafka.topic.orchestrator}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void execute(String message) {
        try {
            log.info("SagaController#execute: message={}", message);
            sagaHandlerUsecase.execute(message);
        } catch (Exception e) {
            log.error("Error: SagaController#execute: message={}", message, e);
            throw new RuntimeException("Error while receiving new message on saga controller!");
        }
    }
}
