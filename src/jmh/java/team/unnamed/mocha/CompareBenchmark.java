package team.unnamed.mocha;

import com.bedrockk.molang.MoLang;
import com.bedrockk.molang.parser.Expression;
import com.bedrockk.molang.runtime.MoLangRuntime;
import gg.moonflower.molangcompiler.api.MolangCompiler;
import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import team.unnamed.mocha.runtime.MochaFunction;

import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class CompareBenchmark {
    private MochaEngine<?> mocha; // unnamed's mocha
    private MolangEnvironment environment; // moonflower's molang-compiler
    private MoLangRuntime mlRuntime; // bedrockk's Molang

    private MochaFunction function; // unnamed's mocha
    private MolangExpression mlExpression; // moonflower's molang-compiler
    private List<Expression> mlExpressions; // bedrockk's MoLang

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(CompareBenchmark.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void prepare() throws Exception {
        mocha = MochaEngine.createStandard();
        environment = MolangRuntime.runtime().create();
        mlRuntime = MoLang.newRuntime();

        final String expr = "temp.t = 3; return 3*temp.t*temp.t - 2*temp.t*temp.t*temp.t;";

        function = mocha.compile(expr);
        mlExpression = MolangCompiler.create(MolangCompiler.DEFAULT_FLAGS, getClass().getClassLoader()).compile(expr);
        mlExpressions = MoLang.newParser("temp.t = 3; return 3*temp.t*temp.t - 2*temp.t*temp.t*temp.t;").parse();
    }

    @Benchmark
    public void bedrockk_MoLang() {
        mlRuntime.execute(mlExpressions);
    }

    @Benchmark
    public void unnamed_mocha() {
        function.evaluate();
    }

    @Benchmark
    public void moonflower_molang_compiler() throws Exception {
        //noinspection OverrideOnly
        mlExpression.get(environment);
    }
}
