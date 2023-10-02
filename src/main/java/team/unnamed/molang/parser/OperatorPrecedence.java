package team.unnamed.molang.parser;

final class OperatorPrecedence {

    // The operator precedence priorities according to the Molang specification

    public static final int MULTIPLICATION_AND_DIVISION = 1000;
    public static final int ADDITION_AND_SUBTRACTION = 900;
    public static final int COMPARISON = 700;
    public static final int AND = 300;
    public static final int OR = 200;
    public static final int NULL_COALESCING = -500;

    private OperatorPrecedence() {
    }

}
