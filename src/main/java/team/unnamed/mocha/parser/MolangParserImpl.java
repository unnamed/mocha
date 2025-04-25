/*
 * This file is part of mocha, licensed under the MIT license
 *
 * Copyright (c) 2021-2025 Unnamed Team
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
package team.unnamed.mocha.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.lexer.MolangLexer;
import team.unnamed.mocha.lexer.Token;
import team.unnamed.mocha.lexer.TokenKind;
import team.unnamed.mocha.parser.ast.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

final class MolangParserImpl implements MolangParser {

    private static final Object UNSET_FLAG = new Object();

    private final MolangLexer lexer;

    // the last parsed expression, returned by next()
    // we have to use Object and a flag since null is a valid value too
    private @Nullable Object current = UNSET_FLAG;

    MolangParserImpl(final @NotNull MolangLexer lexer) {
        this.lexer = requireNonNull(lexer, "lexer");
    }

    //
    // Parses a single expression.
    // Single expressions don't require a left-hand expression
    // to be parsed, e.g. literals, statements, identifiers,
    // wrapped expressions and execution scopes
    //
    static @NotNull Expression parseSingle(final @NotNull MolangLexer lexer) throws IOException {
        Token token = lexer.current();
        switch (token.kind()) {
            case FLOAT:
                lexer.next();
                return FloatExpression.of(Float.parseFloat(token.value()));
            case STRING:
                lexer.next();
                return new StringExpression(token.value());
            case TRUE:
                lexer.next();
                return FloatExpression.ONE;
            case FALSE:
                lexer.next();
                return FloatExpression.ZERO;
            case LPAREN:
                lexer.next();
                // wrapped expression: (expression)
                Expression expression = MolangParserImpl.parseCompoundExpression(lexer, 0);
                token = lexer.current();
                if (token.kind() != TokenKind.RPAREN) {
                    throw new ParseException("Non closed expression", lexer.cursor());
                }
                lexer.next();
                return expression;
            case LBRACE:
                lexer.next();
                List<Expression> expressions = new ArrayList<>();
                while (true) {
                    expressions.add(MolangParserImpl.parseCompoundExpression(lexer, 0));
                    token = lexer.current();
                    if (token.kind() == TokenKind.RBRACE) {
                        lexer.next();
                        break;
                    } else if (token.kind() == TokenKind.EOF) {
                        // end reached but not closed yet, huh?
                        throw new ParseException("Found the end before the execution scope closing token", lexer.cursor());
                    } else if (token.kind() == TokenKind.ERROR) {
                        throw new ParseException("Found an invalid token (error): " + token.value(), lexer.cursor());
                    } else {
                        if (token.kind() != TokenKind.SEMICOLON) {
                            throw new ParseException("Missing semicolon", lexer.cursor());
                        }
                        lexer.next();
                    }
                }
                return new ExecutionScopeExpression(expressions);
            case BREAK:
                lexer.next();
                return new StatementExpression(StatementExpression.Op.BREAK);
            case CONTINUE:
                lexer.next();
                return new StatementExpression(StatementExpression.Op.CONTINUE);
            case IDENTIFIER:
                Expression expr = new IdentifierExpression(token.value());
                token = lexer.next();
                while (token.kind() == TokenKind.DOT) {
                    token = lexer.next();

                    if (token.kind() != TokenKind.IDENTIFIER) {
                        throw new ParseException("Unexpected token, expected a valid field token", lexer.cursor());
                    }

                    expr = new AccessExpression(expr, token.value());
                    token = lexer.next();
                }
                return expr;
            case SUB:
                lexer.next();
                final Expression operatedExpression = parseSingle(lexer);
                if (operatedExpression instanceof FloatExpression) {
                    // NEGATE(A) is just parsed as (-A)
                    return FloatExpression.of(-((FloatExpression) operatedExpression).value());
                }
                return new UnaryExpression(UnaryExpression.Op.ARITHMETICAL_NEGATION, operatedExpression);
            case BANG:
                lexer.next();
                return new UnaryExpression(UnaryExpression.Op.LOGICAL_NEGATION, parseSingle(lexer));
            case RETURN:
                lexer.next();
                return new UnaryExpression(UnaryExpression.Op.RETURN, MolangParserImpl.parseCompoundExpression(lexer, 0));
        }

        return FloatExpression.ZERO;
    }

    static @NotNull Expression parseCompoundExpression(
            final @NotNull MolangLexer lexer,
            final int lastPrecedence
    ) throws IOException {
        Expression expr = parseSingle(lexer);
        while (true) {
            final Expression compoundExpr = parseCompound(lexer, expr, lastPrecedence);

            // current token
            final Token current = lexer.current();
            if (current.kind() == TokenKind.EOF || current.kind() == TokenKind.SEMICOLON) {
                // found eof, stop parsing, return expr
                return compoundExpr;
            } else if (compoundExpr == expr) {
                return expr;
            }

            expr = compoundExpr;
        }
    }

    static @NotNull Expression parseCompound(
            final @NotNull MolangLexer lexer,
            final @NotNull Expression left,
            final int lastPrecedence
    ) throws IOException {
        Token current = lexer.current();

        switch (current.kind()) {
            case RPAREN:
            case EOF:
                return left;
            case LBRACKET: { // ARRAY ACCESS EXPRESSION: "left["
                current = lexer.next();
                if (current.kind() == TokenKind.RBRACKET) {
                    throw new ParseException("Expected a expression, got RBRACKET", lexer.cursor());
                } else if (current.kind() == TokenKind.EOF) {
                    throw new ParseException("Found EOF before closing RBRACKET", lexer.cursor());
                }

                final Expression index = parseCompoundExpression(lexer, 0);

                current = lexer.current();
                if (current.kind() == TokenKind.EOF) {
                    throw new ParseException("Found EOF before closing RBRACKET", lexer.cursor());
                } else if (current.kind() != TokenKind.RBRACKET) {
                    throw new ParseException("Expected a closing RBRACKET, found " + current, lexer.cursor());
                }

                lexer.next();
                return new ArrayAccessExpression(left, index);
            }
            case LPAREN: { // CALL EXPRESSION: "left("
                current = lexer.next();
                final List<Expression> arguments = new ArrayList<>();

                // start reading the arguments
                if (current.kind() == TokenKind.EOF) {
                    throw new ParseException("Found EOF before closing RPAREN", lexer.cursor());
                } else if (current.kind() == TokenKind.RPAREN) {
                    // immediately closed
                    lexer.next();
                } else {
                    while (true) {
                        arguments.add(MolangParserImpl.parseCompoundExpression(lexer, 0));
                        // update current character
                        current = lexer.current();
                        if (current.kind() == TokenKind.EOF) {
                            throw new ParseException("Found EOF before closing RPAREN", lexer.cursor());
                        } else if (current.kind() == TokenKind.RPAREN) {
                            lexer.next();
                            break;
                        } else {
                            if (current.kind() == TokenKind.ERROR) {
                                throw new ParseException("Found error token: " + current.value(), lexer.cursor());
                            } else if (current.kind() != TokenKind.COMMA) {
                                throw new ParseException("Expected a comma, got " + current.kind(), lexer.cursor());
                            }
                            lexer.next();
                        }
                    }
                }

                return new CallExpression(left, arguments);
            }
            case QUES: {
                // ternary precedence is the same as the conditional operator
                final int ternaryPrecedence = BinaryExpression.Op.CONDITIONAL.precedence();
                if (lastPrecedence >= ternaryPrecedence) {
                    return left;
                }

                lexer.next();
                final Expression trueValue = MolangParserImpl.parseCompoundExpression(lexer, 0);

                if (lexer.current().kind() == TokenKind.COLON) {
                    // then it's a ternary expression, since there is a ':', indicating the next expression
                    lexer.next();
                    return new TernaryConditionalExpression(left, trueValue, MolangParserImpl.parseCompoundExpression(lexer, ternaryPrecedence));
                } else {
                    return new BinaryExpression(BinaryExpression.Op.CONDITIONAL, left, trueValue);
                }
            }
        }

        // check for binary expressions
        final BinaryExpression.Op op;

        // @formatter:off
        // I wish this was java 17
        switch (current.kind()) {
            case AMPAMP: op = BinaryExpression.Op.AND; break;
            case BARBAR: op = BinaryExpression.Op.OR; break;
            case LT: op = BinaryExpression.Op.LT; break;
            case LTE: op = BinaryExpression.Op.LTE; break;
            case GT: op = BinaryExpression.Op.GT; break;
            case GTE: op = BinaryExpression.Op.GTE; break;
            case PLUS: op = BinaryExpression.Op.ADD; break;
            case SUB: op = BinaryExpression.Op.SUB; break;
            case STAR: op = BinaryExpression.Op.MUL; break;
            case SLASH: op = BinaryExpression.Op.DIV; break;
            case QUESQUES: op = BinaryExpression.Op.NULL_COALESCE; break;
            case EQ: op = BinaryExpression.Op.ASSIGN; break;
            case EQEQ: op = BinaryExpression.Op.EQ; break;
            case BANGEQ: op = BinaryExpression.Op.NEQ; break;
            case ARROW: op = BinaryExpression.Op.ARROW; break;
            default: return left;
        }
        // @formatter:on

        final int precedence = op.precedence();
        if (lastPrecedence >= precedence) {
            return left;
        }

        lexer.next();
        return new BinaryExpression(op, left, MolangParserImpl.parseCompoundExpression(lexer, precedence));
    }

    @Override
    public @NotNull MolangLexer lexer() {
        return lexer;
    }

    @Override
    public @Nullable Expression current() {
        if (current == UNSET_FLAG) {
            throw new IllegalStateException("No current parsed expression, call next() at least once!");
        }
        return (Expression) current;
    }

    @Override
    public @Nullable Expression next() throws IOException {
        final Expression expr = next0();
        current = expr;
        return expr;
    }

    //
    // Parses an expression until it finds an unexpected token,
    // a semicolon, or an end-of-file token.
    //
    private @Nullable Expression next0() throws IOException {
        Token token = lexer.next();

        if (token.kind() == TokenKind.EOF) {
            // reached end-of-file!
            return null;
        }

        if (token.kind() == TokenKind.ERROR) {
            // tokenization error!
            throw new ParseException("Found an invalid token (error): " + token.value(), cursor());
        }

        final Expression expression = parseCompoundExpression(lexer, 0);

        // check current token, should be a semicolon or an eof
        token = lexer.current();
        if (token.kind() != TokenKind.EOF && token.kind() != TokenKind.SEMICOLON) {
            throw new ParseException("Expected a semicolon, but was " + token, lexer.cursor());
        }

        return expression;
    }

    @Override
    public void close() throws IOException {
        this.lexer.close();
    }

}