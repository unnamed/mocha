package team.unnamed.mocha.runtime.binding;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.runtime.value.Value;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@ApiStatus.Internal
public final class JavaFieldBinding implements RegisteredBinding {
    private static final Set<Class<?>> INLINEABLE_TYPES;

    static {
        final Set<Class<?>> inlineableTypes = new HashSet<>();
        // "A constant variable is a final variable of primitive type or type
        // String that is initialized with a constant expression"
        inlineableTypes.add(int.class);
        inlineableTypes.add(long.class);
        inlineableTypes.add(float.class);
        inlineableTypes.add(double.class);
        inlineableTypes.add(boolean.class);
        inlineableTypes.add(byte.class);
        inlineableTypes.add(short.class);
        inlineableTypes.add(char.class);
        // inlineableTypes.add(String.class); // exclude String at the moment
        INLINEABLE_TYPES = Collections.unmodifiableSet(inlineableTypes);
    }

    private final Object object;
    private final Field field;
    private Supplier<Value> value;
    private boolean canBeInlined;

    JavaFieldBinding(final @Nullable Object object, final @Nullable Field field, final @Nullable Supplier<Value> value) {
        this.object = object;
        this.field = field;
        this.value = value;
        evaluate();
    }

    private void evaluate() {
        if (value == null) {
            // validate
            if (field == null) {
                throw new IllegalArgumentException("Either the field or its value must be given.");
            }

            final int modifiers = field.getModifiers();
            final Class<?> type = field.getType();

            // can we inline?
            if (Modifier.isFinal(modifiers)
                    && Modifier.isStatic(modifiers)
                    && Modifier.isPublic(modifiers)
                    && INLINEABLE_TYPES.contains(type)) {
                final Value val = getFromField();
                this.value = () -> val;
                this.canBeInlined = true;
            }
        }
    }

    public @Nullable Field field() {
        return field;
    }

    public boolean canBeInlined() {
        return canBeInlined;
    }

    public @NotNull Value get() {
        if (value == null) {
            return getFromField();
        }
        return value.get();
    }

    private @NotNull Value getFromField() {
        // try using the field
        requireNonNull(field, "field");

        final Object val;
        try {
            val = field.get(object);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Could not get field value.", e);
        }

        return Value.of(val);
    }
}
