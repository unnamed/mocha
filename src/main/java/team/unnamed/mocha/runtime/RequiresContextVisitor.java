package team.unnamed.mocha.runtime;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.parser.ast.*;

final class RequiresContextVisitor implements ExpressionVisitor<Boolean> {
    private static final ExpressionVisitor<Boolean> INSTANCE = new RequiresContextVisitor();

    private RequiresContextVisitor() {
    }

    public static boolean test(final @NotNull Expression expression) {
        return expression.visit(INSTANCE);
    }

    @Override
    public Boolean visitDouble(final @NotNull DoubleExpression expression) {
        return false;
    }

    @Override
    public Boolean visitString(final @NotNull StringExpression expression) {
        return false;
    }

    @Override
    public Boolean visitIdentifier(final @NotNull IdentifierExpression expression) {
        return true;
    }

    @Override
    public Boolean visitTernaryConditional(final @NotNull TernaryConditionalExpression expression) {
        return expression.condition().visit(this)
                || expression.trueExpression().visit(this)
                || expression.falseExpression().visit(this);
    }

    @Override
    public Boolean visitUnary(final @NotNull UnaryExpression expression) {
        return expression.expression().visit(this);
    }

    @Override
    public Boolean visitExecutionScope(final @NotNull ExecutionScopeExpression expression) {
        for (final Expression expr : expression.expressions()) {
            if (expr.visit(this)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean visitBinary(final @NotNull BinaryExpression expression) {
        return expression.left().visit(this) || expression.right().visit(this);
    }

    @Override
    public Boolean visitAccess(final @NotNull AccessExpression expression) {
        return expression.object().visit(this); // 'string'.length may not require context
    }

    @Override
    public Boolean visitCall(final @NotNull CallExpression expression) {
        return true;
    }

    @Override
    public Boolean visitStatement(final @NotNull StatementExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(final @NotNull Expression expression) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
