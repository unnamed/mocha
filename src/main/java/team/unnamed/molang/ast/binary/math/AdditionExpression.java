package team.unnamed.molang.ast.binary.math;

import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.ast.binary.BinaryExpression;

/**
 * {@link BinaryExpression} implementation for
 * addition of two numerical expressions
 */
public class AdditionExpression
        extends BinaryExpression {

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
    public String toString() {
        return leftHand + " + " + rightHand;
    }

}
