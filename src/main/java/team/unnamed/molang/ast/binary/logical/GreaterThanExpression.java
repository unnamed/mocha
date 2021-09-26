package team.unnamed.molang.ast.binary.logical;

import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.ast.binary.BinaryExpression;

/**
 * Implementation of the 'greater than' expression,
 * it's a binary boolean expression (only evaluated
 * to true or false)
 *
 * Evaluated to true if 'leftHand' is greater than
 * 'rightHand' value.
 */
public class GreaterThanExpression
        extends BinaryExpression {

    public GreaterThanExpression(
            Expression leftHand,
            Expression rightHand
    ) {
        super(leftHand, rightHand);
    }

    @Override
    public boolean evalAsBoolean(EvalContext context) {
        return leftHand.evalAsFloat(context)
                > rightHand.evalAsFloat(context);
    }

    @Override
    public Object eval(EvalContext context) {
        return evalAsBoolean(context);
    }

    @Override
    public String toString() {
        return leftHand + " > " + rightHand;
    }

}
