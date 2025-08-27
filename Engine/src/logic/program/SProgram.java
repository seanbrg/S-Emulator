package logic.program;

import logic.instructions.Instruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Var;
import logic.variables.Variable;
import logic.variables.VariableType;

import java.util.*;

public class SProgram implements Program {
    private String name;
    private List<Instruction> instructions;
    private VariablesList tempVars;
    private Map<Label, Instruction> labels;

    public SProgram(String name, VariablesList tempVars, Map<Label, Instruction> labels) {
        this.name = name;
        this.instructions = new ArrayList<>();
        this.tempVars = tempVars;
        // tempVars is a VariablesList( () -> new Var(VariableType.TEMP, tempVariables.size()) )
        // a list of Vars that creates a new Var equal to 0 when it is first added
        this.labels = labels; // labels must map each label to its instruction
    }

    @Override
    public Variable run(int degree, VariablesList inputVars) {
        Variable result = new Var(VariableType.OUTPUT, -1);

        Label currentLabel = FixedLabel.EMPTY;
        ListIterator<Instruction> iterator = instructions.listIterator();

        while (iterator.hasNext() && currentLabel != FixedLabel.EMPTY) {
            Instruction currentInstruction;

            if (currentLabel == FixedLabel.EMPTY) {
                currentInstruction =  iterator.next();
            }
            else {
                currentInstruction = labels.get(currentLabel);
                int idx = instructions.indexOf(currentInstruction);
                if (idx == -1) {
                    iterator =  instructions.listIterator(idx);
                }
            }
            currentLabel = currentInstruction.execute();
        }

        return result;
    }

    @Override
    public void addInstruction(Instruction instruction) {
        // the variables in instruction must be the same instances saved in
        // tempVars and inputVars - the provided lists of variables in the program
        Label selfLabel = instruction.getLabel();
        if (selfLabel != FixedLabel.EMPTY) {
            labels.put(selfLabel, instruction);
        }
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
        return tempVars;
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
