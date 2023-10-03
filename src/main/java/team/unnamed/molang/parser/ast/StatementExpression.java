package team.unnamed.molang.parser.ast;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class StatementExpression implements Expression {

    private final Op op;

    public StatementExpression(final @NotNull Op op) {
        this.op = Objects.requireNonNull(op, "op");
    }

    public @NotNull Op op() {
        return op;
    }

    @Override
    public <R> R visit(final @NotNull ExpressionVisitor<R> visitor) {
        return visitor.visitStatement(this);
    }

    public enum Op {
        BREAK,
        CONTINUE
    }

}