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
package team.unnamed.mocha.runtime.arrow;

import org.junit.jupiter.api.Test;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.value.ArrayValue;
import team.unnamed.mocha.runtime.value.Function;
import team.unnamed.mocha.runtime.value.JavaValue;
import team.unnamed.mocha.runtime.value.NumberValue;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArrowOperatorTest {
    @Test
    void test() throws Exception {
        final World world = new World();
        final Entity self = new Entity(world, 8, "Steve");
        new Entity(world, 9, "Pig");
        new Entity(world, 15, "Chicken");
        new Entity(world, 0, "Zombie");

        final MochaEngine<Entity> engine = MochaEngine.create(self);
        engine.bindDefaults();
        engine.scope().forceSet("self", new JavaValue(self));
        engine.scope().query().set("get_location", (Function<Entity>) (ctx, args) -> NumberValue.of(ctx.entity().location));
        engine.scope().query().set("get_nearby_entities", (Function<Entity>) (ctx, args) -> {
            final Entity entity = ctx.entity();
            final double distance = args.next().eval().getAsNumber();

            final int from = (int) Math.max(entity.location - distance, 0);
            final int to = (int) Math.min(entity.location + distance, entity.world.entities.length);

            List<JavaValue> found = new ArrayList<>();
            for (int i = from; i < to; i++) {
                final Entity nearby = entity.world.entities[i];
                if (nearby != null) {
                    found.add(new JavaValue(nearby));
                }
            }
            return ArrayValue.of(found.toArray(JavaValue[]::new));
        });

        final double result = engine.eval(
                "v.result = 0;\n"
                        + "for_each(t.nearby, self->q.get_nearby_entities(5), {\n"
                        + "    v.result = v.result + t.nearby->q.get_location();\n"
                        + "});\n"
                        + "return v.result;"
        );
        assertEquals(17D, result);


        final double result2 = engine.eval(
                "v.result = 0;\n"
                        + "for_each(t.nearby, self->q.get_nearby_entities(500), {\n" // Same code but 500 instead of 5
                        + "    v.result = v.result + t.nearby->q.get_location();\n"
                        + "});\n"
                        + "return v.result;"
        );
        assertEquals(32D, result2);
    }
}
