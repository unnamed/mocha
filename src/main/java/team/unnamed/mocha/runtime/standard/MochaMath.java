/*
 * This file is part of mocha, licensed under the MIT license
 *
 * Copyright (c) 2021-2025 Unnamed Team
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
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.runtime.binding.BindExternalFunction;
import team.unnamed.mocha.runtime.binding.Binding;
import team.unnamed.mocha.runtime.value.NumberValue;
import team.unnamed.mocha.runtime.value.ObjectProperty;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.util.CaseInsensitiveStringHashMap;

import java.util.Map;
import java.util.Random;

/**
 * Math function bindings inside an object
 * binding, commonly named 'math'
 */
@Binding("math")
@BindExternalFunction(at = Math.class, name = "abs", args = {float.class}, pure = true)
@BindExternalFunction(at = Math.class, name = "max", args = {float.class, float.class}, pure = true)
@BindExternalFunction(at = Math.class, name = "min", args = {float.class, float.class}, pure = true)
@BindExternalFunction(at = Math.class, name = "round", args = {float.class}, pure = true)
public final class MochaMath implements ObjectValue {
    @Binding("pi")
    public static final double PI = Math.PI;

    private static final double RADIAN = Math.toRadians(1);

    private static final Random RANDOM = new Random();
    private static final int DECIMAL_PART = 4;

    private final Map<String, ObjectProperty> entries = new CaseInsensitiveStringHashMap<>();

    public MochaMath() {
        setFunction("abs", Math::abs);
        setFunction("acos", MochaMath::acos);
        setFunction("asin", MochaMath::asin);
        setFunction("atan", MochaMath::atan);
        setFunction("atan2", MochaMath::atan2);
        setFunction("ceil", MochaMath::ceil);
        setFunction("clamp", MochaMath::clamp);
        setFunction("cos", MochaMath::cos);
        setFunction("die_roll", MochaMath::dieRoll);
        setFunction("die_roll_integer", MochaMath::dieRollInteger);
        setFunction("exp", MochaMath::exp);
        setFunction("floor", MochaMath::floor);
        setFunction("hermite_blend", MochaMath::hermiteBlend);
        setFunction("lerp", MochaMath::lerp);
        setFunction("lerprotate", MochaMath::lerpRotate);
        setFunction("ln",  MochaMath::log);
        setFunction("max", Math::max);
        setFunction("min", Math::min);
        setFunction("min_angle", MochaMath::minAngle);
        setFunction("mod", MochaMath::mod);
        entries.put("pi", ObjectProperty.property(NumberValue.of(Math.PI), true));
        setFunction("pow", MochaMath::pow);
        setFunction("random", MochaMath::random);
        setFunction("random_integer", MochaMath::randomInteger);
        setFunction("round", Math::round);
        setFunction("sin", MochaMath::sin);
        setFunction("sqrt", MochaMath::sqrt);
        setFunction("trunc", MochaMath::trunc);

        // all of our properties are constant
        entries.replaceAll((key, property) -> ObjectProperty.property(property.value(), true));
    }

    @Binding(value = "ceil", pure = true)
    public static float ceil(float a) {
        return (float) StrictMath.ceil(a);
    }

    @Binding(value = "exp", pure = true)
    public static float exp(float a) {
        return (float) StrictMath.exp(a);
    }

    @Binding(value = "floor", pure = true)
    public static float floor(float a) {
        return (float) StrictMath.floor(a);
    }

    @Binding(value = "ln", pure = true)
    public static float log(float a) {
        return (float) StrictMath.log(a);
    }

    @Binding(value = "pow", pure = true)
    public static float pow(float a, float b) {
        return (float) StrictMath.pow(a, b);
    }

    @Binding(value = "sqrt", pure = true)
    public static float sqrt(float a) {
        return (float) StrictMath.sqrt(a);
    }

    private static float radify(float n) {
        return (((n + 180) % 360) + 180) % 360;
    }

    @Binding(value = "acos", skipChecking = true, pure = true)
    public static float acos(final float value) {
        return (float) NumberValue.normalize(Math.acos(value) / RADIAN);
    }

    @Binding(value = "asin", skipChecking = true, pure = true)
    public static float asin(final float value) {
        return (float) NumberValue.normalize(Math.asin(value) / RADIAN);
    }

    @Binding(value = "atan", pure = true)
    public static float atan(final float value) {
        return (float) (Math.atan(value) / RADIAN);
    }

    @Binding(value = "atan2", pure = true)
    public static float atan2(final float y, final float x) {
        return (float) (Math.atan2(y, x) / RADIAN);
    }

    @Binding(value = "clamp", pure = true)
    public static float clamp(final float value, final float min, final float max) {
        return Math.max(Math.min(value, max), min);
    }

    @Binding(value = "cos", pure = true)
    public static float cos(final float value) {
        return (float) Math.cos(value * RADIAN);
    }

    @Binding("die_roll")
    public static float dieRoll(final float amount, final float low, final float high) {
        float result = 0;
        for (int i = 0; i < amount; i++) {
            result += RANDOM.nextInt((int) high) + low;
        }
        return result / DECIMAL_PART;
    }

    @Binding("die_roll_integer")
    public static float dieRollInteger(final float amount, final float low, final float high) {
        int result = 0;
        for (int i = 0; i < amount; i++) {
            result += RANDOM.nextInt((int) low, (int) high);
        }
        return result;
    }

    @Binding(value = "hermite_blend", pure = true)
    public static float hermiteBlend(final float t) {
        final float t2 = t * t;
        final float t3 = t2 * t;
        return 3 * t2 - 2 * t3;
    }

    @Binding(value = "lerp", pure = true)
    public static float lerp(final float start, final float end, final float lerp) {
        return start + lerp * (end - start);
    }

    @Binding(value = "lerprotate", pure = true)
    public static float lerpRotate(float start, float end, float lerp) {
        start = radify(start);
        end = radify(end);

        if (start > end) {
            // swap
            float tmp = start;
            start = end;
            end = tmp;
        }

        float diff = end - start;
        if (diff > 180F) {
            return radify(end + lerp * (360F - diff));
        } else {
            return start + lerp * diff;
        }
    }

    @Binding(value = "min_angle", pure = true)
    public static float minAngle(float angle) {
        while (angle > 180)
            angle -= 360;
        while (angle < -180)
            angle += 360;
        return angle;
    }

    @Binding(value = "mod", pure = true)
    public static float mod(final float a, final float b) {
        return a % b;
    }

    @Binding("random")
    public static float random(final float min, final float max) {
        return RANDOM.nextFloat(min, max);
    }

    @Binding("random_integer")
    public static int randomInteger(final float min, final float max) {
        return RANDOM.nextInt((int) min, (int) max);
    }

    @Binding(value = "sin", pure = true)
    public static float sin(final float value) {
        return (float) Math.sin(value * RADIAN);
    }

    @Binding(value = "trunc", pure = true)
    public static float trunc(final float value) {
        return value - value % 1;
    }

    @Override
    public @Nullable ObjectProperty getProperty(final @NotNull String name) {
        return entries.get(name);
    }
}
