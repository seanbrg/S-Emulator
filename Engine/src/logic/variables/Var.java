package logic.variables;

public class Var implements Variable {
    private int num;
    private long value;
    private VariableType type;

    public Var(VariableType type, int num, long value) {
        this.type = type;
        this.num = num;
        this.value = value;
    }

    public Var(VariableType type, int num) {
        this(type, num, 0);
    }

    @Override
    public VariableType getType() { return type; }

    @Override
    public String getName() {
        if (type == VariableType.INPUT) return "x" + num;
        else if (type == VariableType.TEMP) return "z" + num;
        else if (type == VariableType.OUTPUT) return "y";
        else return null;
    }

    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; }
}
