package team.unnamed.molang.parser.ast.binary;

import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.runtime.EvalContext;

/**
 * The null coalescing expression implementation,
 * if the result of evaluating the 'leftHand' expression
 * is considered invalid, then it returns the 'rightHand'
 * result.
 *
 * See https://bedrock.dev/docs/1.17.0.0/1.17.30.4/
 * Molang#%3F%3F%20Null%20Coalescing%20Operator
 */
public class NullCoalescingExpression implements Expression {

    private final Expression value;
    private final Expression fallback;

    public NullCoalescingExpression(
            Expression value,
            Expression fallback
    ) {
        this.value = value;
        this.fallback = fallback;
    }

    @Override
    public Object eval(EvalContext context) {
        Object val = value.eval(context);
        if (val == null) {
            return fallback.eval(context);
        } else {
            return val;
        }
    }

    @Override
    public String toSource() {
        return value.toSource() + " ?? "
                + fallback.toSource();
    }

    @Override
    public String toString() {
        return "NullCoalescing(" + value + ", " + fallback + ")";
    }

}
