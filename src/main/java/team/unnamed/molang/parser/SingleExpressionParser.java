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

package team.unnamed.molang.parser;

import team.unnamed.molang.parser.ast.*;
import team.unnamed.molang.lexer.MolangLexer;
import team.unnamed.molang.lexer.Token;
import team.unnamed.molang.lexer.TokenKind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class SingleExpressionParser {

    private SingleExpressionParser() {
    }

    static Expression parseSingle(MolangLexer lexer) throws IOException {
        Token token = lexer.current();
        switch (token.kind()) {
            case FLOAT: {
                // found a float literal token!
                lexer.next();
                return new DoubleExpression(Double.parseDouble(token.value()));
            }
            case STRING: {
                // found a string literal token!
                lexer.next();
                return new StringExpression(token.value());
            }
            case TRUE: {
                // found a boolean literal token!
                lexer.next();
                return new DoubleExpression(1.0D);
            }
            case FALSE: {
                // found a false literal token!
                lexer.next();
                return new DoubleExpression(0.0D);
            }
            case LPAREN: {
                lexer.next();
                // wrapped expression: (expression)
                Expression expression = MolangParserImpl.parseCompoundExpression(lexer);
                token = lexer.current();
                if (token.kind() != TokenKind.RPAREN) {
                    throw new ParseException("Non closed expression", null);
                }
                lexer.next();
                return expression;
            }
            case LBRACE: {
                lexer.next();
                List<Expression> expressions = new ArrayList<>();
                while (true) {
                    expressions.add(MolangParserImpl.parseCompoundExpression(lexer));
                    token = lexer.current();
                    if (token.kind() == TokenKind.RBRACE) {
                        lexer.next();
                        break;
                    } else if (token.kind() == TokenKind.EOF) {
                        // end reached but not closed yet huh?
                        throw new ParseException(
                                "Found the end before the execution scope closing token",
                                null
                        );
                    } else {
                        if (token.kind() != TokenKind.SEMICOLON) {
                            throw new ParseException("Missing semicolon", null);
                        }
                        lexer.next();
                    }
                }
                return new ExecutionScopeExpression(expressions);
            }
            case BREAK: {
                lexer.next();
                return new StatementExpression(StatementExpression.Op.BREAK);
            }
            case CONTINUE: {
                lexer.next();
                return new StatementExpression(StatementExpression.Op.CONTINUE);
            }
            case IDENTIFIER: {
                Expression expr = new IdentifierExpression(token.value());
                token = lexer.next();
                while (token.kind() == TokenKind.DOT) {
                    token = lexer.next();

                    if (token.kind() != TokenKind.IDENTIFIER) {
                        throw new ParseException("Unexpected token, expected a valid field token", null);
                    }

                    expr = new AccessExpression(expr, token.value());
                    token = lexer.next();
                }
                return expr;
            }
            case SUB: {
                lexer.next();
                return new UnaryExpression(UnaryExpression.Op.ARITHMETICAL_NEGATION, parseSingle(lexer));
            }
            case BANG: {
                lexer.next();
                return new UnaryExpression(UnaryExpression.Op.LOGICAL_NEGATION, parseSingle(lexer));
            }
            case RETURN: {
                lexer.next();
                return new UnaryExpression(UnaryExpression.Op.RETURN, MolangParserImpl.parseCompoundExpression(lexer));
            }
        }

        return new DoubleExpression(0F);
    }

}
