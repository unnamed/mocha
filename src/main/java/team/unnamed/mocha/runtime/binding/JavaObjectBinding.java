/*
 * This file is part of mocha, licensed under the MIT license
 *
 * Copyright (c) 2021-2023 Unnamed Team
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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.runtime.value.Function;
import team.unnamed.mocha.runtime.value.ObjectProperty;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;
import team.unnamed.mocha.util.CaseInsensitiveStringHashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@ApiStatus.Internal
public final class JavaObjectBinding implements ObjectValue {
    private final String[] names;
    private final Map<String, Object> entries = new CaseInsensitiveStringHashMap<>();

    JavaObjectBinding(final @NotNull String @NotNull [] names) {
        this.names = requireNonNull(names, "names");
    }

    private static <T extends Value> T getBacking(final @Nullable Map<String, ObjectProperty> backingProperties, final @NotNull String functionName, final Class<T> valueType) {
        if (backingProperties != null) {
            final ObjectProperty property = backingProperties.get(functionName);
            if (property != null && valueType.isInstance(property.value())) {
                return valueType.cast(property.value());
            }
        }
        return null;
    }

    public static <T> @NotNull JavaObjectBinding of(final @NotNull Class<T> clazz, final @Nullable T instance, final @Nullable ObjectValue backingObject) {
        final Binding binding = clazz.getDeclaredAnnotation(Binding.class);
        if (binding == null) {
            throw new IllegalArgumentException("Given " + clazz + " is not annotated with @Binding");
        }

        final JavaObjectBinding object = new JavaObjectBinding(binding.value());
        final Map<String, ObjectProperty> backingProperties = backingObject != null ? backingObject.entries() : null;

        {
            // check external bindings
            final BindExternalFunction.Multiple annotation = clazz.getDeclaredAnnotation(BindExternalFunction.Multiple.class);
            if (annotation != null) {
                for (final BindExternalFunction externalFunctionBinding : annotation.value()) {
                    final Class<?> atClass = externalFunctionBinding.at();
                    final String methodName = externalFunctionBinding.name();
                    final Class<?>[] parameterTypes = externalFunctionBinding.args();

                    final Method method;
                    try {
                        method = atClass.getDeclaredMethod(methodName, parameterTypes);
                    } catch (final NoSuchMethodException e) {
                        throw new IllegalArgumentException("No method found with name " + methodName
                                + " and parameter types " + Arrays.toString(parameterTypes) + ". Declared as"
                                + " external binding for " + clazz, e);
                    }

                    final String functionName;
                    {
                        final String alias = externalFunctionBinding.as();
                        if (alias.isEmpty()) {
                            functionName = methodName;
                        } else {
                            functionName = alias;
                        }
                    }

                    final Function backing = getBacking(backingProperties, functionName, Function.class);
                    final boolean pure = externalFunctionBinding.pure();

                    if (backing != null && backing.pure() != pure) {
                        throw new IllegalStateException("Different 'pure' values for interface and Java functions for function " + functionName);
                    }

                    object.entries.put(functionName, new JavaFunction<>(instance, method, backing, pure));
                }
            }
        }

        for (final Field field : clazz.getDeclaredFields()) {
            final Binding annotation = field.getDeclaredAnnotation(Binding.class);
            if (annotation == null) {
                continue;
            }

            if (instance == null) {
                // bind static only
                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
            } else {
                // bind non-static only
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
            }

            final String[] propertyNames = annotation.value();
            if (propertyNames.length < 1) {
                throw new IllegalArgumentException("No property names declared for field " + field);
            }
            final Value backingValue = getBacking(backingProperties, propertyNames[0], Value.class);
            final JavaFieldBinding fieldBinding = new JavaFieldBinding(instance, field, backingValue == null ? null : () -> backingValue);
            for (final String propertyName : propertyNames) {
                object.entries.put(propertyName, fieldBinding);
            }
        }

        for (final Method method : clazz.getDeclaredMethods()) {
            final Binding annotation = method.getDeclaredAnnotation(Binding.class);
            if (annotation == null) {
                continue;
            }

            if (method.isSynthetic()) {
                continue;
            }

            if (instance == null) {
                // bind static only
                if (!Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
            } else {
                // bind non-static only
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
            }

            final String[] functionNames = annotation.value();
            if (functionNames.length < 1) {
                throw new IllegalArgumentException("No function names declared for method " + method);
            }
            final Function backing = getBacking(backingProperties, functionNames[0], Function.class);
            final boolean pure = annotation.pure();

            if (backing != null && backing.pure() != pure) {
                throw new IllegalStateException("Different 'pure' values for interface and Java functions for function " + functionNames[0]);
            }

            final Function javaFunction = new JavaFunction(instance, method, backing, pure);
            for (final String functionName : functionNames) {
                object.entries.put(functionName, javaFunction);
            }
        }

        return object;
    }

    public @NotNull String[] names() {
        return names;
    }

    public @Nullable JavaFieldBinding getField(final @NotNull String name) {
        final Object value = entries.get(name);
        if (value instanceof JavaFieldBinding) {
            return (JavaFieldBinding) value;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ObjectProperty getProperty(final @NotNull String name) {
        final Object value = entries.get(name);
        if (value == null) {
            return null;
        } else if (value instanceof JavaFieldBinding) {
            return ObjectProperty.property(
                    ((JavaFieldBinding) value).get(),
                    ((JavaFieldBinding) value).constant()
            );
        } else {
            return ObjectProperty.property((Value) value, true);
        }
    }

    @Override
    public boolean set(final @NotNull String name, final @Nullable Value value) {
        return true;
    }
}
