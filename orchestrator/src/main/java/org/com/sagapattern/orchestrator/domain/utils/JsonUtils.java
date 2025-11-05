package org.com.sagapattern.orchestrator.domain.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.com.sagapattern.orchestrator.domain.common.dto.SagaEvent;
import org.com.sagapattern.orchestrator.domain.common.exception.GenericException;
import org.springframework.stereotype.Component;

@Component
public class JsonUtils {
    private final static ObjectMapper mapper = new ObjectMapper();

    public String toJsonString(SagaEvent sagaEvent) {
        try {
            return mapper.writeValueAsString(sagaEvent);
        } catch (Exception e) {
            throw new GenericException("Error while trying to convert json string!");
        }
    }

    public SagaEvent fromJsonString(String message) {
        try {
            return mapper.readValue(message, SagaEvent.class);
        } catch (Exception e) {
            throw new GenericException("Error while trying to convert from json string!");
        }
    }
}
