package logic.instructions;

public enum InstructionData {
    INCREASE("INCREASE", 1, InstructionType.BASIC),
    DECREASE("DECREASE", 1, InstructionType.BASIC),
    JUMP_NOT_ZERO("JNZ", 3, InstructionType.BASIC),
    NO_OP("NO_OP", 0, InstructionType.BASIC);

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
