package team.unnamed.molang.ast.binary;

import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.ast.Tokens;
import team.unnamed.molang.binding.ObjectBinding;
import team.unnamed.molang.context.EvalContext;

/**
 * {@link Expression} implementation for
 * representing property accessing
 */
public class AccessExpression implements Expression {

    private final Expression object;
    private final String property;

    public AccessExpression(
            Expression object,
            String property
    ) {
        this.object = object;
        this.property = property;
    }

    @Override
    public Object eval(EvalContext context) {
        Object binding = object.eval(context);
        if (binding instanceof ObjectBinding) {
            return ((ObjectBinding) binding).getProperty(property);
        }
        return null;
    }

    @Override
    public void setValue(EvalContext context, Object value) {
        Object binding = object.eval(context);
        if (binding instanceof ObjectBinding) {
            ((ObjectBinding) binding).setProperty(property, value);
        }
    }

    @Override
    public String toSource() {
        return object.toSource() + Tokens.DOT + property;
    }

    @Override
    public String toString() {
        return "Access(" + object + ", " + property + ")";
    }

}
