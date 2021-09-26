package team.unnamed.molang.ast.conditional;

import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.ast.binary.BinaryExpression;

/**
 * Implementation of the binary conditional expression,
 * it's similar to an "if {...} " expression.
 *
 * If the evaluated value of the {@code conditional}
 * expression is considered true, it evaluates the
 * {@code predicate} expression.
 */
public class BinaryConditionalExpression
        extends BinaryExpression {

    public BinaryConditionalExpression(
            Expression conditional,
            Expression predicate
    ) {
        super(conditional, predicate);
    }

    @Override
    public Object eval(EvalContext context) {
        if (leftHand.evalAsBoolean(context)) {
            return rightHand.eval(context);
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return leftHand + " ? " + rightHand;
    }

}
