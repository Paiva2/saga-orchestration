package org.com.sagapattern.payment.domain.common.exception;

public class OrderPaymentNotFoundException extends GenericException {
    public OrderPaymentNotFoundException(String message) {
        super(message);
    }
}
