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

class TernaryParseTest {
    @Test
    void test() {
        assertCreateTree("query.mark_variant == 1 ? Geometry.robot : Geometry.default", new TernaryConditionalExpression(
                new BinaryExpression(BinaryExpression.Op.EQ, new AccessExpression(
                        new IdentifierExpression("query"),
                        "mark_variant"
                ), FloatExpression.of(1)),
                new AccessExpression(
                        new IdentifierExpression("Geometry"),
                        "robot"
                ),
                new AccessExpression(
                        new IdentifierExpression("Geometry"),
                        "default"
                )
        ));

        assertCreateTree("query.test() ? 1+1 : 2+2", new TernaryConditionalExpression(
                new CallExpression(
                        new AccessExpression(
                                new IdentifierExpression("query"),
                                "test"
                        ),
                        Collections.emptyList()
                ),
                new BinaryExpression(BinaryExpression.Op.ADD, FloatExpression.of(1), FloatExpression.of(1)),
                new BinaryExpression(BinaryExpression.Op.ADD, FloatExpression.of(2), FloatExpression.of(2))
        ));

        assertCreateTree("3 + 3 == 6 ? 1 : 2", new TernaryConditionalExpression(
                new BinaryExpression(BinaryExpression.Op.EQ,
                        new BinaryExpression(BinaryExpression.Op.ADD, FloatExpression.of(3), FloatExpression.of(3)),
                        FloatExpression.of(6)),
                FloatExpression.of(1),
                FloatExpression.of(2)
        ));
    }
}
