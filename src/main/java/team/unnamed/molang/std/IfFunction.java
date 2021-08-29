package team.unnamed.molang.std;

import team.unnamed.molang.context.EvalContext;
import team.unnamed.molang.expression.Expression;

import java.util.List;

public class IfFunction implements Expression {

    public static final Expression INSTANCE = new IfFunction();

    private IfFunction() {
    }

    @Override
    public Object eval(EvalContext context) {
        return 0;
    }

    @Override
    public Object call(EvalContext context, List<Expression> arguments) {
        if (arguments.size() < 2) {
            return 0;
        }

        Expression conditional = arguments.get(0);
        Expression body = arguments.get(1);

        if (conditional.evalAsBoolean(context)) {
            return body.eval(context);
        } else {
            return 0;
        }
    }
}
