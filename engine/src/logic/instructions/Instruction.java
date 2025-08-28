package logic.instructions;
import logic.labels.Label;

public interface Instruction {
    String getRepresentation();

    String print();

    Label execute();

    int getCycles();

    Label getSelfLabel();

    int getDegree();

    void setNum(int num);

    Label getTargetLabel();

    int getNum();
}