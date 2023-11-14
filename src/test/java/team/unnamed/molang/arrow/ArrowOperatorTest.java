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

package team.unnamed.molang.arrow;

import org.junit.jupiter.api.Test;
import team.unnamed.molang.MolangEngine;
import team.unnamed.molang.runtime.Function;

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

        final MolangEngine engine = MolangEngine.builder()
                .withDefaultBindings()
                .bindVariable("self", self)
                .bindVariable("get_nearby_entities", (Function) (ctx, args) -> {
                    if (args.length < 1) {
                        return 0;
                    }

                    final Entity entity = ctx.entity(Entity.class);
                    final double distance = args[0].evalAsDouble();

                    final int from = (int) Math.max(entity.location - distance, 0);
                    final int to = (int) Math.min(entity.location + distance, entity.world.entities.length);

                    List<Entity> found = new ArrayList<>();
                    for (int i = from; i < to; i++) {
                        final Entity nearby = entity.world.entities[i];
                        if (nearby != null) {
                            found.add(nearby);
                        }
                    }
                    return found;
                })
                .bindVariable("get_name", (Function) (ctx, args) -> ctx.entity(Entity.class).name)
                .build();

        final Object result = engine.eval(
                "t.first = 1;\n"
                        + "v.result = '';\n"
                        + "for_each(t.nearby, v.self->v.get_nearby_entities(5), {\n"
                        + "    (!t.first) ? {\n"
                        + "        v.result = v.result + ', ';\n"
                        + "    };\n"
                        + "    v.result = v.result + t.nearby->v.get_name();\n"
                        + "    t.first = 0;\n"
                        + "});\n"
                        + "return v.result;"
        );
        assertEquals("Steve, Pig", result);


        final Object result2 = engine.eval(
                "t.first = 1;\n"
                        + "v.result = '';\n"
                        + "for_each(t.nearby, v.self->v.get_nearby_entities(500), {\n" // Same code but 500 instead of 5
                        + "    (!t.first) ? {\n"
                        + "        v.result = v.result + ', ';\n"
                        + "    };\n"
                        + "    v.result = v.result + t.nearby->v.get_name();\n"
                        + "    t.first = 0;\n"
                        + "});\n"
                        + "return v.result;"
        );
        assertEquals("Zombie, Steve, Pig, Chicken", result2);
    }
}