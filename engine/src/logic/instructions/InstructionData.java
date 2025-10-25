package logic.instructions;

public enum InstructionData {
    INCREASE("INCREASE", 1, 0, 1, InstructionType.BASIC),
    DECREASE("DECREASE", 1, 0, 1, InstructionType.BASIC),
    JUMP_NOT_ZERO("JNZ", 3, 0, 1, InstructionType.BASIC),
    NO_OP("NO_OP", 0, 0, 1, InstructionType.BASIC),

    ZERO_VARIABLE("ZERO_VARIABLE", 1, 1, 2, InstructionType.SYNTHETIC),
    GOTO_LABEL("GOTO_LABEL", 1, 1, 2, InstructionType.SYNTHETIC),
    CONSTANT_ASSIGNMENT("CONSTANT_ASSIGNMENT", 2, 1, 2, InstructionType.SYNTHETIC),

    ASSIGNMENT("ASSIGNMENT", 4, 2, 3,  InstructionType.SYNTHETIC),
    JUMP_ZERO("JUMP_ZERO", 2, 2, 3, InstructionType.SYNTHETIC),
    JUMP_EQUAL_CONSTANT("JUMP_EQUAL_CONSTANT", 2, 3, 3, InstructionType.SYNTHETIC),
    JUMP_EQUAL_VARIABLE("JUMP_EQUAL_VARIABLE", 2, 3, 3, InstructionType.SYNTHETIC),

    QUOTE("QUOTE", 5, 1, 4, InstructionType.SYNTHETIC),
    JUMP_EQUAL_FUNCTION("JUMP_EQUAL_FUNCTION", 6, 2, 4, InstructionType.SYNTHETIC);

    private final String name;
    private final int cycles;
    private final int degree;
    private final int arch;
    private final InstructionType type;

    InstructionData(String name, int cycles, int degree, int arch, InstructionType type) {
        this.name = name;
        this.cycles = cycles;
        this.degree = degree;
        this.arch = arch;
        this.type = type;
    }

    public int getCycles() { return cycles; }

    public int getDegree() { return degree; }

    public int getArch() { return arch; }

    public InstructionType getInstructionType() { return type; }

    public String toString() { return name; }
}
