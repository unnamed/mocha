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
package team.unnamed.mocha.runtime.arrow;

import org.junit.jupiter.api.Test;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.binding.Binding;
import team.unnamed.mocha.runtime.binding.Entity;
import team.unnamed.mocha.runtime.value.JavaValue;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArrowOperatorTest {
    @Test
    void test() {
        final World world = new World();
        final Player self = new Player(world, 8, "Steve");
        new Player(world, 9, "Pig");
        new Player(world, 15, "Chicken");
        new Player(world, 0, "Zombie");

        final MochaEngine<Player> engine = MochaEngine.createStandard(self);
        engine.bind(QueryImpl.class);
        engine.scope().set("self", new JavaValue(self));

        final float result = engine.eval(
                "v.result = 0;\n"
                        + "for_each(t.nearby, self->q.get_nearby_entities(5), {\n"
                        + "    v.result = v.result + t.nearby->q.get_location();\n"
                        + "});\n"
                        + "return v.result;"
        );
        assertEquals(17F, result);


        final float result2 = engine.eval(
                "v.result = 0;\n"
                        + "for_each(t.nearby, self->q.get_nearby_entities(500), {\n" // Same code but 500 instead of 5
                        + "    v.result = v.result + t.nearby->q.get_location();\n"
                        + "});\n"
                        + "return v.result;"
        );
        assertEquals(32F, result2);
    }

    @Binding({"query", "q"})
    public static final class QueryImpl {
        @Binding("get_location")
        public static double getLocation(final @Entity Player player) {
            return player.location;
        }

        @Binding("get_nearby_entities")
        public static Player[] getNearbyEntities(final @Entity Player player, final double distance) {
            final int from = (int) Math.max(player.location - distance, 0);
            final int to = (int) Math.min(player.location + distance, player.world.entities.length);

            List<Player> found = new ArrayList<>();
            for (int i = from; i < to; i++) {
                final Player nearby = player.world.entities[i];
                if (nearby != null) {
                    found.add(nearby);
                }
            }
            return found.toArray(Player[]::new);
        }
    }
}
