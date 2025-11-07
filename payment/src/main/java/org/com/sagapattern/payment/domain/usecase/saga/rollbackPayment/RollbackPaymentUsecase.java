package org.com.sagapattern.payment.domain.usecase.saga.rollbackPayment;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.sagapattern.payment.domain.common.dto.OrderSagaEvent;
import org.com.sagapattern.payment.domain.common.dto.SagaHistory;
import org.com.sagapattern.payment.domain.common.exception.OrderPaymentNotFoundException;
import org.com.sagapattern.payment.domain.common.exception.ValidationException;
import org.com.sagapattern.payment.domain.entity.EventHistory;
import org.com.sagapattern.payment.domain.entity.OrderPayment;
import org.com.sagapattern.payment.domain.enums.EOrderPaymentStatus;
import org.com.sagapattern.payment.domain.enums.ESagaPhase;
import org.com.sagapattern.payment.domain.utils.JsonUtils;
import org.com.sagapattern.payment.infra.persistence.repository.EventHistoryRepository;
import org.com.sagapattern.payment.infra.persistence.repository.OrderPaymentRepository;
import org.com.sagapattern.payment.infra.saga.SagaHandler;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

import static org.com.sagapattern.payment.domain.common.constants.SagaConstants.CURRENT_SAGA_SOURCE;
import static org.com.sagapattern.payment.domain.common.constants.SagaConstants.EVENT_SOURCE_REASON_FLOW;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class RollbackPaymentUsecase {
    private final EventHistoryRepository eventHistoryRepository;
    private final OrderPaymentRepository orderPaymentRepository;

    private final JsonUtils jsonUtils;

    private final SagaHandler sagaHandler;

    public void execute(String input) {
        OrderSagaEvent event = parseEvent(input);
        Optional<EventHistory> eventHistory = findEventHistory(event.getEventTransactionId());

        if (eventHistory.isPresent() && eventHistory.get().getSuccess()) {
            log.info("Event already exists on event history, discarded: {} - timestamp: {}", event.getEventTransactionId(), new Date());
            return;
        }

        String eventHistoryMessage = "";

        OrderPayment orderPayment = findOrderPayment(event);

        EventHistory newEventHistory = eventHistory.orElseGet(() -> EventHistory.builder()
            .eventTransactionId(event.getEventTransactionId())
            .eventSource(EVENT_SOURCE_REASON_FLOW)
            .status(ESagaPhase.FAILED.name())
            .build()
        );

        try {
            validatePayload(event.getPayload());

            orderPayment.setStatus(EOrderPaymentStatus.FAILED);
            event.setPhase(ESagaPhase.FAILED);

            eventHistoryMessage = "Payment rollback success!";
        } catch (Exception exception) {
            event.setPhase(ESagaPhase.ROLLBACK_PENDING);
            orderPayment.setStatus(EOrderPaymentStatus.ROLLBACK_PENDING);

            // it would be good to check headers and validate how many times this rollback occurred to put it on a dlq

            if (isEmpty(eventHistoryMessage)) {
                eventHistoryMessage = "Error while trying to process payment rollback: " + exception.getClass().getName() + " -  message: " + exception.getMessage();
            }
        }

        persistEventHistory(newEventHistory);
        persistOrderPayment(orderPayment);
        updateHistory(event, eventHistoryMessage);

        event.setSource(CURRENT_SAGA_SOURCE);
        sagaHandler.sendSagaMessage(event);
    }

    private OrderSagaEvent parseEvent(String input) {
        return jsonUtils.fromJsonString(input);
    }

    private Optional<EventHistory> findEventHistory(String eventTransactionId) {
        return eventHistoryRepository.findByEventTransactionIdAndStatus(eventTransactionId, ESagaPhase.FAILED.name());
    }

    private OrderPayment findOrderPayment(OrderSagaEvent event) {
        return orderPaymentRepository.findByOrderId(event.getPayload().getOrderId().toString())
            .orElseThrow(() -> new OrderPaymentNotFoundException("Order payment not found!"));
    }

    private void persistOrderPayment(OrderPayment orderPayment) {
        orderPaymentRepository.save(orderPayment);
    }

    private void validatePayload(OrderSagaEvent.SagaEventPayload payload) {
        if (payload.getOrderId() == null) {
            throw new ValidationException("Order id can't be null!");
        }
    }

    private void persistEventHistory(EventHistory eventHistory) {
        eventHistoryRepository.save(eventHistory);
    }

    private void updateHistory(OrderSagaEvent event, String message) {
        SagaHistory history = SagaHistory.builder()
            .source(CURRENT_SAGA_SOURCE)
            .phase(event.getPhase())
            .timestamp(new Date())
            .statusMessage(message)
            .build();

        event.addHistory(history);
    }
}
