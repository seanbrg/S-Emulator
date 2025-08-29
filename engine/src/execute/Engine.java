package execute;

import logic.labels.Label;
import logic.variables.Variable;

import java.util.List;
import java.util.Set;

public interface Engine {
    boolean loadFromXML(String filePath);

    void printProgram();

    public long runProgram(int degree, List<Long> inputs);


    void resetVars();

    boolean validateProgram();

    int maxDegree();

    List<Variable> getInputs();

    public List<List<Variable>> getVarByType();

    int getCycles(int degree);
}
