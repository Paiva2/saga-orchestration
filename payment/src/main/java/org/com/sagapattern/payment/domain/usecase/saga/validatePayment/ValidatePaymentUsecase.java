package org.com.sagapattern.payment.domain.usecase.saga.validatePayment;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.sagapattern.payment.domain.common.dto.OrderSagaEvent;
import org.com.sagapattern.payment.domain.common.dto.SagaHistory;
import org.com.sagapattern.payment.domain.common.exception.ValidationException;
import org.com.sagapattern.payment.domain.entity.EventHistory;
import org.com.sagapattern.payment.domain.entity.OrderPayment;
import org.com.sagapattern.payment.domain.enums.EOrderPaymentStatus;
import org.com.sagapattern.payment.domain.enums.EPaymentMethod;
import org.com.sagapattern.payment.domain.enums.ESagaPhase;
import org.com.sagapattern.payment.domain.utils.JsonUtils;
import org.com.sagapattern.payment.infra.persistence.repository.EventHistoryRepository;
import org.com.sagapattern.payment.infra.persistence.repository.OrderPaymentRepository;
import org.com.sagapattern.payment.infra.saga.SagaHandler;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static org.com.sagapattern.payment.domain.common.constants.SagaConstants.CURRENT_SAGA_SOURCE;
import static org.com.sagapattern.payment.domain.common.constants.SagaConstants.EVENT_SOURCE_REASON_FLOW;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class ValidatePaymentUsecase {
    private final static BigDecimal MIN_PAYMENT_VALUE = new BigDecimal("1");

    private final EventHistoryRepository eventHistoryRepository;
    private final OrderPaymentRepository orderPaymentRepository;

    private final JsonUtils jsonUtils;

    private final SagaHandler sagaHandler;

    public void execute(String input) {
        OrderSagaEvent event = parseEvent(input);
        Optional<EventHistory> eventHistory = findEventHistory(event.getEventTransactionId());

        if (eventHistory.isPresent()) {
            log.info("Event already exists on event history, discarded: {} - timestamp: {}", event.getEventTransactionId(), new Date());
            return;
        }

        String eventHistoryMessage = "";

        OrderPayment orderPayment = fillOrderPayment(event);

        EventHistory newEventHistory = EventHistory.builder()
            .eventTransactionId(event.getEventTransactionId())
            .eventSource(EVENT_SOURCE_REASON_FLOW)
            .status(ESagaPhase.SUCCESS.name())
            .build();

        try {
            validatePayload(orderPayment, event.getPayload());

            Integer installments = event.getPayload().getInstallments();
            EPaymentMethod paymentMethod = parsePaymentMethod(event.getPayload().getPaymentMethod());

            orderPayment.setPaymentMethod(paymentMethod);
            orderPayment.setInstallments(installments);
            orderPayment.setStatus(EOrderPaymentStatus.PROCESSED);

            event.setPhase(ESagaPhase.SUCCESS);
            newEventHistory.setSuccess(true);
            eventHistoryMessage = "Payment validation success!";
        } catch (Exception exception) {
            event.setPhase(ESagaPhase.ROLLBACK_PENDING);
            orderPayment.setStatus(EOrderPaymentStatus.ROLLBACK_PENDING);
            newEventHistory.setSuccess(false);

            if (isEmpty(eventHistoryMessage)) {
                eventHistoryMessage = "Error while trying to process payment validation: " + exception.getClass().getName() + " -  message: " + exception.getMessage();
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
        return eventHistoryRepository.findByEventTransactionIdAndStatus(eventTransactionId, ESagaPhase.SUCCESS.name());
    }

    private EPaymentMethod parsePaymentMethod(String paymentMethod) {
        try {
            return EPaymentMethod.valueOf(paymentMethod);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid payment method!");
        }
    }

    private OrderPayment fillOrderPayment(OrderSagaEvent event) {
        return OrderPayment.builder()
            .installments(0)
            .paymentMethod(EPaymentMethod.UNKNOWN)
            .totalItems(event.getPayload().getTotalItems())
            .totalValue(event.getPayload().getTotalValue())
            .orderId(event.getPayload().getOrderId().toString())
            .build();
    }

    private void persistOrderPayment(OrderPayment orderPayment) {
        orderPaymentRepository.save(orderPayment);
    }

    private void validatePayload(OrderPayment orderPayment, OrderSagaEvent.SagaEventPayload payload) {
        if (payload.getTotalValue().compareTo(MIN_PAYMENT_VALUE) < 0) {
            throw new ValidationException("Total value must be greater than or equal to 1!");
        }

        if (payload.getTotalItems() < 1) {
            throw new ValidationException("Total items can't be less than 1!");
        }

        EPaymentMethod paymentMethod = parsePaymentMethod(payload.getPaymentMethod());

        orderPayment.setPaymentMethod(paymentMethod);

        if ((paymentMethod.equals(EPaymentMethod.BOLETO) || paymentMethod.equals(EPaymentMethod.PIX)) && payload.getInstallments() > 0) {
            throw new ValidationException("Only credit card payments can have installments!");
        }

        if (payload.getInstallments() < 0) {
            throw new ValidationException("Installments can't be less than 1!");
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
