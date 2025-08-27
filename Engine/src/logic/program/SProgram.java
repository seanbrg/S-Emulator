package logic.program;

import logic.instructions.Instruction;
import logic.labels.Label;
import logic.variables.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SProgram implements Program {
    String name;
    List<Instruction> instructions;
    Map<String, Variable> variables;
    Map<Label, Instruction> labels;

    public SProgram(String name) {
        this.name = name;
        instructions = new ArrayList<>();
        variables = new HashMap<>();
        labels = new HashMap<>();
    }

    @Override
    public Variable run() {
        return null;
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
    public Map<String, Variable> getVars() {
        return variables;
    }

    @Override
    public Map<Label, Instruction> getLabels() {
        return labels;
    }

    @Override
    public boolean validate() {
        return false;
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
