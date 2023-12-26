package team.unnamed.mocha.runtime.binding;

/**
 * A registered binding. This is either a field or a method.
 *
 * <p>This interface provides two methods to get the value of
 * the binding. One, using the method/field information (reflection),
 * and another, using a previously given value/function (or internally,
 * can use reflection too).</p>
 *
 * <p>The reflective approach is faster for bindings that are used
 * in compiled scripts, while the value/function approach is faster
 * for bindings that are used in interpreted scripts.</p>
 */
interface RegisteredBinding {
}
