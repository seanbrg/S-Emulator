package logic.program;

import logic.instructions.Instruction;
import logic.labels.Label;
import logic.variables.Var;
import logic.variables.Variable;
import logic.variables.VariableType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SProgram implements Program {
    String name;
    List<Instruction> instructions;
    VariablesList tempVariables;
    Map<Label, Instruction> labels;

    public SProgram(String name) {
        this.name = name;
        instructions = new ArrayList<>();
        tempVariables = new VariablesList
                (() -> new Var(VariableType.TEMP, tempVariables.size()));
        labels = new HashMap<>();
    }

    @Override
    public Variable run(int degree, VariablesList inputVariables) {
        Variable result = new Var(VariableType.OUTPUT, -1);



        return result;
    }

    @Override
    public void addInstruction(Instruction instruction) {
        instructions.add(instruction);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Instruction> getInstructions() {
        return instructions;
    }

    @Override
    public VariablesList getVars() {
        return tempVariables;
    }

    @Override
    public Map<Label, Instruction> getLabels() {
        return labels;
    }

    @Override
    public int maxDegree() {
        return instructions.stream()
                .mapToInt(Instruction::getDegree)
                .max().orElse(0);
    }

    @Override
    public int cycles() {
        return instructions.stream()
                .mapToInt(Instruction::getCycles)
                .sum();
    }

}
