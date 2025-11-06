package org.com.sagapattern.product.domain.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.com.sagapattern.product.domain.common.dto.OrderSagaEvent;
import org.com.sagapattern.product.domain.common.exception.GenericException;
import org.springframework.stereotype.Component;

@Component
public class JsonUtils {
    private final static ObjectMapper mapper = new ObjectMapper();

    public String toJsonString(OrderSagaEvent orderSagaEvent) {
        try {
            return mapper.writeValueAsString(orderSagaEvent);
        } catch (Exception e) {
            throw new GenericException("Error while trying to convert json string!");
        }
    }

    public OrderSagaEvent fromJsonString(String message) {
        try {
            return mapper.readValue(message, OrderSagaEvent.class);
        } catch (Exception e) {
            throw new GenericException("Error while trying to convert from json string!");
        }
    }
}