package team.unnamed.molang.runtime.jvm;

import javassist.ClassPool;
import javassist.NotFoundException;
import javassist.bytecode.Bytecode;
import org.jetbrains.annotations.NotNull;
import team.unnamed.molang.parser.ast.BinaryExpression;
import team.unnamed.molang.parser.ast.DoubleExpression;
import team.unnamed.molang.parser.ast.Expression;
import team.unnamed.molang.parser.ast.ExpressionVisitor;
import team.unnamed.molang.parser.ast.IdentifierExpression;
import team.unnamed.molang.parser.ast.StringExpression;
import team.unnamed.molang.parser.ast.TernaryConditionalExpression;
import team.unnamed.molang.parser.ast.UnaryExpression;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import static java.util.Objects.requireNonNull;

final class MolangCompilingVisitor implements ExpressionVisitor<Void> {
    private final ClassPool classPool;
    private final Bytecode bytecode;
    private final Method method;

    private final Map<String, Integer> argumentParameterIndexes;

    MolangCompilingVisitor(
            final @NotNull ClassPool classPool,
            final @NotNull Bytecode bytecode,
            final @NotNull Method method,
            final @NotNull Map<String, Integer> argumentParameterIndexes
    ) {
        this.classPool = requireNonNull(classPool, "classPool");
        this.bytecode = requireNonNull(bytecode, "bytecode");
        this.method = requireNonNull(method, "method");
        this.argumentParameterIndexes = requireNonNull(argumentParameterIndexes, "argumentParameterIndexes");
    }

    public static void visitEmpty(final @NotNull Bytecode bytecode, final @NotNull Class<?> returnType) {
        if (returnType.equals(int.class)) {
            bytecode.addIconst(0);
        } else if (returnType.equals(long.class)) {
            bytecode.addLconst(0);
        } else if (returnType.equals(double.class)) {
            bytecode.addDconst(0);
        } else if (returnType.equals(float.class)) {
            bytecode.addFconst(0);
        } else if (returnType.equals(boolean.class)) {
            bytecode.addIconst(0);
        } else if (returnType.equals(char.class)) {
            bytecode.addIconst(0);
        } else if (returnType.equals(short.class)) {
            bytecode.addIconst(0);
        } else if (returnType.equals(byte.class)) {
            bytecode.addIconst(0);
        } else if (!returnType.equals(void.class)) {
            // return null
            bytecode.addOpcode(Bytecode.ACONST_NULL);
        }
    }

    @Override
    public Void visitBinary(final @NotNull BinaryExpression expression) {
        expression.left().visit(this);   // pushes lhs value to stack
        expression.right().visit(this);  // pushes rhs value to stack

        //@formatter:off
        switch (expression.op()) {
            case AND: {

                break;
            }
            case ADD: bytecode.addOpcode(Bytecode.DADD); break;
            case SUB: bytecode.addOpcode(Bytecode.DSUB); break;
            case MUL: bytecode.addOpcode(Bytecode.DMUL); break;
            case DIV: bytecode.addOpcode(Bytecode.DDIV); break;
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
    public Void visitDouble(final @NotNull DoubleExpression expression) {
        bytecode.addLdc2w(expression.value());
        return null;
    }

    @Override
    public Void visitString(final @NotNull StringExpression expression) {
        bytecode.addLdc(expression.value());
        return null;
    }

    @Override
    public Void visitUnary(final @NotNull UnaryExpression expression) {
        expression.expression().visit(this); // push value to stack

        switch (expression.op()) {
            case RETURN: {
                endVisit(); // force visit end
                break;
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
                bytecode.addOpcode(Bytecode.DNEG); // double negation
                break;
            }
            default:
                throw new UnsupportedOperationException("Unsupported unary operator: " + expression.op());
        }
        return null;
    }

    @Override
    public Void visitTernaryConditional(final @NotNull TernaryConditionalExpression expression) {
        expression.condition().visit(this); // push value to stack
        bytecode.addOpcode(Bytecode.DCONST_0); // push 0
        bytecode.addOpcode(Bytecode.DCMPL); // compare
        bytecode.addOpcode(Bytecode.IFEQ); // if false skip
        bytecode.addIndex(7);
        expression.trueExpression().visit(this); // push true value to stack
        bytecode.addOpcode(Bytecode.GOTO); // skip pushing false value
        bytecode.addIndex(4);
        expression.falseExpression().visit(this); // push false value to stack
        return null;
    }

    @Override
    public Void visitIdentifier(final @NotNull IdentifierExpression expression) {
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
    public Void visit(final @NotNull Expression expression) {
        throw new UnsupportedOperationException("Unsupported expression type: " + expression);
    }
}
