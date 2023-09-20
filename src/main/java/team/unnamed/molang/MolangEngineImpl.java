package team.unnamed.molang;

import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.runtime.ExpressionEvaluator;
import team.unnamed.molang.parser.MolangParser;
import team.unnamed.molang.runtime.binding.ObjectBinding;
import team.unnamed.molang.runtime.binding.StandardBindings;

import javax.script.ScriptException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

final class MolangEngineImpl implements MolangEngine {

    private final ObjectBinding bindings;

    MolangEngineImpl(MolangEngine.Builder builder) {
        this.bindings = builder.bindings;
    }

    private ObjectBinding createBindings() {
        ObjectBinding bindings = new ObjectBinding();
        bindings.setAllFrom(StandardBindings.BUILT_IN);
        bindings.setAllFrom(this.bindings);
        ObjectBinding temp = new ObjectBinding();
        bindings.setProperty("temp", temp);
        bindings.setProperty("t", temp);
        return bindings;
    }

    @Override
    public Object eval(List<Expression> expressions) {
        ObjectBinding bindings = createBindings();
        ExpressionEvaluator evaluator = new ExpressionEvaluator(bindings);
        Object lastResult = 0;

        for (Expression expression : expressions) {
            lastResult = expression.visit(evaluator);
            Object returnValue = evaluator.popReturnValue();
            if (returnValue != null) {
                lastResult = returnValue;
                break;
            }
        }

        return lastResult;
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        try {
            return eval(parse(reader));
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public List<Expression> parse(Reader reader) throws IOException {
        MolangParser parser = MolangParser.parser(reader);
        List<Expression> expressions = new ArrayList<>(8);
        Expression expr;
        while ((expr = parser.next()) != null) {
            expressions.add(expr);
        };
        return expressions;
    }

}
