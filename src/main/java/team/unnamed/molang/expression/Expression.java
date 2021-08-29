package team.unnamed.molang.expression;

import team.unnamed.molang.binding.Bind;
import team.unnamed.molang.context.EvalContext;

import java.util.List;

/**
 * A fundamental interface representing every
 * possible expression in the MoLang language
 */
public interface Expression {

    /**
     * Evaluates the expression using
     * the given {@code context}
     */
    Object eval(EvalContext context);

    /**
     * Evaluates the given {@code property} for
     * {@code this} expression using the
     * specified {@code context}, used in field
     * access
     */
    default Object evalProperty(EvalContext context, Expression property) {
        return property.eval(context);
    }

    /**
     * Calls {@code this} expression using the
     * specified {@code arguments} in the given
     * {@code context}
     */
    default Object call(EvalContext context, List<Expression> arguments) {
        Object value = eval(context);
        // try call 'value'
        return Bind.callBinding(context, value, arguments);
    }

    /**
     * Evaluates the expression using
     * the given {@code context} and
     * trying to convert it to a float,
     * returns zero if not possible
     */
    default float evalAsFloat(EvalContext context) {
        Object result = eval(context);
        if (result instanceof Boolean) {
            return ((Boolean) result) ? 1 : 0;
        } else if (!(result instanceof Number)) {
            return 0;
        } else {
            return ((Number) result).floatValue();
        }
    }

    /**
     * Evaluates the expression using the
     * given {@code context} and trying to
     * convert it to a boolean.
     */
    default boolean evalAsBoolean(EvalContext context) {
        Object result = eval(context);
        if (result instanceof Boolean) {
            return (Boolean) result;
        } else if (result instanceof Number) {
            // '0' is considered false here
            return ((Number) result).floatValue() != 0;
        }
        return true;
    }

}
