package team.unnamed.mocha.runtime.binding;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.runtime.value.Function;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public final class JavaObjectBinding implements ObjectValue {
    private final Map<String, Object> entries = new HashMap<>();

    private static <T extends Value> T getBacking(final @Nullable Map<String, Value> backingProperties, final @NotNull String functionName, final Class<T> valueType) {
        if (backingProperties != null) {
            final Value removed = backingProperties.get(functionName);
            if (valueType.isInstance(removed)) {
                return valueType.cast(removed);
            }
        }
        return null;
    }

    public static <T> @NotNull JavaObjectBinding of(final @NotNull Class<T> clazz, final @Nullable T instance) {
        final JavaObjectBinding object = new JavaObjectBinding();
        final Map<String, Value> backingProperties = instance instanceof ObjectValue ? ((ObjectValue) instance).entries() : null;

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

                    object.entries.put(functionName, new JavaFunction<>(instance, method, getBacking(backingProperties, functionName, Function.class)));
                }
            }
        }

        for (final Field field : clazz.getDeclaredFields()) {
            final Binding annotation = field.getDeclaredAnnotation(Binding.class);
            if (annotation == null) {
                continue;
            }

            final String propertyName = annotation.value();
            final Value backingValue = getBacking(backingProperties, propertyName, Value.class);
            object.entries.put(propertyName, new JavaFieldBinding(
                    instance,
                    field,
                    backingValue == null ? null : () -> backingValue
            ));
        }

        for (final Method method : clazz.getDeclaredMethods()) {
            final Binding annotation = method.getDeclaredAnnotation(Binding.class);
            if (annotation == null) {
                continue;
            }

            if (!Modifier.isStatic(method.getModifiers()) || method.isSynthetic()) {
                continue;
            }

            final String functionName = annotation.value();
            object.entries.put(functionName, new JavaFunction<>(instance, method, getBacking(backingProperties, functionName, Function.class)));
        }

        return object;
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
    public @NotNull Value get(final @NotNull String name) {
        final Object value = entries.get(name);
        if (value instanceof JavaFieldBinding) {
            // todo:
            return ((JavaFieldBinding) value).get();
        } else {
            return (Value) value;
        }
    }

    @Override
    public boolean set(final @NotNull String name, final @Nullable Value value) {
        return true;
    }
}
