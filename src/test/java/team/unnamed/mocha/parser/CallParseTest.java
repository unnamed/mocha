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

import java.util.Arrays;
import java.util.Collections;

import static team.unnamed.mocha.MochaAssertions.assertCreateTree;
import static team.unnamed.mocha.MochaAssertions.assertParseError;

class CallParseTest {
    @Test
    void test() {
        assertCreateTree("query.is_baby()", new CallExpression(
                new AccessExpression(
                        new IdentifierExpression("query"),
                        "is_baby"
                ),
                Collections.emptyList()
        ));

        assertCreateTree("query.is_item_equipped('main_hand')", new CallExpression(
                new AccessExpression(
                        new IdentifierExpression("query"),
                        "is_item_equipped"
                ),
                Collections.singletonList(new StringExpression(
                        "main_hand"
                ))
        ));

        assertCreateTree("math.cos(query.anim_time * 38)", new CallExpression(
                new AccessExpression(
                        new IdentifierExpression("math"),
                        "cos"
                ),
                Collections.singletonList(new BinaryExpression(
                        BinaryExpression.Op.MUL,
                        new AccessExpression(
                                new IdentifierExpression("query"),
                                "anim_time"
                        ),
                        FloatExpression.of(38)
                ))
        ));

        assertCreateTree("math.clamp(5, 10, 20);", new CallExpression(
                new AccessExpression(
                        new IdentifierExpression("math"),
                        "clamp"
                ),
                Arrays.asList(
                        FloatExpression.of(5),
                        FloatExpression.of(10),
                        FloatExpression.of(20)
                )
        ));
    }

    @Test
    void test_incorrect() {
        // unclosed parenthesis
        assertParseError("math.clamp(", 11);

        // too many parenthesis
        assertParseError("math.clamp())", 13);

        // what is this
        assertParseError("math.clamp)", 11);

        // comma expected
        assertParseError("math.clamp(5 10)", 15);

        // RPAREN expected
        assertParseError("math.clamp(5;)", 13);
    }
}
