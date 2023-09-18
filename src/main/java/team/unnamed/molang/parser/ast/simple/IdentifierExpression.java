package team.unnamed.molang.parser.ast.simple;

import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.runtime.EvalContext;

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
        return context.getBinding(identifier);
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
