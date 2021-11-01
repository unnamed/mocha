package team.unnamed.molang.ast;

import team.unnamed.molang.context.EvalContext;

/**
 * A logical "OR" expression, with a left-hand
 * and right-hand expressions that will be compared
 * to get the final result
 */
public class OrExpression
        extends InfixExpression
        implements BooleanExpression {

    public OrExpression(Expression leftHand, Expression rightHand) {
        super(leftHand, rightHand);
    }

    @Override
    public boolean evalAsBoolean(EvalContext context) {
        return leftHand.evalAsBoolean(context) || rightHand.evalAsBoolean(context);
    }

    @Override
    public String toSource() {
        return leftHand.toSource() + " || " + rightHand.toSource();
    }

    @Override
    public String toString() {
        return "Or(" + leftHand + ", " + rightHand  + ")";
    }

}
