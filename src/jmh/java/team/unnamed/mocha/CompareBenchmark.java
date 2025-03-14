/*
 * This file is part of mocha, licensed under the MIT license
 *
 * Copyright (c) 2021-2025 Unnamed Team
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
