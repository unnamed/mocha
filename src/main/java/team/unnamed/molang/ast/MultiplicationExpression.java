package team.unnamed.molang.ast;

import team.unnamed.molang.context.EvalContext;

/**
 * {@link BinaryExpression} implementation for
 * multiplication of two numerical expressions
 */
public class MultiplicationExpression
        extends BinaryExpression {

    public MultiplicationExpression(
            Expression leftHand,
            Expression rightHand
    ) {
        super(leftHand, rightHand);
    }

    @Override
    public float evalAsFloat(EvalContext context) {
        // override to avoid unboxing
        return leftHand.evalAsFloat(context)
                * rightHand.evalAsFloat(context);
    }

    @Override
    public Object eval(EvalContext context) {
        return evalAsFloat(context);
    }

    @Override
    public String toSource() {
        return leftHand.toSource() + " * " + rightHand.toSource();
    }

    @Override
    public String toString() {
        return "Multiply(" + leftHand + ", " + rightHand + ")";
    }

}
