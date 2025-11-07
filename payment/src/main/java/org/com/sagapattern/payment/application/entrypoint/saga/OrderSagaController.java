package org.com.sagapattern.payment.application.entrypoint.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.sagapattern.payment.domain.usecase.saga.rollbackPayment.RollbackPaymentUsecase;
import org.com.sagapattern.payment.domain.usecase.saga.validatePayment.ValidatePaymentUsecase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OrderSagaController {
    private final ValidatePaymentUsecase validatePaymentUsecase;
    private final RollbackPaymentUsecase rollbackPaymentUsecase;

    @KafkaListener(
        topics = "${spring.kafka.topic.payment-validation}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void executeValidation(String message) {
        try {
            log.info("OrderSagaController#executeValidation: message={}", message);
            validatePaymentUsecase.execute(message);
        } catch (Exception e) {
            log.error("Error: OrderSagaController#executeValidation: message={}", message, e);
            throw new RuntimeException("Error while receiving new message on saga controller!");
        }
    }

    @KafkaListener(
        topics = "${spring.kafka.topic.payment-validation-failed}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void executeRollback(String message) {
        try {
            log.info("OrderSagaController#executeRollback: message={}", message);
            rollbackPaymentUsecase.execute(message);
        } catch (Exception e) {
            log.error("Error: OrderSagaController#executeRollback: message={}", message, e);
            throw new RuntimeException("Error while receiving new message on saga controller!");
        }
    }
}