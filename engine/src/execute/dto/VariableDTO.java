package execute.dto;

import logic.variables.Variable;
import logic.variables.VariableType;

public class VariableDTO {
    private final VariableType type;
    private final int num;
    private final long value;

    public VariableDTO(VariableType type, int num, long value) {
        this.type = type;
        this.num = num;
        this.value = value;
    }

    public VariableDTO(Variable var) {
        this.type = var.getType();
        this.num = var.getNum();
        this.value = var.getValue();
    }

    public VariableDTO(String name, long newValue) {
        switch (name.charAt(0)) {
            case 'x' -> {
                this.type = VariableType.INPUT;
                this.num = Integer.parseInt(name.substring(1));
            }
            case 'y' -> {
                this.type = VariableType.OUTPUT;
                this.num = 0;
            }
            case 'z' -> {
                this.type = VariableType.TEMP;
                this.num = Integer.parseInt(name.substring(1));
            }
            default -> throw new IllegalArgumentException("Invalid variable name: " + name);
        }
        this.value = newValue;
    }


    public VariableType getType() { return type; }
    public int getNum() { return num; }
    public long getValue() { return value; }

    @Override
    public String toString() { return this.getVarString(); }

    public String getVarString() {
        return this.getName() + " = " + this.value;
    }

    public String getName() {
        return switch (this.type) {
            case VariableType.INPUT -> "x" + num;
            case VariableType.OUTPUT -> "y";
            case VariableType.TEMP ->  "z" + num;
        };
    }

    public boolean isOutput() {
        return this.type == VariableType.OUTPUT;
    }
}
