package org.com.sagapattern.order.application.entrypoint.saga;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.sagapattern.order.domain.usecase.failedOrder.FailedOrderUsecase;
import org.com.sagapattern.order.domain.usecase.successOrder.SuccessOrderUsecase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@AllArgsConstructor
public class OrderSagaController {
    private final SuccessOrderUsecase successOrderUsecase;
    private final FailedOrderUsecase failedOrderUsecase;

    @KafkaListener(
        topics = "${spring.kafka.topic.order-ending-success}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void executeSuccess(String message) {
        try {
            log.info("OrderSagaController#executeSuccess: message={}", message);
            successOrderUsecase.execute(message);
        } catch (Exception e) {
            log.error("Error: OrderSagaController#executeSuccess: message={}", message, e);
            throw new RuntimeException("Error while receiving new message on saga controller!");
        }
    }

    @KafkaListener(
        topics = "${spring.kafka.topic.order-ending-fail}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void executeFailure(String message) {
        try {
            log.info("OrderSagaController#executeFailure: message={}", message);
            failedOrderUsecase.execute(message);
        } catch (Exception e) {
            log.error("Error: OrderSagaController#executeFailure: message={}", message, e);
            throw new RuntimeException("Error while receiving new message on saga controller!");
        }
    }
}
