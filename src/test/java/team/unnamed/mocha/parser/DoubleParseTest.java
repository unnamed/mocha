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
import team.unnamed.mocha.parser.ast.FloatExpression;
import team.unnamed.mocha.parser.ast.IdentifierExpression;
import team.unnamed.mocha.parser.ast.UnaryExpression;

import static team.unnamed.mocha.MochaAssertions.assertCreateTree;

class DoubleParseTest {
    @Test
    void test() {
        assertCreateTree("1", FloatExpression.of(1D));
        assertCreateTree("   1   ", FloatExpression.of(1D));
        assertCreateTree("1.0", FloatExpression.of(1D));
        assertCreateTree("3.14", FloatExpression.of(3.14D));
        assertCreateTree("5.8", FloatExpression.of(5.8D));

        // these are not valid doubles and are parsed as identifiers
        assertCreateTree("Infinity", new IdentifierExpression("Infinity"));
        assertCreateTree("NaN", new IdentifierExpression("NaN"));
        assertCreateTree("-Infinity", new UnaryExpression(UnaryExpression.Op.ARITHMETICAL_NEGATION, new IdentifierExpression("Infinity")));

        // NEGATE A should be parsed just as -A
        assertCreateTree("-1", FloatExpression.of(-1D));
        assertCreateTree("-3.14", FloatExpression.of(-3.14D));
        assertCreateTree("--3.14", FloatExpression.of(3.14D));
        assertCreateTree("---3.14", FloatExpression.of(-3.14D));
    }
}
