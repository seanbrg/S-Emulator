package logic.instructions;

public enum InstructionData {
    INCREASE("INCREASE", 1, 0, "I", InstructionType.BASIC),
    DECREASE("DECREASE", 1, 0, "I", InstructionType.BASIC),
    JUMP_NOT_ZERO("JNZ", 3, 0, "I", InstructionType.BASIC),
    NO_OP("NO_OP", 0, 0, "I", InstructionType.BASIC),

    ZERO_VARIABLE("ZERO_VARIABLE", 1, 1, "II", InstructionType.SYNTHETIC),
    GOTO_LABEL("GOTO_LABEL", 1, 1, "II", InstructionType.SYNTHETIC),
    CONSTANT_ASSIGNMENT("CONSTANT_ASSIGNMENT", 2, 1, "II", InstructionType.SYNTHETIC),

    ASSIGNMENT("ASSIGNMENT", 4, 2, "III",  InstructionType.SYNTHETIC),
    JUMP_ZERO("JUMP_ZERO", 2, 2, "III", InstructionType.SYNTHETIC),
    JUMP_EQUAL_CONSTANT("JUMP_EQUAL_CONSTANT", 2, 3, "III", InstructionType.SYNTHETIC),
    JUMP_EQUAL_VARIABLE("JUMP_EQUAL_VARIABLE", 2, 3, "III", InstructionType.SYNTHETIC),

    QUOTE("QUOTE", 5, 1, "IV", InstructionType.SYNTHETIC),
    JUMP_EQUAL_FUNCTION("JUMP_EQUAL_FUNCTION", 6, 2, "IV", InstructionType.SYNTHETIC);

    private final String name;
    private final int cycles;
    private final int degree;
    private final String arch;
    private final InstructionType type;

    InstructionData(String name, int cycles, int degree, String arch, InstructionType type) {
        this.name = name;
        this.cycles = cycles;
        this.degree = degree;
        this.arch = arch;
        this.type = type;
    }

    public int getCycles() { return cycles; }

    public int getDegree() { return degree; }

    public String getArch() { return arch; }

    public InstructionType getInstructionType() { return type; }

    public String toString() { return name; }
}
