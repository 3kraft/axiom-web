package org.zalando.axiom.web.domain;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class OperationTarget {

    private final HttpMethod httpMethod;

    private final Operation operation;

    private final Object target;

    private final Method targetMethod;

    private final MethodHandle targetMethodHandle;

    private final Map<String, Parameter> parametersByName;

    public OperationTarget(HttpMethod httpMethod, Operation operation, Object target, Method targetMethod, MethodHandle targetMethodHandle) {
        this.httpMethod = httpMethod;
        this.operation = operation;
        this.target = target;
        this.targetMethod = targetMethod;
        this.targetMethodHandle = targetMethodHandle;
        this.parametersByName = getParametersByName(targetMethod.getParameters());
    }

    public HttpMethod getHttpMethod() { return httpMethod; }

    public io.vertx.core.http.HttpMethod getVertxHttpMethod() { return io.vertx.core.http.HttpMethod.valueOf(httpMethod.name()); }

    public Operation getOperation() {
        return operation;
    }

    public Object getTarget() {
        return target;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public MethodHandle getTargetMethodHandle() {
        return targetMethodHandle;
    }

    public Class<?> getParameterType(String name) {
        return parametersByName.get(name).getType();
    }

    private Map<String, Parameter> getParametersByName(Parameter[] parameters) {
        Map<String, Parameter> result = new HashMap<>();

        // if the size is 1 we don't care about the name, however if it is bigger,
        // then we need the @PathParam annotation to resolve the name
        if (parameters.length == 1) {
            result.put("arg0", parameters[0]);
        } else {
            for (Parameter parameter : parameters) {
                PathParam annotation = parameter.getAnnotation(PathParam.class);
                if (annotation == null) {
                    throw new IllegalStateException("The PathParam annotation must be set, if there is more than one parameter!");
                }
                String name = annotation.value();
                if (result.get(name) != null) {
                    throw new IllegalStateException(String.format("Name [%s] already exists! Check the PathParam annotations in controller [%s].", name, target.getClass().getName()));
                }
                result.put(name, parameter);
            }
        }

        return result;
    }
}
