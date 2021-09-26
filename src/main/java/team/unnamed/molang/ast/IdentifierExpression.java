package team.unnamed.molang.ast;

import team.unnamed.molang.binding.Bind;
import team.unnamed.molang.binding.CallableBinding;
import team.unnamed.molang.binding.ObjectBinding;
import team.unnamed.molang.context.EvalContext;

import java.util.List;

/**
 * Expression implementation for MoLang 1.17
 * identifiers, they are <b>case-insensitive</b>
 * See https://bedrock.dev/docs/1.17.0.0/1.17.30.4/Molang#Keywords
 */
public class IdentifierExpression implements Expression {

    private final String identifier;

    public IdentifierExpression(String identifier) {
        // MoLang is case-insensitive
        this.identifier = identifier.toLowerCase();
    }

    public String getIdentifier() {
        return identifier;
    }

    /**
     * Finds the value from the given {@code context}
     * environment
     * @param context The environment to check the
     *                identifier from
     * @return The identifier value, or null if not
     * found
     */
    @Override
    public Object eval(EvalContext context) {
        Object binding = context.getBinding(identifier);
        if (binding instanceof CallableBinding) {
            // MoLang specification declares that
            // parenthesis are optional to call a
            // function, so...
            return ((CallableBinding) binding).call();
        }
        // unknown, return null
        return null;
    }

    @Override
    public Object evalProperty(EvalContext context, Expression property) {
        if (!(property instanceof IdentifierExpression)) {
            // TODO: This is invalid, should we throw an error?
            return null;
        }
        Object binding = context.getBinding(identifier);
        if (binding instanceof ObjectBinding) {
            String propertyName = ((IdentifierExpression) property).getIdentifier();
            return ((ObjectBinding) binding).getProperty(propertyName);
        }
        return null;
    }

    @Override
    public Object call(EvalContext context, List<Expression> arguments) {
        Object binding = context.getBinding(identifier);
        // not callable, return null
        return Bind.callBinding(context, binding, arguments);
    }

    @Override
    public String toSource() {
        return identifier;
    }

    @Override
    public String toString() {
        return "Identifier(" + identifier + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentifierExpression that = (IdentifierExpression) o;
        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

}
