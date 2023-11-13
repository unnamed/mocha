/*
 * This file is part of molang, licensed under the MIT license
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

package team.unnamed.molang.runtime.binding;

import org.jetbrains.annotations.Nullable;

public final class ValueConversions {

    private ValueConversions() {
    }

    public static boolean asBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if (obj instanceof Number) {
            // '0' is considered false here, anything else
            // is considered true.
            return ((Number) obj).floatValue() != 0;
        } else {
            return true;
        }
    }

    public static float asFloat(Object obj) {
        if (obj instanceof Boolean) {
            return ((Boolean) obj) ? 1 : 0;
        } else if (!(obj instanceof Number)) {
            return 0;
        } else {
            return ((Number) obj).floatValue();
        }
    }

    public static double asDouble(final @Nullable Object obj) {
        if (obj instanceof Boolean) {
            return ((Boolean) obj) ? 1.0D : 0.0D;
        } else if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else {
            return 0D;
        }
    }

}
