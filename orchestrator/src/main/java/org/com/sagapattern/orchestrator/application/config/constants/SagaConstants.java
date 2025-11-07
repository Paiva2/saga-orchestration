package org.com.sagapattern.orchestrator.application.config.constants;

import org.com.sagapattern.orchestrator.domain.enums.ESagaSources;

import static org.com.sagapattern.orchestrator.domain.enums.ESagaPhase.*;
import static org.com.sagapattern.orchestrator.domain.enums.ETopics.*;

public class SagaConstants {
    public static final int EVENT_SOURCE_INDEX = 0;
    public static final int SAGA_PHASE_INDEX = 1;
    public static final int TOPIC_INDEX = 2;

    public static final String[][] ORDER_ORCHESTRATOR_ORCHESTRATION = {
        {ESagaSources.ORCHESTRATOR.name(), ORDER_STARTED.getPhase(), PRODUCT_VALIDATION.getTopic()},
        {ESagaSources.ORCHESTRATOR.name(), FAILED.getPhase(), ORDER_ENDING_FAIL.getTopic()},

        {ESagaSources.PRODUCT_SERVICE.name(), SUCCESS.getPhase(), PAYMENT_VALIDATION.getTopic()},
        {ESagaSources.PRODUCT_SERVICE.name(), ROLLBACK_PENDING.getPhase(), PRODUCT_VALIDATION_FAILED.getTopic()},
        {ESagaSources.PRODUCT_SERVICE.name(), FAILED.getPhase(), ORDER_ENDING_FAIL.getTopic()},

        {ESagaSources.PAYMENT_SERVICE.name(), SUCCESS.getPhase(), ORDER_ENDING_SUCCESS.getTopic()},
        {ESagaSources.PAYMENT_SERVICE.name(), ROLLBACK_PENDING.getPhase(), PAYMENT_VALIDATION_FAILED.getTopic()},
        {ESagaSources.PAYMENT_SERVICE.name(), FAILED.getPhase(), PRODUCT_VALIDATION_FAILED.getTopic()}
    };
}
