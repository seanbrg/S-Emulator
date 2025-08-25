package logic.instructions;

public enum InstructionData {
    INCREASE("INCREASE", 1, InstructionType.BASIC),
    DECREASE("DECREASE", 1, InstructionType.BASIC),
    JUMP_NOT_ZERO("JNZ", 3, InstructionType.BASIC),
    NO_OP("NO_OP", 0, InstructionType.BASIC),

    ZERO_VARIABLE("ZERO_VARIABLE", 1, InstructionType.SYNTHETIC),
    GOTO_LABEL("GOTO_LABEL", 1, InstructionType.SYNTHETIC),
    ASSIGNMENT("ASSIGNMENT", 4, InstructionType.SYNTHETIC),
    CONSTANT_ASSIGNMENT("CONSTANT_ASSIGNMENT", 2, InstructionType.SYNTHETIC),
    JUMP_ZERO("JUMP_ZERO", 2, InstructionType.SYNTHETIC),
    JUMP_EQUAL_CONSTANT("JUMP_EQUAL_CONSTANT", 2, InstructionType.SYNTHETIC),
    JUMP_EQUAL_VARIABLE("JUMP_EQUAL_VARIABLE", 2, InstructionType.SYNTHETIC),

    QUOTATION("QUOTATION", 5, InstructionType.SYNTHETIC),
    JUMP_EQUAL_FUNCTION("JUMP_EQUAL_FUNCTION", 6, InstructionType.SYNTHETIC);

    private final String name;
    private final int cycles;
    private InstructionType type;

    InstructionData(String name, int cycles, InstructionType type) {
        this.name = name;
        this.cycles = cycles;
        this.type = type;
    }

    public int getCycles() { return cycles; }
    public InstructionType getInstructionType() { return type; }
    public String toString() { return name; }
}
