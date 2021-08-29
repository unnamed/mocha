package team.unnamed.molang.expression.binary.logical;

import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.expression.Expression;

/**
 * Implementation of the 'less than or equal' expression,
 * it's a boolean binary expression (value is always
 * true or false and has two expression components).
 *
 * Returns true if the 'leftHand' expression is less
 * or equal to the 'rightHand' expression value
 */
public class LessThanOrEqualExpression
        extends LessThanExpression {

    public LessThanOrEqualExpression(
            Expression leftHand,
            Expression rightHand
    ) {
        super(leftHand, rightHand);
    }

    @Override
    public boolean evalAsBoolean(EvalContext context) {
        return leftHand.evalAsFloat(context) <= rightHand.evalAsFloat(context);
    }

    @Override
    public String toString() {
        return leftHand + " <= " + rightHand;
    }

}
