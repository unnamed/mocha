package team.unnamed.molang;

import team.unnamed.molang.binding.Bind;
import team.unnamed.molang.binding.StorageBinding;

import javax.script.ScriptException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public interface MoLangEngine {

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

    static MoLangEngine createDefault() {
        return new Builder()
                .withDefaultBindings()
                .build();
    }

    static MoLangEngine createEmpty() {
        return new Builder().build();
    }

    class Builder {

        final Map<String, Object> bindings = new HashMap<>();

        public Builder withDefaultBindings() {
            bindings.put("query", Bind.QUERY_BINDING);
            bindings.put("math", Bind.MATH_BINDING);
            bindings.put("variable", new StorageBinding());
            return this;
        }

        public MoLangEngine build() {
            return new MoLangEngineImpl(this);
        }

    }

}
