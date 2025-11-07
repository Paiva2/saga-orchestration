package org.com.sagapattern.order.domain.usecase.createOrder;

import lombok.AllArgsConstructor;
import org.com.sagapattern.order.domain.common.dto.OrderSagaOutput;
import org.com.sagapattern.order.domain.common.dto.SagaEvent;
import org.com.sagapattern.order.domain.common.dto.SagaHistory;
import org.com.sagapattern.order.domain.entity.Order;
import org.com.sagapattern.order.domain.entity.OrderProduct;
import org.com.sagapattern.order.domain.entity.Product;
import org.com.sagapattern.order.domain.enums.EOrderStatus;
import org.com.sagapattern.order.domain.enums.ESagaPhase;
import org.com.sagapattern.order.domain.usecase.createOrder.dto.CreateOrderInput;
import org.com.sagapattern.order.domain.usecase.createOrder.exception.ProductsNotAvailableException;
import org.com.sagapattern.order.domain.usecase.createOrder.exception.ProductsNotFoundException;
import org.com.sagapattern.order.infra.persistence.OrderRepository;
import org.com.sagapattern.order.infra.persistence.ProductRepository;
import org.com.sagapattern.order.infra.saga.SagaHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@AllArgsConstructor
public class CreateOrderUsecase {
    private final static String SAGA_SOURCE = "ORDER_SERVICE";

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    private final SagaHandler sagaHandler;

    @Transactional
    public void execute(CreateOrderInput input) {
        List<Product> products = findOrderedProducts(input);
        HashMap<Long, BigDecimal> mapProductValues = mapProductValues(products);

        Order order = Order.builder()
            .paymentMethod(input.getPaymentMethod())
            .status(EOrderStatus.CREATED)
            .installments(input.getInstallments())
            .endCustomerEmail(input.getEndCustomerEmail())
            .orderTotal(sumTotalOrder(input, mapProductValues))
            .itemsQuantity(sumTotalItems(input))
            .build();

        List<OrderProduct> orderProducts = fillOrderProduct(input, order, products);
        order.setProducts(orderProducts);

        saveOrder(order);
        startSaga(order);
    }

    private List<Product> findOrderedProducts(CreateOrderInput input) {
        List<Product> productsFound = new ArrayList<>();
        List<Product> productsUpdated = new ArrayList<>();

        List<Long> productsIdsNotFound = new ArrayList<>();
        List<Long> productsIdsNotAvailable = new ArrayList<>();

        for (CreateOrderInput.ProductInput productInput : input.getProducts()) {
            Optional<Product> product = productRepository.findByIdAndSku(productInput.getProductId(), productInput.getSku());

            if (product.isPresent()) {
                if (product.get().getAvailable() < productInput.getQuantity()) {
                    productsIdsNotAvailable.add(productInput.getProductId());
                } else {
                    product.get().setAvailable(product.get().getAvailable() - productInput.getQuantity());
                    productsUpdated.add(product.get());
                    productsFound.add(product.get());
                }
            } else {
                productsIdsNotFound.add(productInput.getProductId());
            }
        }

        if (!productsIdsNotFound.isEmpty()) {
            throw new ProductsNotFoundException("Products with id: " + productsIdsNotFound + " not found! Check the provided id and sku.");
        }

        if (!productsIdsNotAvailable.isEmpty()) {
            throw new ProductsNotAvailableException("Products with id: " + productsIdsNotAvailable + " has no quantity available!");
        }

        productRepository.saveAll(productsUpdated);

        return productsFound;
    }

    private BigDecimal sumTotalOrder(CreateOrderInput input, HashMap<Long, BigDecimal> productValuesMap) {
        return input.getProducts().stream().reduce(new BigDecimal("0"), (acc, productInput) -> {
            BigDecimal productValue = productValuesMap.get(productInput.getProductId());

            if (productValue != null) {
                acc = acc.add(productValue.multiply(new BigDecimal(productInput.getQuantity())));
            }

            return acc;
        }, BigDecimal::add);
    }

    private HashMap<Long, BigDecimal> mapProductValues(List<Product> products) {
        HashMap<Long, BigDecimal> mapProductValues = new HashMap<>();

        for (Product product : products) {
            mapProductValues.put(product.getId(), product.getPrice());
        }

        return mapProductValues;
    }

    private List<OrderProduct> fillOrderProduct(CreateOrderInput input, Order order, List<Product> products) {
        List<OrderProduct> orderProducts = new ArrayList<>();

        for (CreateOrderInput.ProductInput productInput : input.getProducts()) {
            products.stream().filter(p -> p.getId().equals(productInput.getProductId())).findFirst()
                .ifPresent(productFound -> {
                    OrderProduct orderProduct = OrderProduct.builder()
                        .quantity(productInput.getQuantity())
                        .product(productFound)
                        .order(order)
                        .build();

                    orderProducts.add(orderProduct);
                });
        }

        return orderProducts;
    }

    private Integer sumTotalItems(CreateOrderInput input) {
        return input.getProducts().stream().mapToInt(CreateOrderInput.ProductInput::getQuantity).sum();
    }

    private void saveOrder(Order order) {
        orderRepository.save(order);
    }

    private void startSaga(Order order) {
        SagaEvent sagaEvent = SagaEvent.builder()
            .source(SAGA_SOURCE)
            .phase(ESagaPhase.ORDER_STARTED)
            .payload(mountOutput(order))
            .build();

        sagaEvent.addHistory(fillSagaHistory());

        sagaHandler.startNewOrderSaga(sagaEvent);
    }

    private OrderSagaOutput mountOutput(Order order) {
        return OrderSagaOutput.builder()
            .orderId(order.getId())
            .totalValue(order.getOrderTotal())
            .totalItems(order.getItemsQuantity())
            .paymentMethod(order.getPaymentMethod())
            .installments(order.getInstallments())
            .products(order.getProducts().stream().map(op -> OrderSagaOutput.ProductInput.builder()
                    .productId(op.getProduct().getId())
                    .quantity(op.getQuantity())
                    .sku(op.getProduct().getSku())
                    .build()
                ).toList()
            ).build();
    }

    private SagaHistory fillSagaHistory() {
        return SagaHistory.builder()
            .source(SAGA_SOURCE)
            .phase(ESagaPhase.ORDER_STARTED)
            .statusMessage("Saga initialized")
            .timestamp(new Date())
            .build();
    }
}
