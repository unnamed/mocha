## Getting Started

Welcome to the `mocha` documentation

`mocha` is a lightweight, fast and efficient Molang lexer, parser, interpreter
and compiler for Java 8+. Molang is a simple **expression-based** language designed
for fast and **data-driven** calculation of values at run-time.

For more information about the Molang language, check their official documentation
at [learn.microsoft.com](https://learn.microsoft.com/en-us/minecraft/creator/reference/content/molangreference/examples/molangconcepts/molangintroduction?view=minecraft-bedrock-stable).

### Features

- Fast computing. mocha can compile Molang expressions directly to JVM bytecode,
  no need to parse and interpret the expression every time, zero overhead.
- Extensive API, you can bind objects and functions to the Molang expression
  environment, so that they can be called from the evaluated expressions.
- Lightweight, mocha is a small library with a single dependency: javassist, it's easy to
  integrate in your project.

### mocha vs other Molang libraries

Here are some (reduced) results from a benchmark comparing mocha with other Molang
libraries, ordered from fastest to slowest. *(To run the benchmark yourself, run
`./gradlew jmh`)*

<!--@formatter:off-->
```python
   Benchmark                            Mode      Cnt       Score   Error  Units
1. unnamed's mocha                    sample  8874677       0.035 ± 0.006  us/op
2. MoonflowerTeam's molang-compiler   sample  7507288       0.041 ± 0.007  us/op
3. bedrockk's MoLang                  sample  5404802       6.449 ± 0.158  us/op
```
<!--@formatter:on-->

*To run the benchmark yourself, run `./gradlew jmh`. You can also check the benchmark
source code [here](https://github.com/unnamed/mocha/blob/main/src/jmh/java/team/unnamed/mocha/CompareBenchmark.java)*.