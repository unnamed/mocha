package team.unnamed.molang.ast.composite;

import team.unnamed.molang.ast.Expression;
import team.unnamed.molang.binding.CallableBinding;
import team.unnamed.molang.context.EvalContext;

import java.util.Iterator;
import java.util.List;

/**
 * Expression implementation for MoLang 1.17 function
 * call expression
 */
public class CallExpression
        implements Expression {

    private final Expression function;
    private final List<Expression> arguments;

    public CallExpression(Expression function, List<Expression> arguments) {
        this.function = function;
        this.arguments = arguments;
    }

    /**
     * Returns the expression evaluated to the
     * invoked function
     */
    public Expression getFunction() {
        return function;
    }

    /**
     * Returns the expressions evaluated to the
     * function arguments
     */
    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public Object eval(EvalContext context) {
        Object binding = function.eval(context);
        if (!(binding instanceof CallableBinding)) {
            // TODO: This isn't fail-fast, check this in specification
            return 0;
        }

        Object[] evaluatedArguments = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            evaluatedArguments[i] = arguments.get(i).eval(context);
        }
        return ((CallableBinding) binding).call(evaluatedArguments);
    }

    @Override
    public String toSource() {
        StringBuilder builder = new StringBuilder()
                .append(function)
                .append('(');

        Iterator<Expression> argIterator = arguments.iterator();

        while (argIterator.hasNext()) {
            Expression argument = argIterator.next();
            builder.append(argument);

            if (argIterator.hasNext()) {
                builder.append(", ");
            }
        }

        return builder.append(')').toString();
    }

    @Override
    public String toString() {
        return "Call(" + function + ", " + arguments + ")";
    }

}
