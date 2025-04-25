## Bindings

You can bind functions by creating a holder class with static methods and
annotating everything with `@Binding`, see the example below

<!--@formatter:off-->
```java
@Binding("random")
public class RandomBinding {
    // random.select(a, b)
    @Binding("select")
    public static float select(float a, float b) {
        return Math.random() < 0.5 ? a : b;
    }
}

// ...
MochaEngine<?> mocha = MochaEngine.createStandard();

mocha.bind(RandomBinding.class);

mocha.compile("random.select(1, 2)").evaluate(); // evaluates to either 1 or 2
// generates the following code:
//     return RandomBinding.select(1, 2);
```
<!--@formatter:on-->