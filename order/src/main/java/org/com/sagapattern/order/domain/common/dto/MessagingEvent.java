package org.com.sagapattern.order.domain.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagingEvent {
    private String eventTransactionId;
    private String source;
    private Object payload;
}
