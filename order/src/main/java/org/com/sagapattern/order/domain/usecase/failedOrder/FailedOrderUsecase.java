package org.com.sagapattern.order.domain.usecase.failedOrder;

import lombok.RequiredArgsConstructor;
import org.com.sagapattern.order.domain.common.dto.MessagingEvent;
import org.com.sagapattern.order.domain.common.dto.NotificationMessagingOutput;
import org.com.sagapattern.order.domain.common.dto.SagaEvent;
import org.com.sagapattern.order.domain.common.exception.OrderNotFoundException;
import org.com.sagapattern.order.domain.entity.Order;
import org.com.sagapattern.order.domain.entity.OrderProduct;
import org.com.sagapattern.order.domain.entity.Product;
import org.com.sagapattern.order.domain.enums.EOrderStatus;
import org.com.sagapattern.order.domain.usecase.createOrder.exception.ProductsNotFoundException;
import org.com.sagapattern.order.domain.utils.JsonUtils;
import org.com.sagapattern.order.infra.messaging.MessagingProducer;
import org.com.sagapattern.order.infra.persistence.OrderRepository;
import org.com.sagapattern.order.infra.persistence.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FailedOrderUsecase {
    private final static String MESSAGING_SOURCE = "ORDER_SERVICE";

    @Value("${spring.kafka.topic.notification}")
    private String notificationTopic;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    private final JsonUtils jsonUtils;

    private final MessagingProducer messagingProducer;

    @Transactional
    public void execute(String message) {
        SagaEvent sagaEvent = jsonUtils.fromJsonString(message);

        Order order = findOrder(sagaEvent.getPayload().getOrderId());

        List<OrderProduct> orderProducts = order.getProducts();
        rollBackProductQuantity(orderProducts);

        order.setStatus(EOrderStatus.FAILED);
        persistOrder(order);

        sendFailedOrderNotification(order);
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found!"));
    }

    private void rollBackProductQuantity(List<OrderProduct> orderProducts) {
        List<Product> products = productRepository.findByIdForUpdate(orderProducts.stream().map(op -> op.getProduct().getId()).toList());

        for (Product product : products) {
            Optional<OrderProduct> orderProduct = orderProducts.stream().filter(op -> op.getProduct().getId().equals(product.getId()) && op.getProduct().getSku().equals(product.getSku())).findFirst();

            if (orderProduct.isEmpty()) {
                throw new ProductsNotFoundException("Product not found! Id: " + product.getId());
            }

            product.setAvailable(product.getAvailable() + orderProduct.get().getQuantity());
        }

        productRepository.saveAll(products);
    }

    private void persistOrder(Order order) {
        orderRepository.save(order);
    }

    public String generateTxId() {
        return MessageFormat.format("{0}_{1}", Calendar.getInstance().toInstant(), UUID.randomUUID().toString());
    }

    private void sendFailedOrderNotification(Order order) {
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
}
