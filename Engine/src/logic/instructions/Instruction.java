package logic.instructions;
import logic.labels.Label;

public interface Instruction {
    String getRepresentation();

    String print();

    Label execute();

    int getCycles();

    Label getLabel();
}
