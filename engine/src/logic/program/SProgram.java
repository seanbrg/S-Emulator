package logic.program;

import logic.instructions.Instruction;
import logic.labels.FixedLabel;
import logic.labels.Label;

import java.util.*;

public class SProgram implements Program {
    private String name;
    private List<Instruction> instructions;
    private Map<Label, Instruction> labels;

    public SProgram(String name, Map<Label, Instruction> labels) {
        this.name = name;
        this.instructions = new ArrayList<>();
        this.labels = labels; // labels must map each label to its instruction
    }

    public SProgram(String name, Map<Label, Instruction> labels, List<Instruction> instructions) {
        this.name = name;
        this.instructions = instructions;
        this.labels = labels; // labels must map each label to its instruction
    }


    @Override
    public void run(int degree) {
        Label currentLabel = FixedLabel.EMPTY;
        ListIterator<Instruction> iterator = instructions.listIterator();


        while (currentLabel != FixedLabel.EXIT &&
                (iterator.hasNext() || currentLabel != FixedLabel.EMPTY)) {
            Instruction currentInstruction;

            if (currentLabel == FixedLabel.EMPTY) {
                currentInstruction = iterator.next();
            }
            else {
                currentInstruction = labels.get(currentLabel);
                if (currentInstruction == null) {
                    throw new IllegalStateException("Unknown label: " + currentLabel.getLabel());
                }
                int idx = instructions.indexOf(currentInstruction);
                if (idx < 0) {
                    throw new IllegalStateException("Label not in instruction list: " + currentLabel.getLabel());
                }
                iterator = instructions.listIterator(Math.min(idx + 1, instructions.size()));
            }
            currentLabel = currentInstruction.execute();
        }
    }

    @Override
    public void addInstruction(Instruction instruction) {
        // the variables in instruction must be the same instances saved in
        // tempVars and inputVars - the provided lists of variables in the program
        Label selfLabel = instruction.getSelfLabel();
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
