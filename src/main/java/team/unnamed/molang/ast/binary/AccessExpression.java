package team.unnamed.molang.ast.binary;

import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.ast.Tokens;
import team.unnamed.molang.ast.simple.IdentifierExpression;
import team.unnamed.molang.binding.ObjectBinding;
import team.unnamed.molang.context.EvalContext;

/**
 * {@link Expression} implementation for
 * representing property accessing
 */
public class AccessExpression implements Expression {

    private final Expression object;
    private final Expression property;

    public AccessExpression(
            Expression object,
            Expression property
    ) {
        this.object = object;
        this.property = property;
    }

    @Override
    public Object eval(EvalContext context) {
        if (!(property instanceof IdentifierExpression)) {
            return 0;
        }
        Object binding = object.eval(context);
        if (binding instanceof ObjectBinding) {
            String propertyName = ((IdentifierExpression) property).getIdentifier();
            return ((ObjectBinding) binding).getProperty(propertyName);
        }
        return null;
    }

    @Override
    public String toSource() {
        return object.toSource() + Tokens.DOT + property.toSource();
    }

    @Override
    public String toString() {
        return "Access(" + object + ", " + property + ")";
    }

}
