package team.unnamed.molang.ast.binary;

import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.ast.Expression;

/**
 * The null coalescing expression implementation,
 * if the result of evaluating the 'leftHand' expression
 * is considered invalid, then it returns the 'rightHand'
 * result.
 *
 * See https://bedrock.dev/docs/stable/MoLang#%3F%3F%20Null%20Coalescing%20Operator
 */
public class NullCoalescingExpression
        extends BinaryExpression {

    public NullCoalescingExpression(
            Expression leftHand,
            Expression rightHand
    ) {
        super(leftHand, rightHand);
    }

    @Override
    public Object eval(EvalContext context) {
        Object value = leftHand.eval(context);
        // TODO: I don't know how to implement this yet following the specification
        return value;
    }

    @Override
    public String toString() {
        return leftHand + " ?? " + rightHand;
    }

}
