## Usage

Create a new MochaEngine instance with the static factory method `MochaEngine.createStandard()`,
which will create the instance with the standard configuration, where everything tries to be
as spec-compliant as possible.

<!--@formatter:off-->
```java
MochaEngine<?> mocha = MochaEngine.createStandard();
```
<!--@formatter:on-->

Then we can either evaluate (interpret) or compile Molang code.

### Evaluate

To evaluate expressions we can just use the `eval` method.

<!--@formatter:off-->
```java
float result = mocha.eval("math.sqrt(3 * 3 + 4 * 4)");
// evaluates to 5.0

float result2 = mocha.eval("math.abs(-5) + 5");
// evaluates to 10.0
```
<!--@formatter:on-->

Or if we might want to evaluate the same expression multiple times,
we can cache the parsed expressions.

<!--@formatter:off-->
```java
MochaFunction function = mocha.prepareEval("math.sqrt(3 * 3 + 4 * 4)");

function.evaluate();
// evaluates to 5.0

function.evaluate();
// evaluates to 5.0
```
<!--@formatter:on-->

### Compile

Compiling expressions is pretty similar to preparing them and evaluating them
later, but faster.

<!--@formatter:off-->
```java
MochaFunction function = mocha.compile("math.sqrt(3 * 3 + 4 * 4)");
// will compile a class that implements MochaFunction, with the following
// code:
//     return Math.sqrt(25);
// note that the compiler can optimize constant expressions like 3*3+4*4 to just 25

function.evaluate();
// evaluates to 5.0, no interpretation overhead

function.evaluate();
// evaluates to 5.0, no interpretation overhead
```
<!--@formatter:on-->

We could also specify the function type we would like to get.

<!--@formatter:off-->
```java
interface CompareFunction extends MochaCompiledFunction {
    boolean compare(@Named("a") double a, @Named("b") double b);
}

// ...
CompareFunction gt = mocha.compile("a > b", CompareFunction.class);

gt.compare(5, 4);
// true

gt.compare(4, 5);
// false

gt.compare(5, 5);
// false
```
<!--@formatter:on-->