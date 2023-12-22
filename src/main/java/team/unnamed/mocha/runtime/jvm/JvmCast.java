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
package team.unnamed.mocha.runtime.jvm;

import javassist.CtClass;
import javassist.CtPrimitiveType;
import javassist.bytecode.Bytecode;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

final class JvmCast {
    private static final Set<String> WRAPPER_TYPE_NAMES = new HashSet<>();

    static {
        WRAPPER_TYPE_NAMES.add(Boolean.class.getName());
        WRAPPER_TYPE_NAMES.add(Byte.class.getName());
        WRAPPER_TYPE_NAMES.add(Character.class.getName());
        WRAPPER_TYPE_NAMES.add(Short.class.getName());
        WRAPPER_TYPE_NAMES.add(Integer.class.getName());
        WRAPPER_TYPE_NAMES.add(Long.class.getName());
        WRAPPER_TYPE_NAMES.add(Float.class.getName());
        WRAPPER_TYPE_NAMES.add(Double.class.getName());
    }

    private JvmCast() {
    }

    public static boolean isWrapper(final @NotNull CtClass type) {
        requireNonNull(type, "type");
        return WRAPPER_TYPE_NAMES.contains(type.getName());
    }

    public static void addCast(final @NotNull Bytecode bytecode, final @NotNull CtClass from, final @NotNull CtClass to) {
        requireNonNull(bytecode, "bytecode");
        requireNonNull(from, "from");
        requireNonNull(to, "to");

        if (from.equals(to)) {
            // no cast needed
            return;
        }

        if (from.equals(CtClass.voidType) || to.equals(CtClass.voidType)) {
            throw new IllegalArgumentException("Cannot cast to or from void");
        }

        if (from.isPrimitive()) {
            if (to.isPrimitive()) {
                // primitive to primitive
                if (from.equals(CtClass.intType)
                        || from.equals(CtClass.byteType)
                        || from.equals(CtClass.booleanType)
                        || from.equals(CtClass.shortType)
                        || from.equals(CtClass.charType)) {
                    // can be cast to all primitive types
                    addCastIntTo(bytecode, to);
                } else if (from.equals(CtClass.longType)) {
                    // double to another primitive
                    addCastLongTo(bytecode, to);
                } else if (from.equals(CtClass.floatType)) {
                    addCastFloatTo(bytecode, to);
                } else if (from.equals(CtClass.doubleType)) {
                    addCastDoubleTo(bytecode, to);
                } else {
                    throw new TypeCastException("Cannot cast unknown primitive type: " + from.getName());
                }
            } else {
                final CtPrimitiveType fromPrimitive = (CtPrimitiveType) from;

                // primitive to wrapper
                bytecode.addInvokestatic(
                        fromPrimitive.getWrapperName(),
                        "valueOf",
                        "(" + fromPrimitive.getDescriptor() + ")L" + to.getName().replace('.', '/') + ";"
                );
            }
        } else {
            if (to.isPrimitive()) {
                if (WRAPPER_TYPE_NAMES.contains(from.getName())) {
                    final CtPrimitiveType toPrimitive = (CtPrimitiveType) to;

                    // wrapper to primitive
                    bytecode.addInvokevirtual(
                            from,
                            toPrimitive.getGetMethodName(),
                            toPrimitive.getGetMethodDescriptor()
                    );
                } else {
                    throw new TypeCastException("Cannot cast unknown type: " + from.getName());
                }
            } else {
                // object to object
                bytecode.addCheckcast(to);
            }
        }
    }

    public static void addCastIntTo(final @NotNull Bytecode bytecode, final @NotNull CtClass to) {
        requireNonNull(bytecode, "bytecode");
        requireNonNull(to, "to");

        if (to.equals(CtClass.intType)) {
            // no cast needed
            return;
        }

        if (to.equals(CtClass.byteType)) {
            // Convert int to byte
            // Pops an int, truncates it to a byte, then sign-extends it
            // to an int result, and then is pushed again, results in an int
            bytecode.addOpcode(Bytecode.I2B);
        } else if (to.equals(CtClass.booleanType)) {
            // Convert int to boolean
            // Requires us to perform some extra logic, checking if the int is 0
            // and pushing 0 or 1 depending on the result
            bytecode.addOpcode(Bytecode.IFEQ); // if int is 0...
            bytecode.addIndex(6);              // ...jump to push 0 (A)
            bytecode.addIconst(1);          // int is not 0, push 1
            bytecode.addOpcode(Bytecode.GOTO); // ...jump
            bytecode.addIndex(3);              // ...to end (skips A)
            bytecode.addIconst(0);          // (A) push 0
        } else if (to.equals(CtClass.shortType)) {
            // Convert int to short
            // Pops an int, truncates it to a short, then sign-extends it
            // to an int result, and then is pushed again, results in an int
            bytecode.addOpcode(Bytecode.I2S);
        } else if (to.equals(CtClass.charType)) {
            // Convert int to char
            // Pops an int, truncates it to a char, then zero-extends it
            // to an int result, and then is pushed again, results in an int
            // (which is always positive)
            bytecode.addOpcode(Bytecode.I2C);
        } else if (to.equals(CtClass.longType)) {
            // Convert int to long
            // Pops an int, sign-extends it to a long, then pushes it again
            bytecode.addOpcode(Bytecode.I2L);
        } else if (to.equals(CtClass.floatType)) {
            // Convert int to float
            // Pops an int, converts it to a float, then pushes it again
            bytecode.addOpcode(Bytecode.I2F);
        } else if (to.equals(CtClass.doubleType)) {
            // Convert int to double
            // Pops an int, converts it to a double, then pushes it again
            bytecode.addOpcode(Bytecode.I2D);
        } else if (to.equals(CtClass.voidType)) {
            throw new TypeCastException("Cannot cast int to void");
        } else {
            throw new TypeCastException("Cannot cast int to unknown type: " + to.getName());
        }
    }

    public static void addCastDoubleTo(final @NotNull Bytecode bytecode, final @NotNull CtClass to) {
        requireNonNull(bytecode, "bytecode");
        requireNonNull(to, "to");

        if (to.equals(CtClass.doubleType)) {
            // no cast needed
            return;
        }

        if (to.equals(CtClass.intType)) {
            // Convert double to int
            // Pops a double, converts it to an int, then pushes it
            bytecode.addOpcode(Bytecode.D2I);
        } else if (to.equals(CtClass.longType)) {
            // Convert double to long
            // Pops a double, converts it to a long, then pushes it
            bytecode.addOpcode(Bytecode.D2L);
        } else if (to.equals(CtClass.floatType)) {
            // Convert double to float
            // Pops a double, converts it to a float, then pushes it
            bytecode.addOpcode(Bytecode.D2F);
        } else if (to.equals(CtClass.voidType)) {
            throw new IllegalArgumentException("Cannot cast double to void");
        } else {
            // Convert to an int and try to cast it to the target type
            bytecode.addOpcode(Bytecode.D2I);
            try {
                addCastIntTo(bytecode, CtClass.intType);
            } catch (final TypeCastException e) {
                // correct message
                throw new TypeCastException("Cannot cast double to unknown type: " + to.getName());
            }
        }
    }

    public static void addCastLongTo(final @NotNull Bytecode bytecode, final @NotNull CtClass to) {
        requireNonNull(bytecode, "bytecode");
        requireNonNull(to, "to");

        if (to.equals(CtClass.longType)) {
            // no cast needed
            return;
        }

        if (to.equals(CtClass.intType)) {
            // Convert long to int
            // Pops a long, converts it to an int, then pushes it
            bytecode.addOpcode(Bytecode.L2I);
        } else if (to.equals(CtClass.doubleType)) {
            // Convert long to double
            // Pops a long, converts it to a double, then pushes it
            bytecode.addOpcode(Bytecode.L2D);
        } else if (to.equals(CtClass.floatType)) {
            // Convert long to float
            // Pops a long, converts it to a float, then pushes it
            bytecode.addOpcode(Bytecode.L2F);
        } else if (to.equals(CtClass.voidType)) {
            throw new IllegalArgumentException("Cannot cast long to void");
        } else {
            // Convert to an int and try to cast it to the target type
            bytecode.addOpcode(Bytecode.L2I);
            try {
                addCastIntTo(bytecode, CtClass.intType);
            } catch (final TypeCastException e) {
                // correct message
                throw new TypeCastException("Cannot cast long to unknown type: " + to.getName());
            }
        }
    }

    public static void addCastFloatTo(final @NotNull Bytecode bytecode, final @NotNull CtClass to) {
        requireNonNull(bytecode, "bytecode");
        requireNonNull(to, "to");

        if (to.equals(CtClass.floatType)) {
            // no cast needed
            return;
        }

        if (to.equals(CtClass.intType)) {
            // Convert float to int
            // Pops a float, converts it to an int, then pushes it
            bytecode.addOpcode(Bytecode.F2I);
        } else if (to.equals(CtClass.doubleType)) {
            // Convert float to double
            // Pops a float, converts it to a double, then pushes it
            bytecode.addOpcode(Bytecode.F2D);
        } else if (to.equals(CtClass.longType)) {
            // Convert float to long
            // Pops a float, converts it to a long, then pushes it
            bytecode.addOpcode(Bytecode.F2L);
        } else if (to.equals(CtClass.voidType)) {
            throw new IllegalArgumentException("Cannot cast float to void");
        } else {
            // Convert to an int and try to cast it to the target type
            bytecode.addOpcode(Bytecode.F2I);
            try {
                addCastIntTo(bytecode, CtClass.intType);
            } catch (final TypeCastException e) {
                // correct message
                throw new TypeCastException("Cannot cast float to unknown type: " + to.getName());
            }
        }
    }
}
