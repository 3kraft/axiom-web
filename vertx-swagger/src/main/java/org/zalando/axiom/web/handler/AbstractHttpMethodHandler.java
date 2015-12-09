package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.zalando.axiom.web.domain.OperationTarget;

public abstract class AbstractHttpMethodHandler implements Handler<RoutingContext> {

    protected final ObjectMapper mapper;

    protected final OperationTarget operationTarget;

    public AbstractHttpMethodHandler(ObjectMapper mapper, OperationTarget operationTarget) {
        this.mapper = mapper;
        this.operationTarget = operationTarget;
    }
}
