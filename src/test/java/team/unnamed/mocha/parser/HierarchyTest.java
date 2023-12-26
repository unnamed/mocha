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
package team.unnamed.mocha.parser;

import org.junit.jupiter.api.Test;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.parser.ast.BinaryExpression;
import team.unnamed.mocha.parser.ast.Expression;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HierarchyTest {

    private static void assertCreateSameTree(String expr1, String expr2) throws Exception {
        MochaEngine<?> engine = MochaEngine.createStandard();
        List<Expression> expressions1 = engine.parse(expr1);
        List<Expression> expressions2 = engine.parse(expr2);
        assertEquals(expressions1, expressions2, () -> "Expressions:\n\t- " +
                expr1 +
                "\n\t- " +
                expr2 +
                "\nGenerated different syntax trees:\n\t- " +
                expressions1 +
                "\n\t- " +
                expressions2);
    }

    @Test
    public void test_logical_hierarchy() throws Exception {
        List<Expression> expressions = MochaEngine.createStandard().parse("age <= 5 || age >= 70");
        assertEquals(1, expressions.size(), "Size must be 1: " + expressions);

        expressions.forEach(System.out::println);

        // OR expression (a || b)
        Expression expression = expressions.get(0);
        assertTrue(expression instanceof BinaryExpression, "Expression must be binary");
        BinaryExpression binaryExpr = (BinaryExpression) expression;
        assertEquals(BinaryExpression.Op.OR, binaryExpr.op(), "Expression must be OR");

        // Left-hand expression (age <= 5)
        {
            Expression lh = binaryExpr.left();
            assertTrue(lh instanceof BinaryExpression, "Expression must be binary");
            BinaryExpression binarylh = (BinaryExpression) lh;
            assertEquals(BinaryExpression.Op.LTE, binarylh.op(), "Expression must be LESS_THAN_OR_EQUAL");
        }


        // Right-hand expression (age >= 70)
        {
            Expression rh = binaryExpr.right();
            assertTrue(rh instanceof BinaryExpression, "Expression must be binary");
            BinaryExpression binaryrh = (BinaryExpression) rh;
            assertEquals(BinaryExpression.Op.GTE, binaryrh.op(), "Expression must be GREATER_THAN_OR_EQUAL");
        }
    }

    @Test
    public void test_logical_hierarchy_2() throws Exception {
        // OR, LTE, GTE
        assertCreateSameTree("((age) <= 5) || ((age) >= 70)", "age <= 5 || age >= 70");
        // access, LT, OR, GT
        assertCreateSameTree("((t.i < 8) || (t.i > 17))", "t.i < 8 || t.i > 17");
        // arithmetic
        assertCreateSameTree("(1 * 2) + 3", "1 * 2 + 3");
        assertCreateSameTree("1 + 2 * 3", "1 + 2 * 3");
        assertCreateSameTree("math.sqrt((3 * 3) + (4 * 4))", "math.sqrt(3 * 3 + 4 * 4)");
        assertCreateSameTree("math.sqrt((5 * 5) - (4 * 4))", "math.sqrt(5 * 5 - 4 * 4)");
        assertCreateSameTree("3 + (cool * cool) + 9", "3 + cool * cool + 9");
        // combined
        assertCreateSameTree("((!shy) && (!me)) || (5 > ((math.sqrt(9)) * 5))", "!shy && !me || 5 > math.sqrt(9) * 5");
        assertCreateSameTree("r00 = ((2 * ((q0 * q0) + (q1 * q1))) - 1)", "r00 = 2 * (q0 * q0 + q1 * q1) - 1");
        assertCreateSameTree("v.x = ((t.sin_x * t.sin_x) + (v.x * v.x))", "v.x = t.sin_x * t.sin_x + v.x * v.x");
        // left-to-right
        assertCreateSameTree("((12 / 2) / 2)", "12 / 2 / 2");
        assertCreateSameTree("((((1 + 2) + 3) + 4) + 5) + 6", "1 + 2 + 3 + 4 + 5 + 6");
        assertCreateSameTree("((10 - 10) - 10) - 10", "10 - 10 - 10 - 10");
        assertCreateSameTree("((((10 * 5) / 5) * 6) / 20) * 8", "10 * 5 / 5 * 6 / 20 * 8");
        assertCreateSameTree("(((10 + (5 * 8)) + 20) - (8 / 4)) + ((9 / 4) * 5)", "10 + 5 * 8 + 20 - 8 / 4 + 9 / 4 * 5");
        // eq, neq
        assertCreateSameTree("(((true) && (false == false)) || (true))", "true && false == false || true");
    }

}
