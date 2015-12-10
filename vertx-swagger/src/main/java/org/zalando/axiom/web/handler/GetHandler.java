package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.parameters.AbstractParameter;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.Parameter;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.axiom.web.util.Types;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.zalando.axiom.web.util.Preconditions.checkNotNull;
import static org.zalando.axiom.web.util.Strings.getSetterName;
import static org.zalando.axiom.web.util.Types.castValueToType;

public class GetHandler<T, R> implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetWithZeroOrOneParameterHandler.class);

    private final ObjectMapper mapper;

    private final Function<T, R> function;

    private final Class<T> paramType;

    private final Path path;

    public GetHandler(ObjectMapper mapper, Function<T, R> function, Class<T> paramType, Path path) {
        checkNotNull(mapper, "Mapper must not be null!");
        checkNotNull(function, "Function must not be null!");
        checkNotNull(paramType, "ParamType must not be null!");
        checkNotNull(path, "Path must not be null!");

        this.mapper = mapper;
        this.function = function;
        this.paramType = paramType;
        this.path = path;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        MultiMap params = routingContext.request().params();
        try {
            Object parameter = paramType.newInstance();
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Map<HttpMethod, Operation> operationMap = path.getOperationMap();
            Map<String, Parameter> parameters = operationMap.get(HttpMethod.GET).getParameters().stream().collect(
                    Collectors.toMap(
                            Parameter::getName,
                            (Function<Parameter, Parameter>) param -> param));

            for (String name : params.names()) {
                try {
                    Class<?> type = getType(name, parameters.get(name));
                    MethodHandle setter = lookup.findVirtual(paramType, getSetterName(name), MethodType.methodType(void.class, type));
                    setter.invokeWithArguments(parameter, castValueToType(getSingleValue(name, params), type));

                } catch (NoSuchFieldException e) {
                    fail(String.format("Could not find setter for field [%s]", name), e, routingContext);
                } catch (Throwable throwable) {
                    fail(String.format("Could not invoke setter for field [%s]!", name), throwable, routingContext);
                }
            }
            R result = function.apply(paramType.cast(parameter));
            try {
                routingContext.response().end(mapper.writeValueAsString(result));
            } catch (JsonProcessingException e) {
                fail(String.format("Could not serialize result [%s]!", result.getClass().getName()), e, routingContext);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            fail("Error occurred on calling controller method!", e, routingContext);
        }
    }

    private void fail(String message, Throwable throwable, RoutingContext routingContext) {
        LOGGER.error(message, throwable);
        routingContext.fail(500);
    }

    private String getSingleValue(String name, MultiMap params) {
        return params.get(name);
    }

    private Class<?> getType(String name, Parameter parameter) {
        checkNotNull(parameter, String.format("Could not find parameter [%s] in swagger model!", name));
        if (parameter instanceof AbstractParameter) {
            AbstractSerializableParameter castedParam = (AbstractSerializableParameter) parameter;
            return Types.getParameterType(castedParam.getType(), castedParam.getFormat());
        } else {
            throw new UnsupportedOperationException(String.format("Parameter type [%s] not supported!", parameter.getClass().getName()));
        }
    }
}
