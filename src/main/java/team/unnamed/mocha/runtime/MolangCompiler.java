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
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.StackMapTable;
import javassist.bytecode.stackmap.MapMaker;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.runtime.compiled.MochaCompiledFunction;
import team.unnamed.mocha.runtime.compiled.Named;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.util.Objects.requireNonNull;

@ApiStatus.Internal
public final class MolangCompiler {
    private static final Random RANDOM = new Random();
    public static boolean debug = true;
    private final ClassLoader classLoader;
    private final ClassPool classPool;

    private final GlobalScope scope;

    public MolangCompiler(final @NotNull ClassLoader classLoader, final @NotNull GlobalScope scope) {
        this.classLoader = requireNonNull(classLoader, "classLoader");
        this.classPool = ClassPool.getDefault();
        this.scope = requireNonNull(scope, "scope");
    }

    public <T extends MochaCompiledFunction> @NotNull T compile(final @NotNull List<Expression> expressions, final @NotNull Class<T> clazz) {
        requireNonNull(expressions, "expressions");
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
                + "_" + Long.toHexString(System.currentTimeMillis()) + "_" + Integer.toHexString(RANDOM.nextInt(2024));

        try {
            CtClass scriptCtClass = classPool.makeClass(scriptClassName);
            scriptCtClass.addInterface(classPool.get(clazz.getName()));
            scriptCtClass.setModifiers(Modifier.FINAL | Modifier.PUBLIC);

            final Class<?> returnType = implementedMethod.getReturnType();
            final CtClass returnCtType = classPool.get(returnType.getName());

            final Bytecode bytecode = new Bytecode(scriptCtClass.getClassFile().getConstPool());

            final FunctionCompileState compileState = new FunctionCompileState(
                    this,
                    classPool,
                    scriptCtClass,
                    bytecode,
                    implementedMethod,
                    scope,
                    argumentParameterIndexes
            );

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
                final MolangCompilingVisitor visitor = new MolangCompilingVisitor(compileState);
                CompileVisitResult lastVisitResult = null;

                for (final Expression expression : expressions) {
                    lastVisitResult = expression.visit(visitor);
                }

                if (lastVisitResult == null || !lastVisitResult.returned()) {
                    if (lastVisitResult == null || lastVisitResult.lastPushedType() != returnCtType) {
                        JavaTypes.addCast(
                                bytecode,
                                lastVisitResult == null ? CtClass.doubleType : lastVisitResult.lastPushedType(),
                                returnCtType
                        );
                    }

                    visitor.endVisit();
                }
            }

            bytecode.setMaxLocals(compileState.maxLocals());

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

            // print bytecode
            if (debug) {
                System.out.println("\n[instructions for " + expressions + "]");
                final CodeIterator it = method.getCodeAttribute().iterator();
                while (it.hasNext()) {
                    final int index = it.next();
                    final int opcode = it.byteAt(index);
                    final String opcodeName = Mnemonic.OPCODE[opcode];
                    String params = "";
                    switch (opcode) {
                        case Bytecode.IFEQ:
                        case Bytecode.IFNE:
                        case Bytecode.IFGT:
                        case Bytecode.GOTO: {
                            final int plus = it.s16bitAt(index + 1);
                            params += " +" + plus + " (" + (index + plus) + ")";
                            break;
                        }
                    }
                    System.out.println(index + ": " + opcodeName + "       " + params);
                }
                System.out.println("[end instructions]\n");
            }

            method.getCodeAttribute().computeMaxStack();

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
                constructorBytecode.addReturn(null); // return
                ctConstructor.getMethodInfo().setCodeAttribute(constructorBytecode.toCodeAttribute());
                ctConstructor.getMethodInfo().getCodeAttribute().computeMaxStack();
                ctConstructor.getMethodInfo().getCodeAttribute().setMaxLocals(constructorParameterCtTypes.length + 1);
                scriptCtClass.addConstructor(ctConstructor);
            }

            // write class
            if (debug) {
                Files.write(Paths.get("build/generated/" + scriptClassName + ".class"), scriptCtClass.toBytecode());
            }
            final Class<?> compiledClass = classPool.toClass(scriptCtClass, getClass(), classLoader, null);

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
