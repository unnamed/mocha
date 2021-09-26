package team.unnamed.molang.ast;

import team.unnamed.molang.context.EvalContext;

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
    public String toSource() {
        return leftHand.toSource() + " < " + rightHand.toSource();
    }

}