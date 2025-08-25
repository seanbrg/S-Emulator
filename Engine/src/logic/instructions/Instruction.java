package logic.instructions;
import logic.labels.Label;

public interface Instruction {
    String getRepresentation();
    void execute();
    int getCycles();
    Label getLabel();
}
