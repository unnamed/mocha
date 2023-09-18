package team.unnamed.molang.parser.ast.binary;

import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.runtime.EvalContext;

/**
 * Implementation of MoLang 1.17 binary conditional
 * expression, it's similar to an "if {...} " expression.
 *
 * See https://bedrock.dev/docs/1.17.0.0/1.17.30.4/Molang#Conditionals
 *
 * <p>If the evaluated value of the {@code conditional}
 * expression is considered true, it evaluates the
 * {@code predicate} expression.</p>
 */
public class ConditionalExpression implements Expression {

    private final Expression condition;
    private final Expression predicate;

    public ConditionalExpression(
            Expression condition,
            Expression predicate
    ) {
        this.condition = condition;
        this.predicate = predicate;
    }

    @Override
    public Object eval(EvalContext context) {
        if (condition.evalAsBoolean(context)) {
            return predicate.eval(context);
        } else {
            return 0;
        }
    }

    @Override
    public String toSource() {
        return condition.toSource() + " ? " + predicate.toSource();
    }

    @Override
    public String toString() {
        return "Condition(" + condition + ", " + predicate + ")";
    }

}
