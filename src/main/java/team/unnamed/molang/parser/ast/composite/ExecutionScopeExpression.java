package team.unnamed.molang.parser.ast.composite;

import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.runtime.EvalContext;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Expression implementation of MoLang 1.17
 * execution scopes
 * See https://bedrock.dev/docs/1.17.0.0/1.17.30.4/
 * Molang#%7B%20%7D%20Brace%20Scope%20Delimiters
 */
public class ExecutionScopeExpression implements Expression {

    private final List<Expression> expressions;

    public ExecutionScopeExpression(List<Expression> expressions) {
        this.expressions = expressions;
    }

    /**
     * Returns the expressions inside this
     * execution scope, never null
     */
    public List<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public Object eval(EvalContext context) {
        for (Expression expression : expressions) {
            // eval expression, ignore result
            expression.eval(context);

            // check for return values
            Object returnValue = context.popReturnValue();
            if (returnValue != null) {
                return returnValue;
            }
        }
        return 0;
    }

    @Override
    public String toSource() {
        StringBuilder builder = new StringBuilder("{");
        Iterator<Expression> iterator = expressions.iterator();
        while (iterator.hasNext()) {
            Expression expression = iterator.next();
            builder.append(expression.toSource());
            if (iterator.hasNext()) {
                builder.append("; ");
            }
        }
        return builder.append('}').toString();
    }

    @Override
    public String toString() {
        return "ExecutionScope(" + this.expressions + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionScopeExpression that = (ExecutionScopeExpression) o;
        return expressions.equals(that.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressions);
    }

}
