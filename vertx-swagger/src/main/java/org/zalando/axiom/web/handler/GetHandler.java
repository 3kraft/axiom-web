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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
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
        Object parameter;
        try {
            if (hasDefaultConstructor(paramType)) {
                parameter = fillParameterWithDefaultConstructor(routingContext, paramType, params);
            } else {
                parameter = fillParameterWithNonDefaultConstructor(paramType, params);
            }
            R result = function.apply(paramType.cast(parameter));
            try {
                routingContext.response().end(mapper.writeValueAsString(result));
            } catch (JsonProcessingException e) {
                fail(String.format("Could not serialize result [%s]!", result.getClass().getName()), e, routingContext);
            }
        } catch (InvocationTargetException| InstantiationException | IllegalAccessException e) {
            fail("Error occurred on calling controller method!", e, routingContext);
        }
    }

    private Object fillParameterWithNonDefaultConstructor(Class<T> paramType, MultiMap params) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<?> constructor = getConstructorWithParameterCount(params.names().size(), paramType);
        List<Object> constructorValues = new LinkedList<>();
        Map<String, Parameter> parameters = getParameterMap(path);
        for (String name : params.names()) {
            Class<?> type = getType(name, parameters.get(name));
            constructorValues.add(castValueToType(getSingleValue(name, params), type));
        }
        return constructor.newInstance(constructorValues.toArray());
    }

    private Constructor<?> getConstructorWithParameterCount(int size, Class<T> paramType) {
        for (Constructor<?> constructor : paramType.getConstructors()) {
            if (constructor.getParameters().length == size) {
                return constructor;
            }
        }
        throw new IllegalStateException(String.format("Could not find constructor with [%d] arguments for parameter object [%s]!", size, paramType.getName()));
    }

    private Object fillParameterWithDefaultConstructor(RoutingContext routingContext, Class<T> paramType, MultiMap params) throws InstantiationException, IllegalAccessException {
        Object parameter = paramType.newInstance();
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        Map<String, Parameter> parameters = getParameterMap(path);
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
        return parameter;
    }

    private Map<String, Parameter> getParameterMap(Path path) {
        Map<HttpMethod, Operation> operationMap = path.getOperationMap();
        return operationMap.get(HttpMethod.GET).getParameters().stream().collect(
                Collectors.toMap(
                        Parameter::getName,
                        (Function<Parameter, Parameter>) param -> param));
    }

    private boolean hasDefaultConstructor(Class<T> paramType) {
        for (Constructor<?> constructor : paramType.getConstructors()) {
            if (constructor.getParameters().length == 0) {
                return true;
            }
        }
        return false;
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
