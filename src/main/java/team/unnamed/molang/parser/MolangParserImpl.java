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

import team.unnamed.molang.lexer.MolangLexer;
import team.unnamed.molang.lexer.Token;
import team.unnamed.molang.lexer.TokenKind;
import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.lexer.Characters;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * Standard implementation of {@link MolangParser},
 * it's Hephaestus-MoLang parser since some MoLang
 * characteristics may change
 *
 * @see Characters
 * @see Expression
 */
final class MolangParserImpl implements MolangParser {

    private final MolangLexer lexer;

    // the last parsed expression, returned by next()
    private Expression current;

    MolangParserImpl(MolangLexer lexer) {
        this.lexer = requireNonNull(lexer, "lexer");
    }

    @Override
    public MolangLexer lexer() {
        return lexer;
    }

    @Override
    public Expression current() {
        if (current == null) {
            throw new IllegalStateException("No current parsed expression, call next() at least once!");
        }
        return current;
    }

    @Override
    public Expression next() throws IOException {
        return current = next0();
    }

    private Expression next0() throws IOException {
        Token token = lexer.next();

        if (token.kind() == TokenKind.EOF) {
            // reached end-of-file!
            return null;
        }

        if (token.kind() == TokenKind.ERROR) {
            // tokenization error!
            throw new ParseException("Found an invalid token (error): " + token.value(), cursor());
        }

        // parse a single expression
        Expression expression = parseCompoundExpression(lexer, 0);

        // update current token
        token = lexer.current();

        if (token.kind() != TokenKind.EOF && token.kind() != TokenKind.SEMICOLON) {
            throw new ParseException("Expected a semicolon, but was " + token, lexer.cursor());
        }

        return expression;
    }

    static Expression parseCompoundExpression(MolangLexer lexer) throws IOException {
        return parseCompoundExpression(lexer, 0);
    }

    static Expression parseCompoundExpression(MolangLexer lexer, int attachmentPower) throws IOException {
        Expression expr = SingleExpressionParser.parseSingle(lexer);
        while (true) {
            Expression compositeExpr = CompoundExpressionParser.parseCompound(lexer, expr, attachmentPower);

            // current token
            Token current = lexer.current();
            if (current.kind() == TokenKind.EOF || current.kind() == TokenKind.SEMICOLON) {
                // found eof, stop parsing, return expr
                return compositeExpr;
            } else if (compositeExpr == expr) {
                return expr;
            }

            expr = compositeExpr;
        }
    }

    @Override
    public void close() throws IOException {
        this.lexer.close();
    }

}
