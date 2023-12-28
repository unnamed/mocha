## Bindings

There are two ways to bind a external function/property to the Mocha environment so that
it can be used from the used expressions.

### 1. Function interface

The way is faster for interpreted expressions, but slower for compiled expressions.

<!--@formatter:off-->
```java
MochaEngine<?> mocha = MochaEngine.createStandard();

// query.get_age()
mocha.scope().query().set("get_age", (ctx, args) -> {
    return NumberValue.of(18);
});

mocha.eval("query.get_age()"); // evaluates to 18
```
<!--@formatter:on-->

### 2. Java methods

This way is faster for compiled expressions, since they can directly call the methods, but
it is a lot slower for interpreted expressions, since they have to use reflection to call
them.

<!--@formatter:off-->
```java
@Binding("random")
public class RandomBinding {
    // random.select(a, b)
    @Binding("select")
    public static double select(double a, double b) {
        return Math.random() < 0.5 ? a : b;
    }
}

// ...
MochaEngine<?> mocha = MochaEngine.createStandard();

mocha.scope().forceSet("random", JavaObjectBinding.of(RandomBinding.class, new RandomBinding()));

mocha.compile("random.select(1, 2)").evaluate(); // evaluates to either 1 or 2
// generates the following code:
//     return RandomBinding.select(1, 2);
```
<!--@formatter:on-->

### 3. Use both

Mocha allows you to use both approaches so that you can use the best of both worlds.
With this approach, compiled expressions will prefer the Java methods, while interpreted
expressions will prefer the function interfaces.

<!--@formatter:off-->
```java
@Binding("random")
public class RandomBinding implements ObjectValue {
    // random.select(a, b)
    @Binding("select")
    public static double select(double a, double b) {
        return Math.random() < 0.5 ? a : b;
    }
    
    @Override
    public @NotNull Value get(String name) {
        if (name.equals("select")) {
            return (Function<?>) (ctx, args) ->
                    NumberValue.of(select(args[0].asDouble(), args[1].asDouble()));
        }
        return Value.nil();
    }
}

mocha.scope().forceSet("random", JavaObjectBinding.of(RandomBinding.class, new RandomBinding()));

mocha.compile("random.select(1, 2)").evaluate(); // uses Java method

mocha.evaluate("random.select(1, 2)"); // uses Function
```
<!--@formatter:on-->