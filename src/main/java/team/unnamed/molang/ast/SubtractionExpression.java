package team.unnamed.molang.ast;

import team.unnamed.molang.context.EvalContext;

/**
 * {@link InfixExpression} implementation for
 * subtraction of two numerical expressions
 */
public class SubtractionExpression
        extends InfixExpression {

    public SubtractionExpression(
            Expression leftHand,
            Expression rightHand
    ) {
        super(leftHand, rightHand);
    }

    @Override
    public float evalAsFloat(EvalContext context) {
        // override to avoid unboxing
        return leftHand.evalAsFloat(context)
                - rightHand.evalAsFloat(context);
    }

    @Override
    public Object eval(EvalContext context) {
        return evalAsFloat(context);
    }

    @Override
    public String toSource() {
        return leftHand.toSource() + "-" + rightHand.toSource();
    }

    @Override
    public String toString() {
        return "Subtract(" + leftHand + ", " + rightHand + ")";
    }

}
