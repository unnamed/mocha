package team.unnamed.molang.ast;

import team.unnamed.molang.context.EvalContext;

/**
 * {@link InfixExpression} implementation for
 * addition of two numerical expressions
 */
public class AdditionExpression
        extends InfixExpression {

    public AdditionExpression(
            Expression leftHand,
            Expression rightHand
    ) {
        super(leftHand, rightHand);
    }

    @Override
    public float evalAsFloat(EvalContext context) {
        // override to avoid unboxing
        return leftHand.evalAsFloat(context)
                + rightHand.evalAsFloat(context);
    }

    @Override
    public Object eval(EvalContext context) {
        return evalAsFloat(context);
    }

    @Override
    public String toSource() {
        return leftHand.toSource() + " + " + rightHand.toSource();
    }

    @Override
    public String toString() {
        return "Add(" + leftHand + ", " + rightHand + ")";
    }

}
