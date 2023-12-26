/*
 * This file is part of mocha, licensed under the MIT license
 *
 * Copyright (c) 2021-2023 Unnamed Team
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
package team.unnamed.mocha.runtime;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Bytecode;
import javassist.bytecode.Descriptor;
import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.parser.ast.*;
import team.unnamed.mocha.runtime.binding.JavaFieldBinding;
import team.unnamed.mocha.runtime.binding.JavaFunction;
import team.unnamed.mocha.runtime.binding.JavaObjectBinding;
import team.unnamed.mocha.runtime.value.Function;
import team.unnamed.mocha.runtime.value.NumberValue;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class MolangCompilingVisitor implements ExpressionVisitor<CompileVisitResult> {
    private static final int[] OPCODES_BY_BINARY_EXPRESSION_OP = new int[]{
            -1, // AND(300),
            -1, //        OR(200),
            Bytecode.IFLT, //        LT(700),
            Bytecode.IFLE, //        LTE(700),
            Bytecode.IFGT, //        GT(700),
            Bytecode.IFGE, //        GTE(700),
            Bytecode.DADD, //        ADD(900),
            Bytecode.DSUB, //        SUB(900),
            Bytecode.DMUL, //        MUL(1000),
            Bytecode.DDIV, //        DIV(1000),
            -1, //        ARROW(2000),
            -1, //        NULL_COALESCE(2),
            -1, //        ASSIGN(1),
            -1, //        CONDITIONAL(1),
            Bytecode.IFEQ, //        EQ(500),
            Bytecode.IFNE //        NEQ(500);
    };

    private final ClassPool classPool;
    private final Bytecode bytecode;
    private final Method method;

    private final FunctionCompileState functionCompileState;
    private final Map<String, Object> requirements;
    private final Map<String, Integer> argumentParameterIndexes;

    private final Map<String, Integer> localsByName = new HashMap<>();

    MolangCompilingVisitor(final @NotNull FunctionCompileState compileState) {
        this.functionCompileState = compileState;
        this.classPool = compileState.classPool();
        this.bytecode = compileState.bytecode();
        this.method = compileState.method();
        this.requirements = compileState.requirements();
        this.argumentParameterIndexes = compileState.argumentParameterIndexes();
    }

    @Override
    public CompileVisitResult visitBinary(final @NotNull BinaryExpression expression) {
        final BinaryExpression.Op op = expression.op();

        if (op == BinaryExpression.Op.ASSIGN) {
            final Expression left = expression.left();
            if (left instanceof AccessExpression) {
                final Expression objectExpr = ((AccessExpression) left).object();
                if (objectExpr instanceof IdentifierExpression) {
                    final String name = ((IdentifierExpression) objectExpr).name();
                    final String property = ((AccessExpression) left).property();

                    if (name.equals("temp") || name.equals("t")) {
                        final CompileVisitResult result = expression.right().visit(this);
                        final int localIndex = localsByName.computeIfAbsent(property, k -> {
                            int index = functionCompileState.maxLocals();
                            if (result.lastPushedType() == CtClass.doubleType || result.lastPushedType() == CtClass.longType) {
                                functionCompileState.maxLocals(index + 2);
                            } else {
                                functionCompileState.maxLocals(index + 1);
                            }
                            return index;
                        });
                        bytecode.addStore(localIndex, CtClass.doubleType);
                        return null;
                    }
                }
            }
        }

        if (!RequiresContextVisitor.test(expression)) {
            // can be evaluated in compile-time
            final double result = expression.visit(ExpressionEvaluator.evaluator()).getAsNumber();
            bytecode.addLdc2w(result);
            return new CompileVisitResult(CtClass.doubleType);
        }

        expression.left().visit(this);   // pushes lhs value to stack
        expression.right().visit(this);  // pushes rhs value to stack

        //@formatter:off
        switch (op) {
            case AND:
            case OR:
                // not implemented
                return new CompileVisitResult(CtClass.doubleType);
            case EQ:
            case NEQ:
            case LT:
            case LTE:
            case GT:
            case GTE: {
                bytecode.addOpcode(Bytecode.DCMPL); // compare both numbers
                bytecode.addOpcode(OPCODES_BY_BINARY_EXPRESSION_OP[op.ordinal()]); // branch
                bytecode.addIndex(7);
                bytecode.addDconst(0D);
                bytecode.addOpcode(Bytecode.GOTO);
                bytecode.addIndex(4);
                bytecode.addDconst(1D);
                return new CompileVisitResult(CtClass.doubleType);
            }
            case ADD:
            case SUB:
            case MUL:
            case DIV: {
                bytecode.addOpcode(OPCODES_BY_BINARY_EXPRESSION_OP[op.ordinal()]);
                return new CompileVisitResult(CtClass.doubleType);
            }
            case ARROW:
            case NULL_COALESCE:
            case CONDITIONAL:
                break;
        }
        //@formatter:on
        return null;
    }

    public void endVisit() {
        try {
            bytecode.addReturn(classPool.get(method.getReturnType().getName()));
        } catch (final NotFoundException e) {
            throw new IllegalStateException("Return type not found", e);
        }
    }

    @Override
    public CompileVisitResult visitDouble(final @NotNull DoubleExpression expression) {
        final double value = expression.value();
        if (value == 1.0D) {
            bytecode.addOpcode(Bytecode.DCONST_1);
        } else if (value == 0.0D) {
            bytecode.addOpcode(Bytecode.DCONST_0);
        } else {
            bytecode.addLdc2w(value);
        }
        return new CompileVisitResult(CtClass.doubleType);
    }

    @Override
    public CompileVisitResult visitString(final @NotNull StringExpression expression) {
        bytecode.addLdc(expression.value());
        try {
            return new CompileVisitResult(classPool.get(String.class.getName()));
        } catch (final NotFoundException e) {
            throw new IllegalStateException("Couldn't find CtClass for String", e);
        }
    }

    @Override
    public CompileVisitResult visitUnary(final @NotNull UnaryExpression expression) {
        final CompileVisitResult result = expression.expression().visit(this); // push value to stack

        switch (expression.op()) {
            case RETURN: {
                endVisit(); // force visit end
                return new CompileVisitResult(result.lastPushedType(), true);
            }
            case LOGICAL_NEGATION: {
                // logical negation with doubles! so fun! (i spent 2 hours in the following 8 lines)
                bytecode.addOpcode(Bytecode.DCONST_0); // push 0
                bytecode.addOpcode(Bytecode.DCMPL);    // compare
                bytecode.addOpcode(Bytecode.IFNE);     // if not equal to 0, skip
                bytecode.addIndex(7);
                bytecode.addDconst(1D); // equal to 0, set to 1
                bytecode.addOpcode(Bytecode.GOTO); // skip next instruction
                bytecode.addIndex(4);
                bytecode.addDconst(0D);
                break;
            }
            case ARITHMETICAL_NEGATION: {
                if (result.is(CtClass.doubleType)) {
                    bytecode.addOpcode(Bytecode.DNEG);
                } else if (result.is(CtClass.longType)) {
                    bytecode.addOpcode(Bytecode.LNEG);
                } else if (result.is(CtClass.floatType)) {
                    bytecode.addOpcode(Bytecode.FNEG);
                } else if (result.is(CtClass.intType)) {
                    bytecode.addOpcode(Bytecode.INEG);
                } else if (result.is(CtClass.booleanType)) {
                    // logical negation
                    bytecode.addOpcode(Bytecode.ICONST_1);
                    bytecode.addOpcode(Bytecode.IXOR);
                } else {
                    throw new IllegalStateException("Unsupported type for negation: " + result);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unsupported unary operator: " + expression.op());
        }
        return null;
    }

    @Override
    public CompileVisitResult visitTernaryConditional(final @NotNull TernaryConditionalExpression expression) {
        final Expression conditionExpr = expression.condition();
        final Expression trueExpr = expression.trueExpression();
        final Expression falseExpr = expression.falseExpression();

        if (!RequiresContextVisitor.test(conditionExpr)) {
            // condition can be evaluated in compile-time
            final boolean condition = conditionExpr.visit(ExpressionEvaluator.evaluator()).getAsBoolean();
            final Expression resultExpr = condition ? trueExpr : falseExpr;
            return resultExpr.visit(this);
        }

        final CompileVisitResult conditionRes = expression.condition().visit(this); // push value to stack
        if (conditionRes != null && conditionRes.lastPushedType() != null && !conditionRes.is(CtClass.booleanType) && !conditionRes.is(CtClass.intType)) {
            bytecode.addConstZero(conditionRes.lastPushedType()); // push 0
            // compare
            if (conditionRes.is(CtClass.doubleType)) {
                bytecode.addOpcode(Bytecode.DCMPL);
            } else if (conditionRes.is(CtClass.floatType)) {
                bytecode.addOpcode(Bytecode.FCMPL);
            } else if (conditionRes.is(CtClass.longType)) {
                bytecode.addOpcode(Bytecode.LCMP);
            } else {
                throw new IllegalStateException("Unsupported type for comparison: " + conditionRes);
            }
        }

        bytecode.addOpcode(Bytecode.IFEQ); // if false skip
        bytecode.addIndex(7);
        final CompileVisitResult trueRes = trueExpr.visit(this); // push true value to stack
        bytecode.addOpcode(Bytecode.GOTO); // skip pushing false value
        bytecode.addIndex(4);
        final CompileVisitResult falseRes = falseExpr.visit(this); // push false value to stack
        return null;
    }

    @Override
    public CompileVisitResult visitIdentifier(final @NotNull IdentifierExpression expression) {
        final String name = expression.name();
        final Integer paramIndex = argumentParameterIndexes.get(name);
        if (paramIndex == null) {
            throw new IllegalStateException("Unknown variable: " + name);
        }

        final Parameter[] parameters = method.getParameters();
        final Parameter parameter = parameters[paramIndex];
        int loadIndex = 1;
        for (int i = 0; i < paramIndex; i++) {
            final Parameter param = parameters[i];
            final Class<?> paramType = param.getType();
            if (paramType.equals(double.class) || paramType.equals(long.class)) {
                loadIndex += 2;
            } else {
                loadIndex += 1;
            }
        }
        try {
            bytecode.addLoad(loadIndex, classPool.get(parameter.getType().getName()));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        // convert to double, most operations are done with doubles
        final Class<?> parameterType = parameter.getType();
        if (parameterType.equals(int.class)) {
            bytecode.addOpcode(Bytecode.I2D);
        } else if (parameterType.equals(float.class)) {
            bytecode.addOpcode(Bytecode.F2D);
        } else if (parameterType.equals(long.class)) {
            bytecode.addOpcode(Bytecode.L2D);
        } else if (parameterType.equals(short.class)) {
            bytecode.addOpcode(Bytecode.I2D);
        } else if (parameterType.equals(byte.class)) {
            bytecode.addOpcode(Bytecode.I2D);
        } else if (parameterType.equals(char.class)) {
            bytecode.addOpcode(Bytecode.I2D);
        } else if (parameterType.equals(boolean.class)) {
            bytecode.addOpcode(Bytecode.I2D);
        }
        return null;
    }

    @Override
    public CompileVisitResult visitAccess(final @NotNull AccessExpression expression) {
        final Expression objectExpr = expression.object();
        final String property = expression.property();

        if (objectExpr instanceof IdentifierExpression) {
            final String name = ((IdentifierExpression) objectExpr).name();
            if (name.equals("temp") || name.equals("t")) {
                // temps are locals
                final Integer localIndex = localsByName.get(property);
                if (localIndex == null) {
                    bytecode.addConstZero(CtClass.doubleType);
                } else {
                    bytecode.addLoad(localIndex, CtClass.doubleType);
                }
                return new CompileVisitResult(CtClass.doubleType);
            }
        }

        final GlobalScope scope = functionCompileState.scope();
        final Value objectValue = objectExpr.visit(new ExpressionVisitor<>() {
            @Override
            public @NotNull Value visitIdentifier(final @NotNull IdentifierExpression expression) {
                final String name = expression.name();
                return scope.get(name);
            }

            @Override
            public @NotNull Value visitAccess(final @NotNull AccessExpression expression) {
                final Value object = expression.object().visit(this);
                if (object instanceof ObjectValue) {
                    return ((ObjectValue) object).get(expression.property());
                } else {
                    return NumberValue.zero();
                }
            }

            @Override
            public @NotNull Value visit(final @NotNull Expression expression) {
                return NumberValue.zero();
            }
        });

        if (objectValue instanceof ObjectValue) {
            final ObjectValue actualObjectValue = (ObjectValue) objectValue;
            if (actualObjectValue instanceof JavaObjectBinding) {
                final JavaFieldBinding javaFieldBinding = ((JavaObjectBinding) actualObjectValue).getField(property);
                if (javaFieldBinding == null) {
                    // push zero only
                    bytecode.addDconst(0D);
                } else if (javaFieldBinding.canBeInlined()) {
                    // inline const
                    bytecode.addDconst(javaFieldBinding.get().getAsNumber());
                } else {
                    // get field
                    final Field field = javaFieldBinding.field();
                    if (Modifier.isStatic(field.getModifiers())) {
                        try {
                            bytecode.addGetstatic(
                                    classPool.get(field.getDeclaringClass().getName()),
                                    field.getName(),
                                    Descriptor.of(field.getType().toString())
                            );
                        } catch (final NotFoundException ignored) {
                        }
                    }
                }
            }
        }

        return null;
    }

    private @NotNull String stringify(final @NotNull Expression expression) {
        if (expression instanceof IdentifierExpression) {
            return ((IdentifierExpression) expression).name();
        } else if (expression instanceof AccessExpression) {
            final AccessExpression access = (AccessExpression) expression;
            return stringify(access.object()) + '.' + access.property();
        } else {
            throw new IllegalStateException("Can't call expression: " + expression);
        }
    }

    @Override
    public CompileVisitResult visitCall(final @NotNull CallExpression expression) {
        final Expression functionExpr = expression.function();
        final GlobalScope scope = functionCompileState.scope();

        final Value functionValue = functionExpr.visit(new ExpressionVisitor<>() {
            @Override
            public @NotNull Value visitIdentifier(final @NotNull IdentifierExpression expression) {
                final String name = expression.name();
                return scope.get(name);
            }

            @Override
            public @NotNull Value visitAccess(final @NotNull AccessExpression expression) {
                final Value object = expression.object().visit(this);
                if (object instanceof ObjectValue) {
                    return ((ObjectValue) object).get(expression.property());
                } else {
                    return NumberValue.zero();
                }
            }

            @Override
            public @NotNull Value visit(final @NotNull Expression expression) {
                return NumberValue.zero();
            }
        });

        if (!(functionValue instanceof Function<?>)) {
            // not a function, just add 0
            bytecode.addDconst(0D);
            return new CompileVisitResult(CtClass.doubleType);
        }

        final Function<?> function = (Function<?>) functionValue;

        if (function instanceof JavaFunction<?>) {
            // we can compile to directly call this function (Java Method)
            final JavaFunction<?> javaFunction = (JavaFunction<?>) function;
            final Method nativeMethod = javaFunction.method();
            final Parameter[] parameters = nativeMethod.getParameters();
            final List<Expression> arguments = expression.arguments();

            final CtClass[] ctParameters = new CtClass[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                final Parameter parameter = parameters[i];
                try {
                    ctParameters[i] = classPool.get(parameter.getType().getName());
                } catch (final NotFoundException e) {
                    throw new IllegalStateException("Parameter type not found", e);
                }
            }

            final Object object = javaFunction.object();

            // load arguments
            final Iterator<Expression> it = arguments.iterator();
            for (int i = 0; i < parameters.length; i++) {
                if (!it.hasNext()) {
                    bytecode.addConstZero(ctParameters[i]);
                    continue;
                }

                it.next().visit(this);
            }

            if (Modifier.isStatic(nativeMethod.getModifiers())) {
                // invoke static
                try {
                    final CtClass ctReturnType = classPool.get(nativeMethod.getReturnType().getName());
                    bytecode.addInvokestatic(
                            classPool.get(nativeMethod.getDeclaringClass().getName()),
                            nativeMethod.getName(),
                            ctReturnType,
                            ctParameters
                    );

                    if (nativeMethod.getReturnType() != double.class) {
                        JavaTypes.addCast(bytecode, ctReturnType, CtClass.doubleType);
                    }
                } catch (final NotFoundException e) {
                    throw new IllegalStateException("Method not found", e);
                }
            } else {
                throw new UnsupportedOperationException("Instance calls are not supported yet (" + nativeMethod + ")");
                // requirements.put(_native.objectName(), object);

                // we must load object
                // bytecode.addAload(0);
                // bytecode.addGetfield(
                //classPool.get(method.getDeclaringClass().getName()),
                //_native.objectName(),
                //Descriptor.of(classPool.get(object.getClass().getName()))
                //);
            }
        }
        return null;
    }

    @Override
    public CompileVisitResult visit(final @NotNull Expression expression) {
        throw new UnsupportedOperationException("Unsupported expression type: " + expression);
    }
}
