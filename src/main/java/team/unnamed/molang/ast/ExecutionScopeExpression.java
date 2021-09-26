package team.unnamed.molang.ast;

import team.unnamed.molang.context.EvalContext;

import java.util.Iterator;
import java.util.List;

public class ExecutionScopeExpression implements Expression {

    private final List<Expression> expressions;

    public ExecutionScopeExpression(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public Object eval(EvalContext context) {
        Object last = 0;
        for (Expression expression : expressions) {
            last = expression.eval(context);
        }
        return last;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        Iterator<Expression> iterator = expressions.iterator();
        while (iterator.hasNext()) {
            Expression expression = iterator.next();
            builder.append(expression.toString());
            if (iterator.hasNext()) {
                builder.append("; ");
            }
        }
        return builder.append('}').toString();
    }
}
