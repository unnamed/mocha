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
package team.unnamed.mocha.runtime.standard;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.runtime.binding.BindExternalFunction;
import team.unnamed.mocha.runtime.binding.Binding;
import team.unnamed.mocha.runtime.value.NumberValue;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;
import team.unnamed.mocha.util.CaseInsensitiveStringHashMap;

import java.util.Map;
import java.util.Random;

/**
 * Math function bindings inside an object
 * binding, commonly named 'math'
 */
@Binding("math")
@BindExternalFunction(at = Math.class, name = "abs", args = {double.class})
@BindExternalFunction(at = Math.class, name = "log", args = {double.class}, as = "ln")
@BindExternalFunction(at = Math.class, name = "max", args = {double.class, double.class})
@BindExternalFunction(at = Math.class, name = "min", args = {double.class, double.class})
@BindExternalFunction(at = Math.class, name = "round", args = {double.class})
@BindExternalFunction(at = Math.class, name = "sqrt", args = {double.class})
@BindExternalFunction(at = Math.class, name = "pow", args = {double.class, double.class})
@BindExternalFunction(at = Math.class, name = "exp", args = {double.class})
@BindExternalFunction(at = Math.class, name = "floor", args = {double.class})
@BindExternalFunction(at = Math.class, name = "ceil", args = {double.class})
public final class MochaMath implements ObjectValue {
    @Binding("pi")
    public static final double PI = Math.PI;

    private static final double RADIAN = Math.toRadians(1);

    private static final Random RANDOM = new Random();
    private static final int DECIMAL_PART = 4;

    private final Map<String, Value> entries = new CaseInsensitiveStringHashMap<>();

    public MochaMath() {
        setFunction("abs", Math::abs);
        setFunction("acos", MochaMath::acos);
        setFunction("asin", MochaMath::asin);
        setFunction("atan", MochaMath::atan);
        setFunction("atan2", MochaMath::atan2);
        setFunction("ceil", Math::ceil);
        setFunction("clamp", MochaMath::clamp);
        setFunction("cos", MochaMath::cos);
        setFunction("die_roll", MochaMath::dieRoll);
        setFunction("die_roll_integer", MochaMath::dieRollInteger);
        setFunction("exp", Math::exp);
        setFunction("floor", Math::floor);
        setFunction("hermite_blend", MochaMath::hermiteBlend);
        setFunction("lerp", MochaMath::lerp);
        setFunction("lerprotate", MochaMath::lerpRotate);
        setFunction("ln", Math::log);
        setFunction("max", Math::max);
        setFunction("min", Math::min);
        setFunction("min_angle", MochaMath::minAngle);
        setFunction("mod", MochaMath::mod);
        entries.put("pi", NumberValue.of(Math.PI));
        setFunction("pow", Math::pow);
        setFunction("random", MochaMath::random);
        setFunction("random_integer", MochaMath::randomInteger);
        setFunction("round", Math::round);
        setFunction("sin", MochaMath::sin);
        setFunction("sqrt", Math::sqrt);
        setFunction("trunc", MochaMath::trunc);
    }

    private static double radify(double n) {
        return (((n + 180) % 360) + 180) % 360;
    }

    @Binding(value = "acos", skipChecking = true)
    public static double acos(final double value) {
        return NumberValue.normalize(Math.acos(value) / RADIAN);
    }

    @Binding(value = "asin", skipChecking = true)
    public static double asin(final double value) {
        return NumberValue.normalize(Math.asin(value) / RADIAN);
    }

    @Binding("atan")
    public static double atan(final double value) {
        return Math.atan(value) / RADIAN;
    }

    @Binding("atan2")
    public static double atan2(final double y, final double x) {
        return Math.atan2(y, x) / RADIAN;
    }

    @Binding("clamp")
    public static double clamp(final double value, final double min, final double max) {
        return Math.max(Math.min(value, max), min);
    }

    @Binding("cos")
    public static double cos(final double value) {
        return Math.cos(value * RADIAN);
    }

    @Binding("die_roll")
    public static double dieRoll(final double amount, final double low, final double high) {
        double result = 0;
        for (int i = 0; i < amount; i++) {
            result += RANDOM.nextInt((int) high) + low;
        }
        return result / DECIMAL_PART;
    }

    @Binding("die_roll_integer")
    public static double dieRollInteger(final double amount, final double low, final double high) {
        int result = 0;
        for (int i = 0; i < amount; i++) {
            result += RANDOM.nextInt((int) low, (int) high);
        }
        return result;
    }

    @Binding("hermite_blend")
    public static double hermiteBlend(final double t) {
        final double t2 = t * t;
        final double t3 = t2 * t;
        return 3 * t2 - 2 * t3;
    }

    @Binding("lerp")
    public static double lerp(final double start, final double end, final double lerp) {
        return start + lerp * (end - start);
    }

    @Binding("lerprotate")
    public static double lerpRotate(double start, double end, double lerp) {
        start = radify(start);
        end = radify(end);

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
    }

    @Binding("min_angle")
    public static double minAngle(double angle) {
        while (angle > 180)
            angle -= 360;
        while (angle < -180)
            angle += 360;
        return angle;
    }

    @Binding("mod")
    public static double mod(final double a, final double b) {
        return a % b;
    }

    @Binding("random")
    public static double random(final double min, final double max) {
        return RANDOM.nextDouble(min, max);
    }

    @Binding("random_integer")
    public static int randomInteger(final double min, final double max) {
        return RANDOM.nextInt((int) min, (int) max);
    }

    @Binding("sin")
    public static double sin(final double value) {
        return Math.sin(value * RADIAN);
    }

    @Binding("trunc")
    public static double trunc(final double value) {
        return value - value % 1;
    }

    @Override
    public @NotNull Value get(final @NotNull String name) {
        return entries.getOrDefault(name, NumberValue.zero());
    }
}
