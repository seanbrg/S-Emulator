package logic.instructions;
import logic.labels.Label;

public interface Instruction {
    String getRepresentation(int num);

    String print();

    Label execute();

    int getCycles();

    Label getSelfLabel();

    int getDegree();

    Label getTargetLabel();
}