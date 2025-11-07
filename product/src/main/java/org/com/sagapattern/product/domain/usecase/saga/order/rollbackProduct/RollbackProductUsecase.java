package org.com.sagapattern.product.domain.usecase.saga.order.rollbackProduct;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.sagapattern.product.domain.common.dto.OrderSagaEvent;
import org.com.sagapattern.product.domain.common.dto.SagaHistory;
import org.com.sagapattern.product.domain.common.exception.ProductAvailabilityException;
import org.com.sagapattern.product.domain.common.exception.ProductNotFoundException;
import org.com.sagapattern.product.domain.entity.EventHistory;
import org.com.sagapattern.product.domain.entity.Product;
import org.com.sagapattern.product.domain.enums.ESagaPhase;
import org.com.sagapattern.product.domain.utils.JsonUtils;
import org.com.sagapattern.product.infra.persistence.repository.EventHistoryRepository;
import org.com.sagapattern.product.infra.persistence.repository.ProductRepository;
import org.com.sagapattern.product.infra.saga.SagaHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.com.sagapattern.product.domain.common.constants.SagaConstants.CURRENT_SAGA_SOURCE;
import static org.com.sagapattern.product.domain.common.constants.SagaConstants.EVENT_SOURCE_REASON_FLOW;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class RollbackProductUsecase {
    private final ProductRepository productRepository;
    private final EventHistoryRepository eventHistoryRepository;

    private final JsonUtils jsonUtils;

    private final SagaHandler sagaHandler;

    @Transactional
    public void execute(String input) {
        OrderSagaEvent event = jsonUtils.fromJsonString(input);
        Optional<EventHistory> eventHistory = findEventHistory(event.getEventTransactionId());

        if (eventHistory.isPresent() && eventHistory.get().getSuccess()) {
            log.info("Event already exists on event history, discarded: {} - timestamp: {}", event.getEventTransactionId(), new Date());
            return;
        }

        String eventHistoryMessage = "";

        EventHistory newEventHistory = eventHistory.orElseGet(() -> EventHistory.builder()
            .eventTransactionId(event.getEventTransactionId())
            .eventSource(EVENT_SOURCE_REASON_FLOW)
            .status(ESagaPhase.FAILED.name())
            .build()
        );

        try {
            OrderSagaEvent.SagaEventPayload payload = event.getPayload();

            rollbackProductsQuantity(payload, event.getHistory().get(event.getHistory().size() - 1).statusMessage());

            event.setPhase(ESagaPhase.FAILED);

            eventHistoryMessage = "Product rollback success!";
            newEventHistory.setSuccess(true);
        } catch (Exception exception) {
            event.setPhase(ESagaPhase.ROLLBACK_PENDING); // retry - must check headers execution qtt and send it to a dlq if necessary

            if (isEmpty(eventHistoryMessage)) {
                eventHistoryMessage = "Error while trying to process product rollback: " + exception.getClass().getName() + " -  message: " + exception.getMessage();
            }

            newEventHistory.setSuccess(false);
        }

        event.setSource(CURRENT_SAGA_SOURCE);
        updateHistory(event, eventHistoryMessage);
        persistEventHistory(newEventHistory);
        sagaHandler.sendSagaMessage(event);
    }

    private void rollbackProductsQuantity(OrderSagaEvent.SagaEventPayload payload, String eventMessage) {
        List<Product> products = productRepository.findByIdAndSkuForUpdate(payload.getProducts().stream().map(OrderSagaEvent.SagaEventPayload.ProductInput::getProductId).toList());

        for (Product product : products) {
            Optional<OrderSagaEvent.SagaEventPayload.ProductInput> productInput = payload.getProducts().stream()
                .filter(productOrdered -> productOrdered.getProductId().equals(product.getId()) && productOrdered.getSku().equals(product.getSku()))
                .findFirst();

            if (productInput.isEmpty()) {
                throw new ProductNotFoundException("Product " + product.getId() + "not found!");
            }

            boolean mustRollbackQuantity = !eventMessage.contains(ProductNotFoundException.class.getName()) && !eventMessage.contains(ProductAvailabilityException.class.getName());

            if (mustRollbackQuantity) {
                product.setAvailable(product.getAvailable() + productInput.get().getQuantity());
            }
        }

        if (!products.isEmpty()) {
            productRepository.saveAll(products);
        }
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

    private void persistEventHistory(EventHistory eventHistory) {
        eventHistoryRepository.save(eventHistory);
    }

    private Optional<EventHistory> findEventHistory(String eventTransactionId) {
        return eventHistoryRepository.findByEventTransactionIdAndStatus(eventTransactionId, ESagaPhase.FAILED.name());
    }
}
