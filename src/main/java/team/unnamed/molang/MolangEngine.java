package team.unnamed.molang;

import team.unnamed.molang.lexer.Cursor;
import team.unnamed.molang.parser.ParseException;
import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.runtime.binding.Bind;
import team.unnamed.molang.runtime.binding.StorageBinding;
import team.unnamed.molang.parser.MolangParser;

import javax.script.ScriptException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface MolangEngine {

    /**
     * Parses the data from the given {@code reader}
     * to a {@link List} of {@link Expression}
     *
     * <strong>Note that this method won't close
     * the given {@code reader}</strong>
     *
     * @throws ParseException If read failed or there
     * are syntax errors in the script
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

    class Builder {

        final Map<String, Object> bindings = new HashMap<>();
        StorageBinding variables;

        public Builder bindVariable(String key, Object binding) {
            ensureBoundVariables();
            variables.setProperty(key, binding);
            return this;
        }

        private void ensureBoundVariables() {
            if (variables == null) {
                variables = new StorageBinding();
                bindings.put("variable", variables);
                bindings.put("v", variables); // <-- alias
            }
        }

        public Builder withDefaultBindings() {
            bindings.put("query", Bind.QUERY_BINDING);
            bindings.put("math", Bind.MATH_BINDING);
            ensureBoundVariables();
            return this;
        }

        public MolangEngine build() {
            return new MolangEngineImpl(this);
        }

    }

}
