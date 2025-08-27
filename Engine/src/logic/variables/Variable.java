package logic.variables;

public interface Variable {
    VariableType getType();

    String getName();

    public long getValue();

    public void setValue(long value);
}
