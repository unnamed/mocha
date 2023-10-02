package team.unnamed.molang.parser;

import team.unnamed.molang.lexer.MolangLexer;
import team.unnamed.molang.lexer.Token;
import team.unnamed.molang.lexer.TokenKind;
import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.lexer.Tokens;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * Standard implementation of {@link MolangParser},
 * it's Hephaestus-MoLang parser since some MoLang
 * characteristics may change
 *
 * @see Tokens
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
        boolean o = attachmentPower == 0;
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
