package org.com.sagapattern.order.domain.usecase.createOrder.exception;

public class ProductsNotFoundException extends RuntimeException {
    public ProductsNotFoundException(String message) {
        super(message);
    }
}
