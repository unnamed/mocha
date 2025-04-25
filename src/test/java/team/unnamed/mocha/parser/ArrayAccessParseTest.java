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
package team.unnamed.mocha.parser;

import org.junit.jupiter.api.Test;
import team.unnamed.mocha.parser.ast.*;

import java.util.Collections;

import static team.unnamed.mocha.MochaAssertions.assertCreateTree;
import static team.unnamed.mocha.MochaAssertions.assertParseError;

class ArrayAccessParseTest {
    @Test
    void test() {
        assertCreateTree("materials[0]", new ArrayAccessExpression(
                new IdentifierExpression("materials"),
                FloatExpression.of(0D)
        ));

        assertCreateTree("array.my_geos[math.cos(query.anim_time * 12.3 + 41.9) * 10 + 0.6]", new ArrayAccessExpression(
                new AccessExpression(
                        new IdentifierExpression("array"),
                        "my_geos"
                ),
                new BinaryExpression(
                        BinaryExpression.Op.ADD,
                        new BinaryExpression(
                                BinaryExpression.Op.MUL,
                                new CallExpression(
                                        new AccessExpression(
                                                new IdentifierExpression("math"),
                                                "cos"
                                        ),
                                        Collections.singletonList(new BinaryExpression(
                                                BinaryExpression.Op.ADD,
                                                new BinaryExpression(
                                                        BinaryExpression.Op.MUL,
                                                        new AccessExpression(
                                                                new IdentifierExpression("query"),
                                                                "anim_time"
                                                        ),
                                                        FloatExpression.of(12.3D)
                                                ),
                                                FloatExpression.of(41.9D)
                                        ))
                                ),
                                FloatExpression.of(10D)
                        ),
                        FloatExpression.of(0.6D)
                )
        ));
    }

    @Test
    void test_incorrect() {
        // unexpected COMMA, expected RBRACKET
        assertParseError("array.my_geos[0, 1]", 16);

        // unexpected RBRACKET, expected expression
        assertParseError("array.my_geos[]", 15);

        // unpaired LBRACKET
        assertParseError("array.my_geos[", 14);
    }
}
