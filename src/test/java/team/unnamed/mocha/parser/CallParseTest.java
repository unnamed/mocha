package team.unnamed.mocha.parser;

import org.junit.jupiter.api.Test;
import team.unnamed.mocha.parser.ast.AccessExpression;
import team.unnamed.mocha.parser.ast.BinaryExpression;
import team.unnamed.mocha.parser.ast.CallExpression;
import team.unnamed.mocha.parser.ast.DoubleExpression;
import team.unnamed.mocha.parser.ast.IdentifierExpression;
import team.unnamed.mocha.parser.ast.StringExpression;

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
                        new DoubleExpression(38)
                ))
        ));

        assertCreateTree("math.clamp(5, 10, 20);", new CallExpression(
                new AccessExpression(
                        new IdentifierExpression("math"),
                        "clamp"
                ),
                Arrays.asList(
                        new DoubleExpression(5),
                        new DoubleExpression(10),
                        new DoubleExpression(20)
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
