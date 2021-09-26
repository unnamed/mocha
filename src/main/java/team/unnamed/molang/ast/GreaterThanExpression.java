package team.unnamed.molang.ast;

import team.unnamed.molang.context.EvalContext;

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
    public String toSource() {
        return leftHand.toSource() + " > " + rightHand.toSource();
    }

    @Override
    public String toString() {
        return "GreaterThan(" + leftHand + ", " + rightHand + ")";
    }

}
