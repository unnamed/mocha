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

import team.unnamed.molang.lexer.Cursor;
import team.unnamed.molang.parser.ParseException;
import team.unnamed.molang.parser.ast.Expression;
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
public interface MolangEngine {

    static Builder builder() {
        return new Builder();
    }

    static MolangEngine createDefault() {
        return new Builder()
                .withDefaultBindings()
                .build();
    }

    static MolangEngine createEmpty() {
        return new Builder().build();
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

    Object eval(Reader reader) throws ScriptException;

    default Object eval(String script) throws ScriptException {
        try (Reader reader = new StringReader(script)) {
            return eval(reader);
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    class Builder {

        // @VisibleForTesting
        final ObjectBinding bindings = new ObjectBinding();
        ObjectBinding variables;

        public Builder bindVariable(String key, Object binding) {
            ensureBoundVariables();
            variables.setProperty(key, binding);
            return this;
        }

        private void ensureBoundVariables() {
            if (variables == null) {
                variables = new ObjectBinding();
                bindings.setProperty("variable", variables);
                bindings.setProperty("v", variables); // <-- alias
            }
        }

        public Builder withDefaultBindings() {
            bindings.setProperty("query", StandardBindings.QUERY_BINDING);
            bindings.setProperty("math", StandardBindings.MATH_BINDING);
            ensureBoundVariables();
            return this;
        }

        public MolangEngine build() {
            return new MolangEngineImpl(this);
        }

    }

}
