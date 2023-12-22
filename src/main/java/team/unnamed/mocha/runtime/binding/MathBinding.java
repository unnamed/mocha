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

import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.runtime.Function;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static team.unnamed.mocha.runtime.binding.ValueConversions.preferZero;

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
        bindCallable("abs", (ctx, args) -> Math.abs(args.next().evalAsDouble()));
        bindCallable("acos", (ctx, args) -> preferZero(Math.acos(args.next().evalAsDouble()) / RADIAN));
        bindCallable("asin", (ctx, args) -> preferZero(Math.asin(args.next().evalAsDouble()) / RADIAN));
        bindCallable("atan", (ctx, args) -> Math.atan(args.next().evalAsDouble()) / RADIAN);
        bindCallable("atan2", (ctx, args) -> Math.atan2(args.next().evalAsDouble(), args.next().evalAsDouble()) / RADIAN);
        bindCallable("ceil", (ctx, args) -> Math.ceil(args.next().evalAsDouble()));
        bindCallable("clamp", (ctx, args) -> {
            final var value = args.next().evalAsDouble();
            final var min = args.next().evalAsDouble();
            final var max = args.next().evalAsDouble();
            return Math.max(Math.min(value, max), min);
        });
        bindCallable("cos", (ctx, args) -> Math.cos(args.next().evalAsDouble() * RADIAN));
        bindCallable("die_roll", (ctx, args) -> {
            int amount = (int) args.next().evalAsDouble();
            int low = (int) (args.next().evalAsDouble() * DECIMAL_PART);
            int high = (int) (args.next().evalAsDouble() * DECIMAL_PART) - low;
            double result = 0;
            for (int i = 0; i < amount; i++) {
                result += RANDOM.nextInt(high) + low;
            }
            return result / DECIMAL_PART;
        });
        bindCallable("die_roll_integer", (ctx, args) -> {
            int amount = (int) args.next().evalAsDouble();
            int low = (int) args.next().evalAsDouble();
            int high = (int) args.next().evalAsDouble();
            int result = 0;
            for (int i = 0; i < amount; i++) {
                result += RANDOM.nextInt(low, high);
            }
            return result;
        });
        bindCallable("exp", (ctx, args) -> Math.exp(args.next().evalAsDouble()));
        bindCallable("floor", (ctx, args) -> Math.floor(args.next().evalAsDouble()));
        bindCallable("hermite_blend", (ctx, args) -> {
            final var t = args.next().evalAsDouble();
            final var t2 = t * t;
            final var t3 = t2 * t;
            return 3 * t2 - 2 * t3;
        });
        bindCallable("lerp", (ctx, args) -> {
            final var start = args.next().evalAsDouble();
            final var end = args.next().evalAsDouble();
            final var lerp = args.next().evalAsDouble();
            return start + lerp * (end - start);
        });
        bindCallable("lerprotate", (ctx, args) -> {
            double start = radify(args.next().evalAsDouble());
            double end = radify(args.next().evalAsDouble());
            double lerp = args.next().evalAsDouble();

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
        bindCallable("ln", (ctx, args) -> Math.log(args.next().evalAsDouble()));
        bindCallable("max", (ctx, args) -> Math.max(args.next().evalAsDouble(), args.next().evalAsDouble()));
        bindCallable("min", (ctx, args) -> Math.min(args.next().evalAsDouble(), args.next().evalAsDouble()));
        bindCallable("min_angle", (ctx, args) -> {
            // Minimize angle magnitude (in degrees) into the range [-180, 180]
            double angle = args.next().evalAsDouble();
            // todo: is there any faster way to do this? brain hurts rn
            while (angle > 180)
                angle -= 360;
            while (angle < -180)
                angle += 360;
            return angle;
        });
        bindCallable("mod", (ctx, args) -> args.next().evalAsDouble() % args.next().evalAsDouble());
        bindings.put("pi", Math.PI);
        bindCallable("pow", (ctx, args) -> Math.pow(args.next().evalAsDouble(), args.next().evalAsDouble()));
        bindCallable("random", (ctx, args) -> RANDOM.nextDouble(args.next().evalAsDouble(), args.next().evalAsDouble()));
        bindCallable("random_integer", (ctx, args) -> RANDOM.nextInt((int) args.next().evalAsDouble(), (int) args.next().evalAsDouble()));
        bindCallable("round", (ctx, args) -> Math.round(args.next().evalAsDouble()));
        bindCallable("sin", (ctx, args) -> Math.sin(args.next().evalAsDouble() * RADIAN));
        bindCallable("sqrt", (ctx, args) -> Math.sqrt(args.next().evalAsDouble()));
        bindCallable("trunc", (ctx, args) -> {
            final var value = args.next().evalAsDouble();
            return value - value % 1;
        });
    }

    private static double radify(double n) {
        return (((n + 180) % 360) + 180) % 360;
    }

    private static double toRadians(Object object) {
        if (object instanceof Number) {
            return Math.toRadians(((Number) object).doubleValue());
        } else {
            // not fail-fast
            return 0D;
        }
    }

    private void bindCallable(final @NotNull String name, final @NotNull Function binding) {
        bindings.put(name, binding);
    }

    @Override
    public Object getProperty(String name) {
        return bindings.getOrDefault(name, 0);
    }

    @Override
    public void setProperty(String name, Object value) {
    }

}