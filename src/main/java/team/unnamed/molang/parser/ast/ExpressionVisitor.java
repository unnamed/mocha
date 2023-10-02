/*
 * This file is part of molang, licensed under the MIT license
 *
 * Copyright (c) 2021-2023 Unnamed Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
