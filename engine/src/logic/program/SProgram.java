package logic.program;

import logic.instructions.Instruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Variable;
import logic.variables.VariableType;

import java.util.*;

public class SProgram implements Program {
    private String userStr;
    private String name;
    private List<Instruction> instructions;
    private Map<Label, Instruction> labels;
    private Set<Variable> vars;
    private Map<Integer, Variable> inputVars;
    private Variable outputVar;

    public SProgram(String name, Map<Label, Instruction> labels, String userStr) {
        this.userStr = userStr == null ? name : userStr;
        this.name = name;
        this.instructions = new ArrayList<>();
        this.labels = labels; // labels must map each label to its instruction
    }

    public SProgram(String name, Map<Label, Instruction> labels, List<Instruction> instructions, String userStr) {
        this.userStr = userStr == null ? name : userStr;
        this.name = name;
        this.instructions = instructions;
        this.labels = labels; // labels must map each label to its instruction

        for (int i = 0; i < instructions.size(); i++) { // number instructions
            instructions.get(i).setNum(i + 1);
        }

        findVariables();
    }

    public SProgram(String name, String userStr) {
        this.userStr = userStr == null ? name : userStr;
        this.name = name;
        this.instructions = new ArrayList<>();
        this.labels = new HashMap<>();
    }


    @Override
    public void run() {
        Label nextLabel;
        int pc = 0;

        while (0 <= pc && pc < instructions.size()) {
            Instruction current = instructions.get(pc);
            nextLabel = current.execute();
            //System.out.println("pc = " + pc); // for debugging
            if (FixedLabel.EXIT == nextLabel) {
                break;
            }

            if (FixedLabel.EMPTY != nextLabel) {
                Instruction target = labels.get(nextLabel);
                if (target == null) {
                    throw new IllegalStateException("Unknown label: " + nextLabel.getLabel());
                }
                int idx = instructions.indexOf(target);
                if (idx < 0) {
                    throw new IllegalStateException("Label not in instruction list: " + nextLabel.getLabel());
                }
                pc = idx;
            } else {
                pc += 1;
            }
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
        instruction.setNum(instructions.size());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUserStr() { return userStr; }

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

    @Override
    public boolean checkLabels() {
        List<Label> usedLabels = new ArrayList<>();
        for (Instruction instruction : instructions) {
            Label usedLabel = instruction.getTargetLabel();
            if (usedLabel != FixedLabel.EMPTY && usedLabel != FixedLabel.EXIT) {
                usedLabels.add(usedLabel);
            }
        }

        for  (Label label : usedLabels) {
            if (!labels.containsKey(label)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setInstrList(List<Instruction> newInstrList) {
        this.instructions = newInstrList;
    }

    @Override
    public void setLabelMap(Map<Label, Instruction> newLabelsMap) {
        this.labels = newLabelsMap;
    }

    @Override
    public Set<Variable> getVariables() {
        return vars;
    }

    @Override
    public Map<Integer, Variable> getInputs() {
        return inputVars;
    }

    @Override
    public Variable getOutput() {
        return outputVar;
    }

    @Override
    public void setOutputVar(Variable newVar) {
        this.outputVar = newVar;
    }

    @Override
    public void setInputs(Map<Integer, Variable> inputVars) {
        this.inputVars = inputVars;
    }

    @Override
    public void findVariables() {
        vars = new HashSet<>();
        inputVars = new HashMap<>();

        for (Instruction instr : instructions) {
            vars.addAll(instr.getVars());
        }

        for (Variable var : vars) {
            if (var.getType().equals(VariableType.INPUT)) {
                inputVars.put(var.getNum(), var);
            }
        }

        outputVar = vars.stream().filter(var -> var.getType().equals(VariableType.OUTPUT)).findFirst().orElse(null);
    }

}
