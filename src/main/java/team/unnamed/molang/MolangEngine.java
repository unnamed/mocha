package team.unnamed.molang;

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

public interface MolangEngine extends MolangParser {

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
