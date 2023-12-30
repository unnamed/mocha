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
    private boolean constant;

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
                this.constant = true;
            }
        }
    }

    public @Nullable Field field() {
        return field;
    }

    public boolean constant() {
        return constant;
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
