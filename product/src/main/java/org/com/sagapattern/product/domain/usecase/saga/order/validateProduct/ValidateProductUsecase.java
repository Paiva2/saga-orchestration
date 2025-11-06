package org.com.sagapattern.product.domain.usecase.saga.order.validateProduct;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.sagapattern.product.domain.common.dto.OrderSagaEvent;
import org.com.sagapattern.product.domain.common.dto.SagaHistory;
import org.com.sagapattern.product.domain.common.exception.ProductAvailabilityException;
import org.com.sagapattern.product.domain.common.exception.ProductNotFoundException;
import org.com.sagapattern.product.domain.common.exception.ValidationException;
import org.com.sagapattern.product.domain.entity.EventHistory;
import org.com.sagapattern.product.domain.entity.Product;
import org.com.sagapattern.product.domain.enums.ESagaPhase;
import org.com.sagapattern.product.domain.utils.JsonUtils;
import org.com.sagapattern.product.infra.persistence.repository.EventHistoryRepository;
import org.com.sagapattern.product.infra.persistence.repository.ProductRepository;
import org.com.sagapattern.product.infra.saga.SagaHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;

@Service
@Slf4j
@AllArgsConstructor
public class ValidateProductUsecase {
    private final static String EVENT_SOURCE_REASON_FLOW = "ORDER_SAGA";
    private final static String CURRENT_SAGA_SOURCE = "PRODUCT_SERVICE";

    private final ProductRepository productRepository;
    private final EventHistoryRepository eventHistoryRepository;

    private final SagaHandler sagaHandler;
    private final JsonUtils jsonUtils;

    public void execute(String input) {
        OrderSagaEvent event = jsonUtils.fromJsonString(input); // it would be nice to treat that in a separated method to handle parsing problems (send to a dlq etc)
        Optional<EventHistory> eventHistory = findEventHistory(event.getEventTransactionId());

        if (eventHistory.isPresent()) {
            log.info("Event already exists on event history, discarded: {} - timestamp: {}", event.getEventTransactionId(), new Date());
            return;
        }

        String eventHistoryMessage = "";

        EventHistory newEventHistory = EventHistory.builder()
            .eventTransactionId(event.getEventTransactionId())
            .eventSource(EVENT_SOURCE_REASON_FLOW)
            .build();

        try {
            OrderSagaEvent.SagaEventPayload payload = event.getPayload();

            validateEventPayloadFormat(payload);
            handleOrderedProducts(payload);

            event.setPhase(ESagaPhase.SUCCESS);

            eventHistoryMessage = "Product validation success!";
            newEventHistory.setSuccess(true);
        } catch (Exception exception) {
            event.setPhase(ESagaPhase.ROLLBACK_PENDING);

            if (isEmpty(eventHistoryMessage)) {
                eventHistoryMessage = "Error while trying to process product validation: " + exception.getClass().getName() + " -  message: " + exception.getMessage();
            }

            newEventHistory.setSuccess(false);
        }

        event.setSource(CURRENT_SAGA_SOURCE);
        updateHistory(event, eventHistoryMessage);
        persistEventHistory(newEventHistory);
        sagaHandler.sendSagaMessage(event);
    }

    private Optional<EventHistory> findEventHistory(String eventTransactionId) {
        return eventHistoryRepository.findByEventTransactionId(eventTransactionId);
    }

    private void validateEventPayloadFormat(OrderSagaEvent.SagaEventPayload payload) {
        if (payload.getProducts() == null || payload.getProducts().isEmpty()) {
            throw new ValidationException("Products list can't be null or empty");
        }

        List<OrderSagaEvent.SagaEventPayload.ProductInput> productsWithInvalidQuantity = payload.getProducts().stream().filter(product -> product.getQuantity() == null || product.getQuantity() < 1).toList();

        if (!productsWithInvalidQuantity.isEmpty()) {
            throw new ValidationException("Products with invalid quantity can't be null or less than 1! Ids: " + productsWithInvalidQuantity);
        }

        List<OrderSagaEvent.SagaEventPayload.ProductInput> productsWithInvalidIdOrSku = payload.getProducts().stream().filter(product -> product.getProductId() == null || product.getSku() == null).toList();

        if (!productsWithInvalidIdOrSku.isEmpty()) {
            throw new ValidationException("Products with invalid id or sku. Can't be null or empty! Ids: " + productsWithInvalidIdOrSku);
        }
    }

    private void handleOrderedProducts(OrderSagaEvent.SagaEventPayload payload) {
        List<Product> productsUpdated = new ArrayList<>();

        for (OrderSagaEvent.SagaEventPayload.ProductInput product : payload.getProducts()) {
            Optional<Product> productOrdered = productRepository.findByIdAndSku(product.getProductId(), product.getSku());

            if (productOrdered.isEmpty()) {
                throw new ProductNotFoundException("Product " + product.getProductId() + "not found!");
            }

            if (productOrdered.get().getAvailable() < product.getQuantity()) {
                throw new ProductAvailabilityException("Product " + product.getProductId() + " has no quantity available! Available: " + productOrdered.get().getAvailable());
            } else {
                productOrdered.get().setAvailable(productOrdered.get().getAvailable() - product.getQuantity());
                productsUpdated.add(productOrdered.get());
            }
        }

        if (!productsUpdated.isEmpty()) {
            productRepository.saveAll(productsUpdated);
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
}
