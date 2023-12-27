package team.unnamed.mocha.parser;

import org.junit.jupiter.api.Test;
import team.unnamed.mocha.parser.ast.AccessExpression;
import team.unnamed.mocha.parser.ast.ArrayAccessExpression;
import team.unnamed.mocha.parser.ast.BinaryExpression;
import team.unnamed.mocha.parser.ast.CallExpression;
import team.unnamed.mocha.parser.ast.DoubleExpression;
import team.unnamed.mocha.parser.ast.IdentifierExpression;

import java.util.Collections;

import static team.unnamed.mocha.MochaAssertions.assertCreateTree;
import static team.unnamed.mocha.MochaAssertions.assertParseError;

class ArrayAccessParseTest {
    @Test
    void test() {
        assertCreateTree("materials[0]", new ArrayAccessExpression(
                new IdentifierExpression("materials"),
                new DoubleExpression(0D)
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
                                                        new DoubleExpression(12.3D)
                                                ),
                                                new DoubleExpression(41.9D)
                                        ))
                                ),
                                new DoubleExpression(10D)
                        ),
                        new DoubleExpression(0.6D)
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
