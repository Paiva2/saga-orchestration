package org.com.sagapattern.product.application.entrypoint.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.sagapattern.product.domain.usecase.saga.order.rollbackProduct.RollbackProductUsecase;
import org.com.sagapattern.product.domain.usecase.saga.order.validateProduct.ValidateProductUsecase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OrderSagaController {
    private final ValidateProductUsecase validateProductUsecase;
    private final RollbackProductUsecase rollbackProductUsecase;

    @KafkaListener(
        topics = "${spring.kafka.topic.product-validation}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void executeValidation(String message) {
        try {
            log.info("OrderSagaController#executeValidation: message={}", message);
            validateProductUsecase.execute(message);
        } catch (Exception e) {
            log.error("Error: OrderSagaController#executeValidation: message={}", message, e);
            throw new RuntimeException("Error while receiving new message on saga controller!");
        }
    }

    @KafkaListener(
        topics = "${spring.kafka.topic.product-validation-failed}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void executeRollback(String message) {
        try {
            log.info("OrderSagaController#executeRollback: message={}", message);
        } catch (Exception e) {
            log.error("Error: OrderSagaController#executeRollback: message={}", message, e);
            throw new RuntimeException("Error while receiving new message on saga controller!");
        }
    }
}
