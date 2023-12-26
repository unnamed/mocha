package team.unnamed.mocha.runtime.binding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.runtime.ExecutionContext;
import team.unnamed.mocha.runtime.JavaTypes;
import team.unnamed.mocha.runtime.value.ArrayValue;
import team.unnamed.mocha.runtime.value.Function;
import team.unnamed.mocha.runtime.value.JavaValue;
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
            } else {
                final Argument argument = arguments.next();
                value = argument.eval();
            }

            if (value == null) {
                values[i] = new JavaValue(JavaTypes.getNullValueForType(parameterType));
            } else {
                values[i] = JavaTypes.convert(value, parameterType);
            }
        }

        try {
            return Value.of(method.invoke(object, values));
        } catch (final Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
