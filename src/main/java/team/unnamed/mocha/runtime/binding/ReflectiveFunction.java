/*
 * This file is part of mocha, licensed under the MIT license
 *
 * Copyright (c) 2021-2025 Unnamed Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package team.unnamed.mocha.runtime.binding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.runtime.ExecutionContext;
import team.unnamed.mocha.runtime.JavaTypes;
import team.unnamed.mocha.runtime.value.ArrayValue;
import team.unnamed.mocha.runtime.value.Function;
import team.unnamed.mocha.runtime.value.JavaValue;
import team.unnamed.mocha.runtime.value.NumberValue;
import team.unnamed.mocha.runtime.value.StringValue;
import team.unnamed.mocha.runtime.value.Value;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

final class ReflectiveFunction<T> implements Function<T> {
    private final Object object;
    private final Method method;

    ReflectiveFunction(final @Nullable Object object, final @NotNull Method method) {
        this.object = object;
        this.method = requireNonNull(method, "method");
    }

    static @NotNull Value of(final @Nullable Object any) {
        if (any instanceof Value) {
            return (Value) any;
        } else if (any instanceof Number) {
            return NumberValue.of(((Number) any).floatValue());
        } else if (any instanceof String) {
            return StringValue.of((String) any);
        } else if (any instanceof Boolean) {
            return (Boolean) any ? NumberValue.one() : NumberValue.zero();
        } else {
            if (any != null && any.getClass().isArray()) {
                // array types
                final int length = Array.getLength(any);
                final Value[] values = new Value[length];
                for (int i = 0; i < length; i++) {
                    values[i] = of(Array.get(any, i));
                }
                return ArrayValue.of(values);
            } else if (any != null) {
                // small change here, use javaValue
                return new JavaValue(any);
            } else {
                return Value.nil();
            }
        }
    }

    @Override
    public @NotNull Value evaluate(final @NotNull ExecutionContext<T> context, final @NotNull Arguments arguments) {
        final Parameter[] parameters = method.getParameters();
        final Type[] genericParameterTypes = method.getGenericParameterTypes();

        final Object[] values = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            Class<?> parameterType = parameter.getType();
            final Value value;

            if (i == parameters.length - 1 && method.isVarArgs()) {
                // varargs
                final Class<?> componentType = parameterType.getComponentType();
                final List<Value> varArgsValues = new ArrayList<>();
                while (true) {
                    final Argument argument = arguments.next();
                    if (argument.expression() == null) {
                        break;
                    }
                    final Value object = argument.eval();
                    if (componentType.isInstance(object)) {
                        varArgsValues.add(object);
                    } else {
                        varArgsValues.add(null);
                    }
                }
                value = ArrayValue.of(varArgsValues.toArray(size -> (Value[]) Array.newInstance(componentType, size)));
            } else if (parameterType == Lazy.class) {
                // use the Lazy<T> argument as type, and pass the argument
                final Type genericParameterType = genericParameterTypes[i];
                if (!(genericParameterType instanceof ParameterizedType)) {
                    throw new IllegalArgumentException(
                            "Lazy<T> parameter must be a parameterized type."
                    );
                }
                parameterType = (Class<?>) ((ParameterizedType) genericParameterType)
                        .getActualTypeArguments()[0];
                if (parameterType == ExecutionContext.class) {
                    value = new JavaValue((Lazy<ExecutionContext<T>>) () -> context);
                } else {
                    final Class<?> argumentType = parameterType;
                    final Argument argument = arguments.next();
                    value = new JavaValue((Lazy<?>) () -> {
                        final Object object = argument.eval();
                        if (argumentType.isInstance(object)) {
                            return object;
                        } else {
                            return null;
                        }
                    });
                }
            } else if (parameterType == ExecutionContext.class) {
                value = new JavaValue(context);
            } else if (parameter.isAnnotationPresent(Entity.class)) {
                value = new JavaValue(context.entity());
            } else {
                final Argument argument = arguments.next();
                value = argument.eval();
            }

            if (value == null) {
                values[i] = JavaTypes.getNullValueForType(parameterType);
            } else if (parameter.isAnnotationPresent(Entity.class)) {
                values[i] = ((JavaValue) value).value();
            } else {
                values[i] = JavaTypes.convert(value, parameterType);
            }
        }

        try {
            return of(method.invoke(object, values));
        } catch (final Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
