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
package team.unnamed.molang;

import org.jetbrains.annotations.NotNull;
import team.unnamed.molang.lexer.Cursor;
import team.unnamed.molang.parser.ParseException;
import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.runtime.Function;
import team.unnamed.molang.runtime.binding.ObjectBinding;
import team.unnamed.molang.runtime.binding.StandardBindings;

import javax.script.ScriptException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

/**
 * The engine's entry class. Provides methods to evaluate
 * and parse Molang code from strings and readers.
 *
 * @since 3.0.0
 */
public interface MolangEngine<T> {
    static <T> MolangEngine<T> create(T entity) {
        return new MolangEngineImpl<>(entity);
    }

    static MolangEngine<?> create() {
        return new MolangEngineImpl<>(null);
    }

    static MolangEngine<?> createDefault() {
        final MolangEngine<?> engine = create();
        engine.bindDefaults();
        return engine;
    }

    /**
     * Parses the data from the given {@code reader}
     * to a {@link List} of {@link Expression}
     *
     * <strong>Note that this method won't close
     * the given {@code reader}</strong>
     *
     * @throws ParseException If read failed or there
     *                        are syntax errors in the script
     */
    List<Expression> parse(Reader reader) throws IOException;

    /**
     * Parses the given {@code string} to a list of
     * {@link Expression}
     *
     * @param string The MoLang string
     * @return The list of parsed expressions
     * @throws ParseException If parsing fails
     */
    default List<Expression> parse(String string) throws ParseException {
        try (Reader reader = new StringReader(string)) {
            return parse(reader);
        } catch (ParseException e) {
            throw e;
        } catch (IOException e) {
            throw new ParseException("Failed to close string reader", e, new Cursor(0, 0));
        }
    }

    Object eval(List<Expression> expressions) throws ScriptException;

    default Object eval(Reader reader) throws ScriptException {
        try {
            return eval(parse(reader));
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    default Object eval(String script) throws ScriptException {
        try (Reader reader = new StringReader(script)) {
            return eval(reader);
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    /**
     * Returns the bindings for this Molang engine
     * instance.
     *
     * @return This engine's bindings
     * @since 3.0.0
     */
    @NotNull ObjectBinding bindings();

    /**
     * Makes this engine use the default bindings.
     *
     * <p>Default bindings include math and query bindings.</p>
     *
     * @since 3.0.0
     */
    default void bindDefaults() {
        final ObjectBinding bindings = bindings();
        bindings.setProperty("query", StandardBindings.QUERY_BINDING);
        bindings.setProperty("q", StandardBindings.QUERY_BINDING);
        bindings.setProperty("math", StandardBindings.MATH_BINDING);
    }

    void bindVariable(String key, Object binding);

    void bindQuery(String key, Object binding);

    default void bindQueryFunction(String key, Function<T> function) {
        bindQuery(key, function);
    }
}
