package team.unnamed.molang.parser;

import org.junit.jupiter.api.Test;
import team.unnamed.molang.MolangEngine;
import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.parser.ast.InfixExpression;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HierarchyTest {

    @Test
    public void test_logical_hierarchy() throws Exception {
        List<Expression> expressions = MolangEngine.createDefault().parse("age <= 5 || age >= 70");
        assertEquals(1, expressions.size(), "Size must be 1: " + expressions);

        expressions.forEach(System.out::println);

        // OR expression (a || b)
        Expression expression = expressions.get(0);
        assertTrue(expression instanceof InfixExpression, "Expression must be infix");
        InfixExpression infixExpr = (InfixExpression) expression;
        assertEquals(InfixExpression.OR, infixExpr.code(), "Expression must be OR");

        // Left-hand expression (age <= 5)
        {
            Expression lh = infixExpr.left();
            assertTrue(lh instanceof InfixExpression, "Expression must be infix");
            InfixExpression infixlh = (InfixExpression) lh;
            assertEquals(InfixExpression.LESS_THAN_OR_EQUAL, infixlh.code(), "Expression must be LESS_THAN_OR_EQUAL");
        }


        // Right-hand expression (age >= 70)
        {
            Expression rh = infixExpr.right();
            assertTrue(rh instanceof InfixExpression, "Expression must be infix");
            InfixExpression infixrh = (InfixExpression) rh;
            assertEquals(InfixExpression.GREATER_THAN_OR_EQUAL, infixrh.code(), "Expression must be GREATER_THAN_OR_EQUAL");
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
    }

    private static void assertCreateSameTree(String expr1, String expr2) throws Exception {
        MolangEngine engine = MolangEngine.createDefault();
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

}
