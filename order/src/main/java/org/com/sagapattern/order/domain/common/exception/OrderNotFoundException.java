package org.com.sagapattern.order.domain.common.exception;

public class OrderNotFoundException extends GenericException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
