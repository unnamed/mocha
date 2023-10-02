package team.unnamed.molang.parser.ast;

public interface ExpressionVisitor<R> {

    default R visitDouble(DoubleExpression expression) {
        return visit(expression);
    }

    default R visitString(StringExpression expression) {
        return visit(expression);
    }

    default R visitIdentifier(IdentifierExpression expression) {
        return visit(expression);
    }

    default R visitNullCoalescing(NullCoalescingExpression expression) {
        return visit(expression);
    }

    default R visitTernaryConditional(TernaryConditionalExpression expression) {
        return visit(expression);
    }

    default R visitReturn(ReturnExpression expression) {
        return visit(expression);
    }

    default R visitNegation(NegationExpression expression) {
        return visit(expression);
    }

    default R visitExecutionScope(ExecutionScopeExpression expression) {
        return visit(expression);
    }

    default R visitInfix(InfixExpression expression) {
        return visit(expression);
    }

    default R visitAccess(AccessExpression expression) {
        return visit(expression);
    }

    default R visitAssign(AssignExpression expression) {
        return visit(expression);
    }

    default R visitCall(CallExpression expression) {
        return visit(expression);
    }

    default R visitConditional(ConditionalExpression expression) {
        return visit(expression);
    }

    default R visitBreak(BreakExpression expression) {
        return visit(expression);
    }

    default R visitContinue(ContinueExpression expression) {
        return visit(expression);
    }

    R visit(Expression expression);

}
