package team.unnamed.molang.ast;

import team.unnamed.molang.context.EvalContext;

/**
 * {@link BinaryExpression} implementation for
 * division of two numerical expressions
 */
public class DivisionExpression
        extends BinaryExpression {

    public DivisionExpression(
            Expression leftHand,
            Expression rightHand
    ) {
        super(leftHand, rightHand);
    }

    @Override
    public float evalAsFloat(EvalContext context) {
        float divisor = rightHand.evalAsFloat(context);
        if (divisor == 0F) {
            // MoLang specification declares that division by
            // zero returns zero
            // "Errors (such as divide by zero, ...) generally return a value of 0.0"
            return 0F;
        }
        // override to avoid unboxing
        return leftHand.evalAsFloat(context) / divisor;
    }

    @Override
    public Object eval(EvalContext context) {
        return evalAsFloat(context);
    }

    @Override
    public String toSource() {
        return leftHand.toSource() + " / " + rightHand.toSource();
    }

    @Override
    public String toString() {
        return "Divide(" + leftHand + ", " + rightHand + ")";
    }

}
