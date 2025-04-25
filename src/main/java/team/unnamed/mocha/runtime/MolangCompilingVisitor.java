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
package team.unnamed.mocha.runtime;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Bytecode;
import javassist.bytecode.Descriptor;
import javassist.bytecode.Opcode;
import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.parser.ast.*;
import team.unnamed.mocha.runtime.binding.Entity;
import team.unnamed.mocha.runtime.binding.JavaFieldBinding;
import team.unnamed.mocha.runtime.binding.JavaFunction;
import team.unnamed.mocha.runtime.binding.JavaObjectBinding;
import team.unnamed.mocha.runtime.value.Function;
import team.unnamed.mocha.runtime.value.NumberValue;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;
import team.unnamed.mocha.util.CaseInsensitiveStringHashMap;
import team.unnamed.mocha.util.JavassistUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
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
            Bytecode.FADD, //        ADD(900),
            Bytecode.FSUB, //        SUB(900),
            Bytecode.FMUL, //        MUL(1000),
            Bytecode.FDIV, //        DIV(1000),
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

    private final Map<String, Integer> localsByName = new CaseInsensitiveStringHashMap<>();

    private final CtClass stringCtType;
    /**
     * The method return type
     */
    private final CtClass methodReturnType;
    /**
     * The type that the current visitor method is expecting
     * to be pushed to the stack.
     */
    private CtClass expectedType;

    MolangCompilingVisitor(final @NotNull FunctionCompileState compileState) {
        this.functionCompileState = compileState;
        this.classPool = compileState.classPool();
        this.bytecode = compileState.bytecode();
        this.method = compileState.method();
        this.requirements = compileState.requirements();
        this.argumentParameterIndexes = compileState.argumentParameterIndexes();

        try {
            this.stringCtType = classPool.get(String.class.getName());
            this.methodReturnType = classPool.get(method.getReturnType().getName());
        } catch (final NotFoundException e) {
            throw new IllegalStateException("Couldn't find CtClass for standard classes", e);
        }

        expectedType = methodReturnType;
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
                        bytecode.addFstore(localIndex);
                        return null;
                    }
                }
            }
        }

        final CtClass currentExpectedType = expectedType;

        //@formatter:off
        switch (op) {
            case AND: {
                final int const_0;
                final int const_1;

                if (expectedType == CtClass.doubleType) {
                    const_0 = Bytecode.DCONST_0;
                    const_1 = Bytecode.DCONST_1;
                } else if (expectedType == CtClass.floatType) {
                    const_0 = Bytecode.FCONST_0;
                    const_1 = Bytecode.FCONST_1;
                } else if (expectedType == CtClass.longType) {
                    const_0 = Bytecode.LCONST_0;
                    const_1 = Bytecode.LCONST_1;
                } else {
                    const_0 = Bytecode.ICONST_0;
                    const_1 = Bytecode.ICONST_1;
                }

                expectedType = CtClass.booleanType;
                expression.left().visit(this); // pushes lhs value to stack as boolean
                bytecode.addOpcode(Bytecode.IFEQ); // if lhs is false set to zero
                final int indexPc = bytecode.currentPc();
                bytecode.addGap(2); // index1, index2 (we don't know how many bytes the next instruction will take)
                expression.right().visit(this); // pushes rhs value to stack as boolean
                bytecode.addOpcode(Bytecode.IFEQ); // if rhs is false set to zero
                bytecode.addIndex(7); // index1, index2, const_1, goto, index1, index2, const_0
                bytecode.addOpcode(const_1);
                bytecode.addOpcode(Bytecode.GOTO);
                bytecode.addIndex(4); // index1, index2, const_0, ((?))
                // jump here!
                bytecode.write16bit(indexPc, bytecode.currentPc() - indexPc + 1);
                bytecode.addOpcode(const_0);
                expectedType = currentExpectedType;
                return new CompileVisitResult(currentExpectedType);
            }
            case OR: {
                final int const_0;
                final int const_1;

                if (expectedType == CtClass.doubleType) {
                    const_0 = Bytecode.DCONST_0;
                    const_1 = Bytecode.DCONST_1;
                } else if (expectedType == CtClass.floatType) {
                    const_0 = Bytecode.FCONST_0;
                    const_1 = Bytecode.FCONST_1;
                } else if (expectedType == CtClass.longType) {
                    const_0 = Bytecode.LCONST_0;
                    const_1 = Bytecode.LCONST_1;
                } else {
                    const_0 = Bytecode.ICONST_0;
                    const_1 = Bytecode.ICONST_1;
                }

                expectedType = CtClass.booleanType;
                expression.left().visit(this); // pushes lhs value to stack as boolean
                bytecode.addOpcode(Bytecode.IFNE); // if lhs is false set to zero
                final int indexPc = bytecode.currentPc();
                bytecode.addGap(2); // index1, index2 (we don't know how many bytes the next instruction will take)
                expression.right().visit(this); // pushes rhs value to stack as boolean
                bytecode.addOpcode(Bytecode.IFEQ); // if rhs is false set to zero
                bytecode.addIndex(7); // index1, index2, const_1, goto, index1, index2, const_0
                // jump here!
                bytecode.write16bit(indexPc, bytecode.currentPc() - indexPc + 1);
                bytecode.addOpcode(const_1);
                bytecode.addOpcode(Bytecode.GOTO);
                bytecode.addIndex(4); // index1, index2, const_0, ((?))
                bytecode.addOpcode(const_0);
                expectedType = currentExpectedType;
                return new CompileVisitResult(currentExpectedType);
            }
            case EQ:
            case NEQ:
            case LT:
            case LTE:
            case GT:
            case GTE: {
                expectedType = CtClass.floatType;
                expression.left().visit(this);   // pushes lhs value to stack
                expression.right().visit(this);  // pushes rhs value to stack
                expectedType = currentExpectedType;

                final int const_0;
                final int const_1;

                if (expectedType == CtClass.doubleType) {
                    const_0 = Bytecode.DCONST_0;
                    const_1 = Bytecode.DCONST_1;
                } else if (expectedType == CtClass.floatType) {
                    const_0 = Bytecode.FCONST_0;
                    const_1 = Bytecode.FCONST_1;
                } else if (expectedType == CtClass.longType) {
                    const_0 = Bytecode.LCONST_0;
                    const_1 = Bytecode.LCONST_1;
                } else {
                    const_0 = Bytecode.ICONST_0;
                    const_1 = Bytecode.ICONST_1;
                }

                bytecode.addOpcode(Bytecode.FCMPL); // compare both numbers
                bytecode.addOpcode(OPCODES_BY_BINARY_EXPRESSION_OP[op.ordinal()]); // branch
                bytecode.addIndex(7);
                bytecode.addOpcode(const_0);
                bytecode.addOpcode(Bytecode.GOTO);
                bytecode.addIndex(4);
                bytecode.addOpcode(const_1);
                return new CompileVisitResult(expectedType == null ? CtClass.booleanType : expectedType);
            }
            case ADD:
            case SUB:
            case MUL:
            case DIV: {
                expectedType = CtClass.floatType;
                expression.left().visit(this);   // pushes lhs value to stack
                expression.right().visit(this);  // pushes rhs value to stack
                expectedType = currentExpectedType;

                bytecode.addOpcode(OPCODES_BY_BINARY_EXPRESSION_OP[op.ordinal()]);
                return new CompileVisitResult(CtClass.floatType);
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
        bytecode.addReturn(methodReturnType);
    }

    @Override
    public @NotNull CompileVisitResult visitFloat(final @NotNull FloatExpression expression) {
        final float value = expression.value();
        if (expectedType == CtClass.voidType) {
            // nothing!
            return new CompileVisitResult(CtClass.voidType);
        } else if (expectedType == null || expectedType == CtClass.floatType) {
            // expects a float, happy!
            bytecode.addFconst(value);
            return new CompileVisitResult(CtClass.floatType);
        } else if (expectedType == CtClass.booleanType) {
            // expects a boolean, push boolean
            if (value != 0.0D) {
                bytecode.addOpcode(Bytecode.ICONST_1);
            } else {
                bytecode.addOpcode(Bytecode.ICONST_0);
            }
            return new CompileVisitResult(CtClass.booleanType);
        } else if (expectedType == CtClass.intType) {
            // expects an int, push int
            bytecode.addLdc((int) value);
            return new CompileVisitResult(CtClass.intType);
        } else if (expectedType == CtClass.longType) {
            // expects a long, push long
            bytecode.addLdc2w((long) value);
            return new CompileVisitResult(CtClass.longType);
        } else {
            System.err.println("[warning] expected type " + expectedType + " has no possible cast from float (" + expression + ")");
            // evaluate to zero
            bytecode.addConstZero(expectedType);
            return new CompileVisitResult(expectedType);
        }
    }

    @Override
    public @NotNull CompileVisitResult visitString(final @NotNull StringExpression expression) {
        if (expectedType == CtClass.voidType) {
            // nothing!
            return new CompileVisitResult(CtClass.voidType);
        } else if (expectedType == null || expectedType == stringCtType) {
            // expected a string, happy
            bytecode.addLdc(expression.value());
            return new CompileVisitResult(stringCtType);
        } else {
            // evaluate to zero
            bytecode.addConstZero(expectedType);
            return new CompileVisitResult(expectedType);
        }
    }

    @Override
    public @NotNull CompileVisitResult visitUnary(final @NotNull UnaryExpression expression) {
        switch (expression.op()) {
            case RETURN: {
                expectedType = methodReturnType;
                expression.expression().visit(this);
                expectedType = null;
                bytecode.addReturn(methodReturnType);
                return new CompileVisitResult(methodReturnType, true);
            }
            case LOGICAL_NEGATION: {
                if (expectedType == CtClass.voidType) {
                    // void,
                    // we must evaluate in case of weird expressions
                    // like: !query.print('hello')
                    // won't push anything since expectedType is set to voidType
                    expression.expression().visit(this);
                    return new CompileVisitResult(CtClass.voidType);
                }

                final CtClass currentExpectedType = expectedType;

                if (currentExpectedType != null && !currentExpectedType.isPrimitive()) {
                    // an unknown Object type, evaluate without pushing anything
                    // and then just push null in the stack
                    expectedType = CtClass.voidType; // set to void so that doesn't push anything
                    expression.expression().visit(this);
                    expectedType = currentExpectedType;
                    bytecode.addConstZero(currentExpectedType);
                    return new CompileVisitResult(currentExpectedType);
                }

                // todo: wrap primitives to their wrapper class if needed

                expectedType = CtClass.booleanType;
                expression.expression().visit(this); // push boolean value to stack
                expectedType = currentExpectedType;

                if (currentExpectedType == CtClass.booleanType) {
                    // booleans just leave it ready to branch
                    bytecode.addOpcode(Bytecode.IFNE);
                    return new CompileVisitResult(CtClass.booleanType);
                }

                final int const_1;
                final int const_0;

                if (currentExpectedType == CtClass.doubleType) {
                    const_1 = Bytecode.DCONST_1;
                    const_0 = Bytecode.DCONST_0;
                } else if (currentExpectedType == CtClass.floatType) {
                    const_1 = Bytecode.FCONST_1;
                    const_0 = Bytecode.FCONST_0;
                } else if (currentExpectedType == CtClass.longType) {
                    const_1 = Bytecode.LCONST_1;
                    const_0 = Bytecode.LCONST_0;
                } else {
                    const_1 = Bytecode.ICONST_1;
                    const_0 = Bytecode.ICONST_0;
                }

                bytecode.addOpcode(Bytecode.IFNE);
                bytecode.addIndex(7); // index1, index2, const_1, goto, index1, index2, ((const_0))
                bytecode.addOpcode(const_1);
                bytecode.addOpcode(Bytecode.GOTO);
                bytecode.addIndex(4); // index1, index2, const_0, ((?))
                bytecode.addOpcode(const_0);
                return new CompileVisitResult(currentExpectedType);
            }
            case ARITHMETICAL_NEGATION: {
                final CompileVisitResult result = expression.expression().visit(this); // push value to stack
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
    public @NotNull CompileVisitResult visitTernaryConditional(final @NotNull TernaryConditionalExpression expression) {
        final Expression trueExpr = expression.trueExpression();
        final Expression falseExpr = expression.falseExpression();

        final CtClass currentExpectedType = expectedType;
        expectedType = CtClass.booleanType;
        final CompileVisitResult conditionRes = expression.condition().visit(this); // push boolean value to stack
        expectedType = currentExpectedType;

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
        final int indexPc = bytecode.currentPc();
        bytecode.addGap(2);
        trueExpr.visit(this); // push true value to stack
        bytecode.addOpcode(Bytecode.GOTO); // skip pushing false value
        final int indexPc2 = bytecode.currentPc();
        bytecode.addGap(2);
        // jump here if false
        bytecode.write16bit(indexPc, bytecode.currentPc() - indexPc + 1);
        falseExpr.visit(this); // push false value to stack
        // jump here if true
        bytecode.write16bit(indexPc2, bytecode.currentPc() - indexPc2 + 1);
        return new CompileVisitResult(currentExpectedType);
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

        final CtClass parameterCtType;

        try {
            parameterCtType = classPool.get(parameter.getType().getName());
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }

        bytecode.addLoad(loadIndex, parameterCtType);

        if (expectedType == null) {
            // we are free to use anything, no need to cast
            return new CompileVisitResult(parameterCtType);
        }

        // convert to the expected type
        JavassistUtil.addCast(bytecode, parameterCtType, expectedType);
        return new CompileVisitResult(expectedType);
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
                    bytecode.addConstZero(CtClass.floatType);
                } else {
                    bytecode.addFload(localIndex);
                }
                return new CompileVisitResult(CtClass.floatType);
            }
        }

        final Scope scope = functionCompileState.scope();
        final Value objectValue = objectExpr.visit(new ExpressionVisitor<Value>() {
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
                    bytecode.addOpcode(Opcode.FCONST_0);
                } else if (javaFieldBinding.constant()) {
                    // inline const
                    bytecode.addFconst(javaFieldBinding.get().getAsNumber());
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

    @Override
    public CompileVisitResult visitCall(final @NotNull CallExpression expression) {
        final Scope scope = functionCompileState.scope();
        final Expression functionExpr = expression.function();

        final Value functionValue = functionExpr.visit(new ExpressionVisitor<Value>() {
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
            bytecode.addOpcode(Opcode.FCONST_0);
            return new CompileVisitResult(CtClass.floatType);
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
                final Parameter parameter = parameters[i];

                if (parameter.isAnnotationPresent(Entity.class)) {
                    Object entity = functionCompileState.compiler().entity();
                    if (entity == null || !parameter.getType().isInstance(entity)) {
                        // load null
                        bytecode.addConstZero(ctParameters[i]);
                    } else {
                        // add entity requirement
                        requirements.put("__entity__", entity);

                        // load entity requirement (field)
                        bytecode.addAload(0); // load this
                        bytecode.addGetfield(
                                functionCompileState.type(),
                                "__entity__",
                                Descriptor.of(ctParameters[i])
                        );
                    }
                    continue;
                }

                if (!it.hasNext()) {
                    bytecode.addConstZero(ctParameters[i]);
                    continue;
                }

                // Set the expected type, then load
                expectedType = ctParameters[i];
                it.next().visit(this);
            }

            final CtClass nativeMethodDeclaringCtClass;
            final CtClass ctReturnType;

            try {
                nativeMethodDeclaringCtClass = classPool.get(nativeMethod.getDeclaringClass().getName());
                ctReturnType = classPool.get(nativeMethod.getReturnType().getName());
            } catch (final NotFoundException e) {
                throw new IllegalStateException("Return type not found", e);
            }

            if (Modifier.isStatic(nativeMethod.getModifiers())) {
                // invoke static
                bytecode.addInvokestatic(nativeMethodDeclaringCtClass, nativeMethod.getName(), ctReturnType, ctParameters);
            } else {
                final String fieldName = object.getClass().getSimpleName().toLowerCase() + Integer.toHexString(object.hashCode());
                requirements.put(fieldName, object);

                final CtClass requirementType;

                try {
                    requirementType = classPool.get(object.getClass().getName());
                } catch (final NotFoundException e) {
                    throw new IllegalStateException("Field not found", e);
                }

                // we must load object
                bytecode.addAload(0);
                bytecode.addGetfield(functionCompileState.type(), fieldName, Descriptor.of(requirementType));
                bytecode.addInvokevirtual(nativeMethodDeclaringCtClass, nativeMethod.getName(), ctReturnType, ctParameters);
            }

            if (nativeMethod.getReturnType() == void.class) {
                if (expectedType != CtClass.voidType) {
                    bytecode.addConstZero(expectedType);
                }
            } else if (!nativeMethod.getReturnType().getName().equals(expectedType.getName())) {
                JavassistUtil.addCast(bytecode, ctReturnType, expectedType);
            }
        } else {
            throw new UnsupportedOperationException("Not supporting non-Java functions yet");
        }
        return null;
    }

    @Override
    public CompileVisitResult visit(final @NotNull Expression expression) {
        throw new UnsupportedOperationException("Unsupported expression type: " + expression);
    }
}