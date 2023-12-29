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
package team.unnamed.mocha.runtime.value;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.util.CaseInsensitiveStringHashMap;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Represents an object-like binding,
 * these objects can have properties
 * (or fields) that can be read and
 * sometimes written
 */
public class MutableObjectBinding implements ObjectValue {
    private final Map<String, Value> properties = new CaseInsensitiveStringHashMap<>();
    private boolean blocked = false;

    /**
     * Gets the property value in this
     * object with the given {@code name}
     */
    @Override
    public @NotNull Value get(final @NotNull String name) {
        return properties.getOrDefault(name, NumberValue.zero());
    }

    /**
     * Sets the property with the given
     * {@code name} to the specified {@code value},
     * may not be supported
     */
    @Override
    public boolean set(final @NotNull String name, final @Nullable Value value) {
        if (blocked) {
            return false;
        }
        if (value == null) {
            properties.remove(name);
        } else {
            properties.put(name, value);
        }
        return true;
    }

    public void setAllFrom(MutableObjectBinding binding) {
        requireNonNull(binding, "binding");
        if (blocked) {
            throw new IllegalStateException("This object binding has been blocked!");
        }
        this.properties.putAll(binding.properties);
    }

    public boolean blocked() {
        return blocked;
    }

    public void block() {
        if (blocked) {
            throw new IllegalStateException("Already blocked!");
        }
        blocked = true;
    }

}
