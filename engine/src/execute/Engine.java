package execute;

import logic.variables.Variable;

import java.util.List;
import java.util.Set;

public interface Engine {
    boolean loadFromXML(String filePath);

    void printProgram();

    long runProgram(int degree);

    boolean validateProgram();

    int maxDegree();

    List<Variable> getInputs();
}
