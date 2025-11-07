package org.com.sagapattern.order.domain.usecase.successOrder;

import lombok.RequiredArgsConstructor;
import org.com.sagapattern.order.domain.common.dto.MessagingEvent;
import org.com.sagapattern.order.domain.common.dto.NotificationMessagingOutput;
import org.com.sagapattern.order.domain.common.dto.SagaEvent;
import org.com.sagapattern.order.domain.common.exception.OrderNotFoundException;
import org.com.sagapattern.order.domain.entity.Order;
import org.com.sagapattern.order.domain.enums.EOrderStatus;
import org.com.sagapattern.order.domain.utils.JsonUtils;
import org.com.sagapattern.order.infra.messaging.MessagingProducer;
import org.com.sagapattern.order.infra.persistence.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuccessOrderUsecase {
    private final static String MESSAGING_SOURCE = "ORDER_SERVICE";

    @Value("${spring.kafka.topic.notification}")
    private String notificationTopic;

    private final OrderRepository orderRepository;

    private final JsonUtils jsonUtils;

    private final MessagingProducer messagingProducer;

    public void execute(String message) {
        SagaEvent sagaEvent = jsonUtils.fromJsonString(message);

        Order order = findOrder(sagaEvent.getPayload().getOrderId());
        order.setStatus(EOrderStatus.SUCCESS);

        persistOrder(order);

        sendSuccessNotification(order);
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found!"));
    }

    public String generateTxId() {
        return MessageFormat.format("{0}_{1}", Calendar.getInstance().toInstant(), UUID.randomUUID().toString());
    }

    private void sendSuccessNotification(Order order) {
        MessagingEvent messagingEvent = MessagingEvent.builder()
            .eventTransactionId(generateTxId())
            .source(MESSAGING_SOURCE)
            .payload(NotificationMessagingOutput.builder()
                .orderId(order.getId())
                .totalValue(order.getOrderTotal())
                .totalItems(order.getItemsQuantity())
                .paymentMethod(order.getPaymentMethod())
                .installments(order.getInstallments())
                .customerEmail(order.getEndCustomerEmail())
                .products(order.getProducts().stream().map(NotificationMessagingOutput.ProductInput::new).toList())
                .build()
            ).build();

        messagingProducer.send(notificationTopic, jsonUtils.toJsonString(messagingEvent));
    }

    private void persistOrder(Order order) {
        orderRepository.save(order);
    }
}
