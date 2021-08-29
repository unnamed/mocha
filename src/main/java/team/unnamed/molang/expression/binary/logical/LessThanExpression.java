package team.unnamed.molang.expression.binary.logical;

import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.expression.Expression;
import team.unnamed.molang.expression.binary.BinaryExpression;

/**
 * Implementation of the 'less than' expression,
 * it's a boolean binary expression (value is always
 * true or false and has two expression components).
 *
 * Returns true if the 'leftHand' expression is less
 * than the 'rightHand' expression
 */
public class LessThanExpression
        extends BinaryExpression {

    public LessThanExpression(
            Expression leftHand,
            Expression rightHand
    ) {
        super(leftHand, rightHand);
    }

    @Override
    public boolean evalAsBoolean(EvalContext context) {
        return leftHand.evalAsFloat(context) < rightHand.evalAsFloat(context);
    }

    @Override
    public Object eval(EvalContext context) {
        return evalAsBoolean(context);
    }

    @Override
    public String toString() {
        return leftHand + " < " + rightHand;
    }

}
