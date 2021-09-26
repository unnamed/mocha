package team.unnamed.molang.ast.binary.math;

import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.ast.binary.BinaryExpression;

/**
 * {@link BinaryExpression} implementation for
 * subtraction of two numerical expressions
 */
public class SubtractionExpression
        extends BinaryExpression {

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
