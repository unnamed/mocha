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

import team.unnamed.molang.lexer.Cursor;
import team.unnamed.molang.lexer.MolangLexer;
import team.unnamed.molang.parser.ast.Expression;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

/**
 * Parser for the Molang language.
 *
 * <p>The parser converts token streams to expression
 * streams</p>
 *
 * <p>This is the next level after lexical analysis
 * done by {@link team.unnamed.molang.lexer.MolangLexer}</p>
 *
 * @since 1.0.0
 */
public /* sealed */ interface MolangParser /* permits MolangParserImpl */ extends Closeable {

    MolangLexer lexer();

    default Cursor cursor() {
        return lexer().cursor();
    }

    Expression current();

    Expression next() throws IOException;

    @Override
    void close() throws IOException;

    /**
     * Returns a {@link MolangParser} instance
     *
     * @return The MoLang parser instance
     */
    static MolangParser parser(MolangLexer lexer) throws IOException {
        return new MolangParserImpl(lexer);
    }

    static MolangParser parser(Reader reader) throws IOException {
        return parser(MolangLexer.lexer(reader));
    }

}
