package org.com.sagapattern.order.domain.usecase.createOrderUsecase.exception;

public class ProductsNotFoundException extends RuntimeException {
    public ProductsNotFoundException(String message) {
        super(message);
    }
}
