package team.unnamed.molang.expression.binary.math;

import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.expression.Expression;
import team.unnamed.molang.expression.binary.BinaryExpression;

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
    public String toString() {
        return leftHand + " * " + rightHand;
    }
}
