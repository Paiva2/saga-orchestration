package org.com.sagapattern.product.domain.common.exception;

public class ProductNotFoundException extends GenericException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
