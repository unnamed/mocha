package team.unnamed.molang.ast;

import team.unnamed.molang.context.EvalContext;

/**
 * Implementation of the 'greater than or equal'
 * expression, it's a binary boolean expression (only
 * evaluated to true or false)
 *
 * Evaluated to true if 'leftHand' is greater or
 * equal to 'rightHand' value.
 */
public class GreaterThanOrEqualExpression
        extends GreaterThanExpression {

    public GreaterThanOrEqualExpression(
            Expression leftHand,
            Expression rightHand
    ) {
        super(leftHand, rightHand);
    }

    @Override
    public boolean evalAsBoolean(EvalContext context) {
        return leftHand.evalAsFloat(context)
                >= rightHand.evalAsFloat(context);
    }

    @Override
    public String toSource() {
        return leftHand.toSource() + " >= " + rightHand.toSource();
    }

}
