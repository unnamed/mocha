package team.unnamed.molang.parser.ast;

/**
 * A fundamental interface representing every
 * possible expression in the MoLang language
 */
public interface Expression {

    <R> R visit(ExpressionVisitor<R> visitor);

    /**
     * Returns the expression as source string,
     *
     * <p>It represents the source string used to parse
     * this expression instance, but may not be exact
     * since spaces and line breaks aren't stored.</p>
     *
     * @return The expression as source string
     */
    String toSource();

}
