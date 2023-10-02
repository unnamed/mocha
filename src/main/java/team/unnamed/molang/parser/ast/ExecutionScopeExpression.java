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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Expression implementation of MoLang 1.17
 * execution scopes
 * See https://bedrock.dev/docs/1.17.0.0/1.17.30.4/
 * Molang#%7B%20%7D%20Brace%20Scope%20Delimiters
 */
public class ExecutionScopeExpression implements Expression {

    private final List<Expression> expressions;

    public ExecutionScopeExpression(List<Expression> expressions) {
        this.expressions = expressions;
    }

    /**
     * Returns the expressions inside this
     * execution scope, never null
     */
    public List<Expression> expressions() {
        return expressions;
    }

    @Override
    public <R> R visit(ExpressionVisitor<R> visitor) {
        return visitor.visitExecutionScope(this);
    }

    @Override
    public String toSource() {
        StringBuilder builder = new StringBuilder("{");
        Iterator<Expression> iterator = expressions.iterator();
        while (iterator.hasNext()) {
            Expression expression = iterator.next();
            builder.append(expression.toSource());
            if (iterator.hasNext()) {
                builder.append("; ");
            }
        }
        return builder.append('}').toString();
    }

    @Override
    public String toString() {
        return "ExecutionScope(" + this.expressions + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionScopeExpression that = (ExecutionScopeExpression) o;
        return expressions.equals(that.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressions);
    }

}
