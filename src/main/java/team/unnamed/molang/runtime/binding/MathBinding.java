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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Math function bindings inside an object
 * binding, commonly named 'math'
 */
public class MathBinding extends ObjectBinding {

    private static final double RADIAN = Math.toRadians(1);

    private static final Random RANDOM = new Random();
    private static final int DECIMAL_PART = 4;

    private final Map<String, Object> bindings = new HashMap<>();

    public MathBinding() {

        bindCallable("abs", args -> Math.abs(toDouble(args[0])));
        bindCallable("acos", args -> Math.acos(toDouble(args[0])) / RADIAN);
        bindCallable("asin", args -> Math.asin(toDouble(args[0])) / RADIAN);
        bindCallable("atan", args -> Math.atan(toDouble(args[0])) / RADIAN);
        bindCallable("atan2", args -> Math.atan2(toDouble(args[0]), toDouble(args[1])) / RADIAN);
        bindCallable("ceil", args -> Math.ceil(toDouble(args[0])));
        bindCallable("clamp", args -> Math.max(Math.min(toDouble(args[0]), toDouble(args[2])), toDouble(args[1])));
        bindCallable("cos", args -> Math.cos(toRadians(args[0])));
        bindCallable("die_roll", args -> {
            int amount = (int) toDouble(args[0]);
            int low = (int) (toDouble(args[1]) * DECIMAL_PART);
            int high = (int) (toDouble(args[2]) * DECIMAL_PART) - low;
            double result = 0;
            for (int i = 0; i < amount; i++) {
                result += RANDOM.nextInt(high) + low;
            }
            return result / DECIMAL_PART;
        });
        // TODO: die_roll_integer

        bindCallable("exp", args -> Math.exp(toDouble(args[0])));
        bindCallable("floor", args -> Math.floor(toDouble(args[0])));
        bindCallable("lerprotate", args -> {
            double start = radify(toDouble(args[0]));
            double end = radify(toDouble(args[1]));
            double lerp = toDouble(args[2]);

            if (start > end) {
                // swap
                double tmp = start;
                start = end;
                end = tmp;
            }

            double diff = end - start;
            if (diff > 180F) {
                return radify(end + lerp * (360F - diff));
            } else {
                return start + lerp * diff;
            }
        });
        // TODO: hermite_blend, lerp, lerprotate
        bindCallable("ln", args -> Math.log(toDouble(args[0])));
        bindCallable("max", args -> Math.max(toDouble(args[0]), toDouble(args[1])));
        bindCallable("min", args -> Math.min(toDouble(args[0]), toDouble(args[1])));
        bindCallable("mod", args -> toDouble(args[0]) % toDouble(args[1]));
        bindings.put("pi", Math.PI);
        bindCallable("pow", args -> Math.pow(toDouble(args[0]), toDouble(args[1])));
        // TODO: random, random_integer
        bindCallable("round", args -> Math.round(toDouble(args[0])));
        bindCallable("sin", args -> Math.sin(toRadians(args[0])));
        bindCallable("sqrt", args -> Math.sqrt(toDouble(args[0])));
        // TODO: trunc
    }

    private void bindCallable(String name, CallableBinding binding) {
        bindings.put(name, binding);
    }

    @Override
    public Object getProperty(String name) {
        return bindings.getOrDefault(name, 0);
    }

    @Override
    public void setProperty(String name, Object value) {
    }

    private static double radify(double n) {
        return (((n + 180) % 360) + 180) % 360;
    }

    private static double toDouble(Object object) {
        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        } else {
            return 0D;
        }
    }

    private static double toRadians(Object object) {
        if (object instanceof Number) {
            return Math.toRadians(((Number) object).doubleValue());
        } else {
            // not fail-fast
            return 0D;
        }
    }

}
