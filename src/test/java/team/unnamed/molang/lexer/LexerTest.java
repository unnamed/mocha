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
package team.unnamed.molang.lexer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static team.unnamed.molang.lexer.TokenKind.*;

public class LexerTest {

    @Test
    @DisplayName("Test lexing Molang")
    public void test() throws IOException {
        assertTokenization("unexpected $ \\ .", IDENTIFIER, ERROR, ERROR, DOT);
        assertTokenization("1 -> 2", FLOAT, ARROW, FLOAT);
        assertTokenization("(hello) * (world)", LPAREN, IDENTIFIER, RPAREN, STAR, LPAREN, IDENTIFIER, RPAREN);
        assertTokenization("error & here", IDENTIFIER, ERROR, IDENTIFIER);
        assertTokenization("this * is * 'string literal'", IDENTIFIER, STAR, IDENTIFIER, STAR, STRING);
        assertTokenization("(whats true and false)", LPAREN, IDENTIFIER, TRUE, IDENTIFIER, FALSE, RPAREN);
        assertTokenization("we_love && we_live || we_lie", IDENTIFIER, AMPAMP, IDENTIFIER, BARBAR, IDENTIFIER);
        assertTokenization("more + tests * on / this", IDENTIFIER, PLUS, IDENTIFIER, STAR, IDENTIFIER, SLASH, IDENTIFIER);
        assertTokenization("q.get_equipped_item_name == 'stick'", IDENTIFIER, DOT, IDENTIFIER, EQEQ, STRING);
        assertTokenization("q.is_sneaking || q.is_jumping", IDENTIFIER, DOT, IDENTIFIER, BARBAR, IDENTIFIER, DOT, IDENTIFIER);
        assertTokenization("q.is_sneaking ? 5", IDENTIFIER, DOT, IDENTIFIER, QUES, FLOAT);
        assertTokenization("q.is_sneaking ? 10 : 3", IDENTIFIER, DOT, IDENTIFIER, QUES, FLOAT, COLON, FLOAT);
        assertTokenization(
                "q.is_sneaking && (q.get_equipped_item_name == 'stick' || q.get_equipped_item_name == 'diamond')",
                IDENTIFIER, DOT, IDENTIFIER, AMPAMP, LPAREN, IDENTIFIER, DOT, IDENTIFIER, EQEQ, STRING, BARBAR, IDENTIFIER,
                DOT, IDENTIFIER, EQEQ, STRING, RPAREN
        );
        assertTokenization(
                "math.cos(query.anim_time * 38) * variable.rotation_scale + variable.x * variable.x * query.life_time;",
                IDENTIFIER, DOT, IDENTIFIER, LPAREN, IDENTIFIER, DOT, IDENTIFIER, STAR, FLOAT, RPAREN, STAR, IDENTIFIER, DOT, IDENTIFIER,
                PLUS, IDENTIFIER, DOT, IDENTIFIER, STAR, IDENTIFIER, DOT, IDENTIFIER, STAR, IDENTIFIER, DOT, IDENTIFIER, SEMICOLON
        );
        assertTokenization(
                "v.x = 0;\n" +
                        "for_each(v.pig, query.get_nearby_entities(4, 'minecraft:pig'), {\n" +
                        "    v.x = v.x + v.pig->query.get_relative_block_state(0, 1, 0, 'flammable');\n" +
                        "});",
                IDENTIFIER, DOT, IDENTIFIER, EQ, FLOAT, SEMICOLON,
                IDENTIFIER, LPAREN, IDENTIFIER, DOT, IDENTIFIER, COMMA, IDENTIFIER, DOT, IDENTIFIER, LPAREN, FLOAT, COMMA, STRING, RPAREN, COMMA, LBRACE,
                IDENTIFIER, DOT, IDENTIFIER, EQ, IDENTIFIER, DOT, IDENTIFIER, PLUS, IDENTIFIER, DOT, IDENTIFIER, ARROW, IDENTIFIER, DOT, IDENTIFIER, LPAREN, FLOAT, COMMA, FLOAT, COMMA, FLOAT, COMMA, STRING, RPAREN, SEMICOLON,
                RBRACE, RPAREN, SEMICOLON
        );
        assertTokenization(
                "v.x = 1;\n" +
                        "v.y = 1;\n" +
                        "loop(10, {\n" +
                        "  t.x = v.x + v.y;\n" +
                        "  v.x = v.y;\n" +
                        "  v.y = t.x;\n" +
                        "});",
                IDENTIFIER, DOT, IDENTIFIER, EQ, FLOAT, SEMICOLON,
                IDENTIFIER, DOT, IDENTIFIER, EQ, FLOAT, SEMICOLON,
                IDENTIFIER, LPAREN, FLOAT, COMMA, LBRACE,
                IDENTIFIER, DOT, IDENTIFIER, EQ, IDENTIFIER, DOT, IDENTIFIER, PLUS, IDENTIFIER, DOT, IDENTIFIER, SEMICOLON,
                IDENTIFIER, DOT, IDENTIFIER, EQ, IDENTIFIER, DOT, IDENTIFIER, SEMICOLON,
                IDENTIFIER, DOT, IDENTIFIER, EQ, IDENTIFIER, DOT, IDENTIFIER, SEMICOLON,
                RBRACE, RPAREN, SEMICOLON
        );
        assertTokenization(
                "variable.x = (variable.x ?? 1.2) + 0.3;",
                IDENTIFIER, DOT, IDENTIFIER, EQ, LPAREN, IDENTIFIER, DOT, IDENTIFIER, QUESQUES, FLOAT, RPAREN, PLUS, FLOAT, SEMICOLON
        );
    }

    private static void assertTokenization(String expr, TokenKind... tokenKinds) throws IOException {
        List<Token> tokens = MolangLexer.tokenizeAll(expr);
        assertEquals(
                tokenKinds.length,
                tokens.size(),
                () -> {
                    StringBuilder builder = new StringBuilder(128)
                            .append("Different length of tokens (")
                            .append("Expected: ")
                            .append(tokenKinds.length)
                            .append(", Got: ")
                            .append(tokens.size())
                            .append(")\n");
                    for (int i = 0; i < Math.max(tokenKinds.length, tokens.size()); i++) {
                        if (i != 0) {
                            builder.append("\n");
                        }
                        String got = i < tokens.size() ? tokens.get(i).kind().toString() : "";
                        String expected = i < tokenKinds.length ? tokenKinds[i].toString() : "";
                        builder.append('\t')
                                .append(got)
                                .append(" (should be ")
                                .append(expected)
                                .append(")");
                        if (!got.equals(expected)) { // we should compare kinds actually
                            builder.append(" <-- DIFFERENT");
                        }
                    }
                    return builder.toString();
                }
        );

        StringBuilder differencesMessage = new StringBuilder(128)
                .append("Different length of tokens (")
                .append("Expected: ")
                .append(tokenKinds.length)
                .append(", Got: ")
                .append(tokens.size())
                .append(")\n");
        boolean different = false;
        for (int i = 0; i < tokenKinds.length; i++) {
            if (i != 0) {
                differencesMessage.append("\n");
            }
            TokenKind expected = tokenKinds[i];
            Token token = tokens.get(i);
            differencesMessage.append('\t')
                    .append(token.kind())
                    .append(" (should be ")
                    .append(expected)
                    .append(")");
            if (token.kind() != expected) {
                differencesMessage.append(" <-- DIFFERENT");
                different = true;
            }
        }

        if (different) {
            fail(differencesMessage.toString());
        }
    }

}
