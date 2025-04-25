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

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.StackMapTable;
import javassist.bytecode.stackmap.MapMaker;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.runtime.compiled.MochaCompiledFunction;
import team.unnamed.mocha.runtime.compiled.Named;
import team.unnamed.mocha.util.CaseInsensitiveStringHashMap;
import team.unnamed.mocha.util.JavassistUtil;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@ApiStatus.Internal
public final class MolangCompiler {
    private static final Random RANDOM = new Random();

    private final Object entity;
    private final ClassLoader classLoader;
    private final ClassPool classPool;

    private final Scope scope;
    private Consumer<byte @NotNull []> postCompile;

    public MolangCompiler(final @Nullable Object entity, final @NotNull ClassLoader classLoader, final @NotNull Scope scope) {
        this.entity = entity;
        this.classLoader = requireNonNull(classLoader, "classLoader");
        this.classPool = ClassPool.getDefault();
        this.scope = requireNonNull(scope, "scope");
    }

    public @Nullable Object entity() {
        return entity;
    }

    public @NotNull ClassPool classPool() {
        return classPool;
    }

    public void postCompile(final @Nullable Consumer<byte @NotNull []> postCompile) {
        this.postCompile = postCompile;
    }

    public <T extends MochaCompiledFunction> @NotNull T compile(final @NotNull List<Expression> expressions, final @NotNull Class<T> clazz) {
        requireNonNull(expressions, "expressions");
        requireNonNull(clazz, "clazz");

        if (clazz == MochaFunction.class && expressions.isEmpty()) {
            // no expressions and the target type is MochaFunction,
            // we know the NOP function
            return clazz.cast(MochaFunction.nop());
        }

        if (!clazz.isInterface()) {
            throw new IllegalArgumentException("Target type must be an interface: " + clazz.getName());
        }

        Method implementedMethod = null;
        for (final Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || method.isDefault()) {
                continue;
            }
            if (implementedMethod != null) {
                throw new IllegalArgumentException("Target type must have only one method: " + clazz.getName());
            }
            implementedMethod = method;
        }

        if (implementedMethod == null) {
            throw new IllegalArgumentException("Target type must have a method to implement: " + clazz.getName());
        }

        final Map<String, Integer> argumentParameterIndexes = new CaseInsensitiveStringHashMap<>();
        final CtClass[] ctParameters;

        // check method parameter types
        {
            final Parameter[] parameters = implementedMethod.getParameters();
            ctParameters = new CtClass[parameters.length];

            for (int i = 0; i < parameters.length; ++i) {
                final Parameter parameter = parameters[i];
                final Named named = parameter.getDeclaredAnnotation(Named.class);
                final String name;

                if (named != null) {
                    name = named.value();
                } else if (parameter.isNamePresent()) {
                    name = parameter.getName();
                } else {
                    throw new IllegalArgumentException("Parameter " + parameter.getName() + " (index " + i
                            + ") must be annotated with @Named and specify a name");
                }

                argumentParameterIndexes.put(name, i);
                try {
                    ctParameters[i] = classPool.get(parameter.getType().getName());
                } catch (NotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        final CtClass interfaceCtClass = JavassistUtil.getClassUnchecked(classPool, clazz);
        final String scriptClassName = getClass().getPackage().getName() + ".MolangFunctionImpl_" + clazz.getSimpleName() + "_" + implementedMethod.getName()
                + "_" + Long.toHexString(System.currentTimeMillis()) + "_" + Integer.toHexString(RANDOM.nextInt(2024));

        final CtClass scriptCtClass = classPool.makeClass(scriptClassName);
        scriptCtClass.addInterface(interfaceCtClass);
        scriptCtClass.setModifiers(Modifier.PUBLIC);

        final Class<?> returnType = implementedMethod.getReturnType();
        final CtClass returnCtType = JavassistUtil.getClassUnchecked(classPool, returnType);

        final Bytecode bytecode = new Bytecode(scriptCtClass.getClassFile().getConstPool());
        final FunctionCompileState compileState = new FunctionCompileState(this, classPool, scriptCtClass, bytecode, implementedMethod, scope, argumentParameterIndexes);

        // compute initial max locals
        {
            int maxLocals = 1; // 1: this
            for (final CtClass paramType : ctParameters) {
                if (paramType == CtClass.doubleType || paramType == CtClass.longType) {
                    maxLocals += 2; // doubles and longs take 2 places
                } else {
                    maxLocals++;
                }
            }
            compileState.maxLocals(maxLocals);
        }

        if (expressions.isEmpty()) {
            // add only a "return 0", "return" or "return null" instruction
            bytecode.addConstZero(returnCtType);
            bytecode.addReturn(returnCtType);
        } else {
            final MolangCompilingVisitor compiler = new MolangCompilingVisitor(compileState);
            CompileVisitResult lastVisitResult = null;

            final ExpressionInliner inliner = new ExpressionInliner(new ExpressionInterpreter<>(null, scope), scope);

            for (final Expression expression : expressions) {
                lastVisitResult = expression.visit(inliner).visit(compiler);
            }

            if (lastVisitResult == null || !lastVisitResult.returned()) {
                if (lastVisitResult == null || lastVisitResult.lastPushedType() != returnCtType) {
                    JavassistUtil.addCast(
                            bytecode,
                            lastVisitResult == null ? CtClass.floatType : lastVisitResult.lastPushedType(),
                            returnCtType
                    );
                }

                compiler.endVisit();
            }
        }

        bytecode.setMaxLocals(compileState.maxLocals());

        final MethodInfo method = new MethodInfo(scriptCtClass.getClassFile().getConstPool(), implementedMethod.getName(), Descriptor.ofMethod(returnCtType, ctParameters));
        method.setAccessFlags(Modifier.PUBLIC | Modifier.FINAL);
        method.setCodeAttribute(bytecode.toCodeAttribute());
        final StackMapTable stackMapTable;

        try {
            method.getCodeAttribute().computeMaxStack();
            stackMapTable = MapMaker.make(classPool, method);
        } catch (final BadBytecode e) {
            throw new IllegalStateException("Generated bad bytecode, open an issue at https://github.com/unnamed/mocha/issues", e);
        }

        if (stackMapTable != null) {
            method.getCodeAttribute().setAttribute(stackMapTable);
        }

        try {
            scriptCtClass.addMethod(CtMethod.make(method, scriptCtClass));
        } catch (final CannotCompileException e) {
            throw new IllegalStateException("Couldn't compile main function method", e);
        }

        final Map<String, Object> requirements = compileState.requirements();

        // add fields for the requirements
        for (final Map.Entry<String, Object> entry : requirements.entrySet()) {
            final String fieldName = entry.getKey();
            final Object fieldValue = entry.getValue();
            final CtClass fieldType = JavassistUtil.getClassUnchecked(classPool, fieldValue.getClass());
            try {
                scriptCtClass.addField(new CtField(fieldType, fieldName, scriptCtClass));
            } catch (final CannotCompileException e) {
                throw new IllegalStateException("Couldn't compile field " + fieldName + " with type " + fieldType.getName(), e);
            }
        }

        // add constructor that needs requirements and initializes them
        final CtClass[] constructorParameterCtTypes = new CtClass[requirements.size()];
        int j = 0;
        for (final Map.Entry<String, Object> entry : requirements.entrySet()) {
            constructorParameterCtTypes[j] = JavassistUtil.getClassUnchecked(classPool, entry.getValue().getClass());
            ++j;
        }

        {
            final CtConstructor ctConstructor = new CtConstructor(constructorParameterCtTypes, scriptCtClass);
            final Bytecode constructorBytecode = new Bytecode(scriptCtClass.getClassFile().getConstPool());
            constructorBytecode.addAload(0); // load this
            constructorBytecode.addInvokespecial(JavassistUtil.getClassUnchecked(classPool, Object.class), "<init>", "()V"); // invoke superclass constructor
            // put!
            int parameterIndex = 0;
            for (final Map.Entry<String, Object> entry : requirements.entrySet()) {
                final String fieldName = entry.getKey();
                final Object fieldValue = entry.getValue();
                constructorBytecode.addAload(0); // load this
                constructorBytecode.addAload(parameterIndex + 1); // load parameter
                constructorBytecode.addPutfield(scriptCtClass, fieldName, Descriptor.of(JavassistUtil.getClassUnchecked(classPool, fieldValue.getClass()))); // set!
                parameterIndex++;
            }
            constructorBytecode.addReturn(null); // return
            ctConstructor.getMethodInfo().setCodeAttribute(constructorBytecode.toCodeAttribute());
            try {
                ctConstructor.getMethodInfo().getCodeAttribute().computeMaxStack();
            } catch (final BadBytecode e) {
                throw new IllegalStateException("Generated bad bytecode, open an issue at https://github.com/unnamed/mocha/issues", e);
            }

            ctConstructor.getMethodInfo().getCodeAttribute().setMaxLocals(constructorParameterCtTypes.length + 1);
            try {
                scriptCtClass.addConstructor(ctConstructor);
            } catch (final CannotCompileException e) {
                throw new IllegalStateException("Couldn't compile script constructor", e);
            }
        }

        if (postCompile != null) {
            try {
                postCompile.accept(scriptCtClass.toBytecode());
            } catch (IOException | CannotCompileException e) {
                throw new IllegalStateException("Couldn't collect script bytecode", e);
            }
        }
        final Class<?> compiledClass;
        try {
            compiledClass = classPool.toClass(scriptCtClass, getClass(), classLoader, null);
        } catch (final Exception e) {
            throw new IllegalStateException("Couldn't compile script class", e);
        }

        // find the constructor with the requirements
        final Class<?>[] constructorParameterTypes = new Class[requirements.size()];
        final Object[] constructorArguments = new Object[requirements.size()];
        int i = 0;
        for (final Object requirement : requirements.values()) {
            constructorParameterTypes[i] = requirement.getClass();
            constructorArguments[i] = requirement;
            ++i;
        }

        final Constructor<?> constructor;
        try {
            constructor = compiledClass.getDeclaredConstructor(constructorParameterTypes);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Couldn't find constructor with parameters " + requirements.keySet(), e);
        }
        final Object instance;
        try {
            instance = constructor.newInstance(constructorArguments);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Couldn't instantiate script class", e);
        }
        return clazz.cast(instance);
    }
}