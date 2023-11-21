/*
 * This file is part of molang, licensed under the MIT license
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
package team.unnamed.molang.runtime.jvm;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.Bytecode;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.StackMapTable;
import javassist.bytecode.stackmap.MapMaker;
import org.jetbrains.annotations.NotNull;
import team.unnamed.molang.parser.MolangParser;
import team.unnamed.molang.parser.ast.Expression;

import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

final class MolangCompilerImpl implements MolangCompiler {
    private final Map<String, RegisteredMolangNative> natives = new HashMap<>();
    private final ClassLoader classLoader;
    private final ClassPool classPool;

    MolangCompilerImpl(final @NotNull ClassLoader classLoader) {
        this.classLoader = requireNonNull(classLoader, "classLoader");
        this.classPool = ClassPool.getDefault();
    }

    @Override
    public void registerStaticNatives(final @NotNull Class<?> clazz) {
        requireNonNull(clazz, "clazz");
        for (final Method method : clazz.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || method.isSynthetic()) {
                continue;
            }

            final MolangNative spec = method.getDeclaredAnnotation(MolangNative.class);
            if (spec == null) {
                continue;
            }

            final String functionName = spec.value();
            final RegisteredMolangNative _native = new RegisteredMolangNative(functionName, null, null, method);
            natives.put(functionName, _native);
        }
    }

    @Override
    public void registerNatives(final @NotNull Object object, final @NotNull String name) {
        requireNonNull(object, "object");
        requireNonNull(name, "name");
        final Class<?> clazz = object.getClass();
        for (final Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || method.isSynthetic()) {
                continue;
            }

            final MolangNative spec = method.getDeclaredAnnotation(MolangNative.class);
            if (spec == null) {
                continue;
            }

            final String functionName = name + '.' + spec.value();
            final RegisteredMolangNative _native = new RegisteredMolangNative(functionName, name, object, method);
            natives.put(functionName, _native);
        }
    }

    @Override
    public <T extends MolangFunction> @NotNull T compile(final @NotNull Reader source, final @NotNull Class<T> clazz) {
        requireNonNull(source, "source");
        requireNonNull(clazz, "clazz");

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

        final Map<String, Integer> argumentParameterIndexes = new HashMap<>();
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

        final String scriptClassName = getClass().getPackage().getName() + ".MolangFunctionImpl_" + clazz.getSimpleName() + "_" + implementedMethod.getName()
                + "_" + Long.toHexString(System.currentTimeMillis());

        try {
            CtClass scriptCtClass = classPool.makeClass(scriptClassName);
            scriptCtClass.addInterface(classPool.get(clazz.getName()));
            scriptCtClass.setModifiers(Modifier.FINAL | Modifier.PUBLIC);

            final Class<?> returnType = implementedMethod.getReturnType();
            final CtClass returnCtType = classPool.get(returnType.getName());

            final List<Expression> expressions = MolangParser.parseAll(source);
            final Bytecode bytecode = new Bytecode(scriptCtClass.getClassFile().getConstPool());

            final FunctionCompileState compileState = new FunctionCompileState(
                    this,
                    classPool,
                    scriptCtClass,
                    bytecode,
                    implementedMethod,
                    natives,
                    argumentParameterIndexes
            );

            if (expressions.isEmpty()) {
                // add only a "return 0", "return" or "return null" instruction
                MolangCompilingVisitor.visitEmpty(bytecode, returnType);
            } else {
                final MolangCompilingVisitor visitor = new MolangCompilingVisitor(compileState);
                for (final Expression expression : expressions) {
                    expression.visit(visitor);
                }

                if (returnType.equals(int.class)) {
                    bytecode.addOpcode(Bytecode.D2I);
                } else if (returnType.equals(float.class)) {
                    bytecode.addOpcode(Bytecode.D2F);
                } else if (returnType.equals(long.class)) {
                    bytecode.addOpcode(Bytecode.D2L);
                } else if (returnType.equals(short.class)) {
                    bytecode.addOpcode(Bytecode.D2I);
                    bytecode.addOpcode(Bytecode.I2S);
                } else if (returnType.equals(byte.class)) {
                    bytecode.addOpcode(Bytecode.D2I);
                    bytecode.addOpcode(Bytecode.I2B);
                } else if (returnType.equals(char.class)) {
                    bytecode.addOpcode(Bytecode.D2I);
                    bytecode.addOpcode(Bytecode.I2C);
                } else if (returnType.equals(boolean.class)) {
                    bytecode.addOpcode(Bytecode.D2I);
                    bytecode.addOpcode(Bytecode.I2B);
                }

                visitor.endVisit();
            }

            bytecode.setMaxLocals(16);
            bytecode.setMaxStack(24);

            final MethodInfo method = new MethodInfo(
                    scriptCtClass.getClassFile().getConstPool(),
                    implementedMethod.getName(),
                    Descriptor.ofMethod(
                            classPool.get(returnType.getName()),
                            ctParameters
                    )
            );
            method.setAccessFlags(Modifier.PUBLIC | Modifier.FINAL);
            method.setCodeAttribute(bytecode.toCodeAttribute());
            final StackMapTable stackMapTable = MapMaker.make(classPool, method);
            if (stackMapTable != null) {
                method.getCodeAttribute().setAttribute(stackMapTable);
            }

            scriptCtClass.addMethod(CtMethod.make(method, scriptCtClass));

            final Map<String, Object> requirements = compileState.requirements();

            // add fields for the requirements
            for (final Map.Entry<String, Object> entry : requirements.entrySet()) {
                final String fieldName = entry.getKey();
                final Object fieldValue = entry.getValue();
                final CtClass fieldType = classPool.get(fieldValue.getClass().getName());
                scriptCtClass.addField(new CtField(fieldType, fieldName, scriptCtClass));
            }

            // add constructor that needs requirements and initializes them
            final CtClass[] constructorParameterCtTypes = new CtClass[requirements.size()];
            int j = 0;
            for (final Map.Entry<String, Object> entry : requirements.entrySet()) {
                constructorParameterCtTypes[j] = classPool.get(entry.getValue().getClass().getName());
                ++j;
            }

            {
                final CtConstructor ctConstructor = new CtConstructor(constructorParameterCtTypes, scriptCtClass);
                final Bytecode constructorBytecode = new Bytecode(scriptCtClass.getClassFile().getConstPool());
                constructorBytecode.addAload(0); // load this
                constructorBytecode.addInvokespecial(classPool.get(Object.class.getName()), "<init>", "()V"); // invoke superclass constructor
                // put!
                int parameterIndex = 0;
                for (final Map.Entry<String, Object> entry : requirements.entrySet()) {
                    final String fieldName = entry.getKey();
                    final Object fieldValue = entry.getValue();
                    constructorBytecode.addAload(0); // load this
                    constructorBytecode.addAload(parameterIndex + 1); // load parameter
                    constructorBytecode.addPutfield(scriptCtClass, fieldName, Descriptor.of(classPool.get(fieldValue.getClass().getName()))); // set!
                    parameterIndex++;
                }
                ctConstructor.getMethodInfo().setCodeAttribute(constructorBytecode.toCodeAttribute());
                scriptCtClass.addConstructor(ctConstructor);
            }

            final Class<?> compiledClass = classPool.toClass(scriptCtClass, null, classLoader, null);

            // find the constructor with the requirements
            final Class<?>[] constructorParameterTypes = new Class[requirements.size()];
            final Object[] constructorArguments = new Object[requirements.size()];
            int i = 0;
            for (final Object requirement : requirements.values()) {
                constructorParameterTypes[i] = requirement.getClass();
                constructorArguments[i] = requirement;
                ++i;
            }

            final Constructor<?> constructor = compiledClass.getDeclaredConstructor(constructorParameterTypes);
            final Object instance = constructor.newInstance(constructorArguments);
            return clazz.cast(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
