package execute;

import logic.instructions.Instruction;
import logic.instructions.api.basic.Decrease;
import logic.instructions.api.basic.Increase;
import logic.instructions.api.basic.JumpNotZero;
import logic.instructions.api.basic.Neutral;
import logic.instructions.api.synthetic.*;
import logic.instructions.ExpandedInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.labels.NumericLabel;
import logic.program.Program;
import logic.program.SProgram;
import logic.variables.Var;
import logic.variables.Variable;
import logic.variables.VariableType;

import java.util.*;
import java.util.stream.Collectors;

public class EngineImpl implements Engine {
    Program currentProgram;
    Program currentExpandedProgram;
    Map<String, Variable> currentVars;
    List<Label> currentLabels;
    private static int tempCounter = 1;


    private final List<RunRecord> history = new ArrayList<>();
    private int runCounter = 0;

    public EngineImpl() {}

    @Override
    public boolean loadFromXML(String filePath) {
        Map<String, Variable> vars = new HashMap<>();
        Program program = XmlLoader.parse(filePath, vars);
        if (program != null) {
            this.currentProgram = program;
            this.currentVars = vars;
            System.out.println("Program '" + program.getName() + "' loaded successfully!");
            return true;
        } else {
            System.out.println("Program not loaded. Keeping previous program.");
            return false;
        }
    }

    @Override
    public void printProgram() {
        if (currentProgram == null) {
            System.out.println("No program loaded.");
            return;
        }
        List<Instruction> instrList = currentProgram.getInstructions();
        for (int i = 0; i < instrList.size(); i++) {
            Instruction instr = instrList.get(i);
            System.out.println(instr.getRepresentation());
        }
    }


    @Override
    public long runProgram(int degree, List<Long> inputs) {
        Program program = currentProgram;
        if (degree > 0) {
            currentLabels = currentProgram.getLabels().keySet().stream().collect(Collectors.toList());
            if (currentLabels == null) { currentLabels = new ArrayList<>(); }

            List<ExpandedInstruction> expanded = expandRecursive(
                    currentProgram.getInstructions(), degree, new ArrayList<>()
            );

            currentLabels = expanded.stream()
                    .map(ExpandedInstruction::getInstruction)
                    .map(Instruction::getSelfLabel)
                    .filter(label -> label instanceof NumericLabel)
                    .collect(Collectors.toList());

            List<Instruction> expandedInstrList = expanded.stream()
                    .map(ExpandedInstruction::getInstruction).toList();

            Map<Label, Instruction> expandedLabels = new HashMap<>();
            for (Instruction instr : expandedInstrList) {
                Label selfLabel = instr.getSelfLabel();
                if (selfLabel != FixedLabel.EMPTY && selfLabel != FixedLabel.EXIT) {
                    expandedLabels.put(selfLabel, instr);
                }
            }

            program = new SProgram(currentProgram.getName(), expandedLabels, expandedInstrList);
            currentExpandedProgram = program;
        }
        program.run();

        long result = currentVars.get("y").getValue();
        int cycles = program.cycles();

        runCounter++;
        history.add(new RunRecord(runCounter, degree, inputs, result, cycles));

        return result;
    }

    @Override
    public void resetVars() {
        currentVars.values().forEach(v -> v.setValue(0)); // reset vars
    }

    @Override
    public boolean validateProgram() {
        return currentProgram.checkLabels();
    }

    @Override
    public int maxDegree() {
        return currentProgram.maxDegree();
    }

    @Override
    public List<Variable> getInputs() {
        return currentVars.values().stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getType() == VariableType.INPUT)
                .sorted(Comparator.comparing(Variable::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<List<Variable>> getVarByType() {
        Variable y = currentVars.values().stream()
                .filter(var -> var.getType().equals(VariableType.OUTPUT))
                .findFirst()
                .orElse(new Var(VariableType.OUTPUT, 0));

        List<Variable> yList = List.of(y);

        List<Variable> xList = currentVars.values().stream()
                .filter(var -> var.getType().equals(VariableType.INPUT))
                .sorted(Comparator.comparing(Variable::getName))
                .toList();

        List<Variable> zList = currentVars.values().stream()
                .filter(var -> var.getType().equals(VariableType.TEMP))
                .sorted(Comparator.comparing(Variable::getName))
                .toList();

        return List.of(yList, xList, zList);
    }

    @Override
    public int getCycles(int degree) {
        if (degree == 0) return currentProgram.cycles();
        else return currentExpandedProgram.cycles();
    }

    // ========= printExpandProgram =========

    public void printExpandProgram(int degree) {
        if (currentProgram == null) {
            System.out.println("No program loaded.");
            return;
        }

        currentLabels = currentProgram.getInstructions().stream()
                .map(Instruction::getSelfLabel)
                .filter(label -> label instanceof NumericLabel)
                .collect(Collectors.toList());

        List<ExpandedInstruction> expanded = expandRecursive(
                currentProgram.getInstructions(), degree, new ArrayList<>()
        );

        for (int i = 0; i < expanded.size(); i++) { // number instructions
            expanded.get(i).setNum(i + 1);
        }

        for (int i = 0; i < expanded.size(); i++) {
            System.out.println(expanded.get(i).getRepresentation());
        }
    }

    private List<ExpandedInstruction> expandRecursive(List<Instruction> instrs,
                                                      int degree,
                                                      List<Instruction> history) {
        List<ExpandedInstruction> result = new ArrayList<>();

        int lineNum = 1;
        for (Instruction instr : instrs) {
            if (instr.getDegree() > 0 && degree > 0) {
                // expand synthetic instruction
                List<Instruction> expansion = expandOne(instr, lineNum);
                lineNum += expansion.size();
                for (Instruction child : expansion) {
                    List<Instruction> newHistory = new ArrayList<>(history);
                    newHistory.add(instr);
                    result.addAll(expandRecursive(List.of(child), degree - 1, newHistory));
                }
            } else {
                result.add(new ExpandedInstruction(instr, history));
            }
        }
        return result;
    }

    private List<Instruction> expandOne(Instruction instr, int lineNum) {
        List<Instruction> result = new ArrayList<>();
        Label self = instr.getSelfLabel();

        // ---- ZERO_VARIABLE ----
        if (instr instanceof ZeroVariable zv) {
            Variable v = zv.getVariable();
            Label loop = freshLabel(currentLabels);
            currentLabels.add(loop);
            result.add(new JumpNotZero(self, v, loop, lineNum++));
            result.add(new Decrease(loop, v, lineNum++));
            result.add(new JumpNotZero(FixedLabel.EMPTY, v, loop, lineNum++));
        }

        // ---- CONSTANT_ASSIGNMENT ----
        else if (instr instanceof ConstantAssignment ca) {
            Variable v = ca.getVariable();
            int k = ca.getConstant();
            result.add(new ZeroVariable(self, v, lineNum++));
            for (int i = 0; i < k; i++) {
                result.add(new Increase(FixedLabel.EMPTY, v, lineNum++));
            }
        }

        // ---- ASSIGNMENT ----
        else if (instr instanceof Assignment asg) {
            Variable x = asg.getX();
            Variable y = asg.getY();
            Label loop = freshLabel(currentLabels);
            currentLabels.add(loop);
            result.add(new ZeroVariable(self, x, lineNum++));
            result.add(new JumpNotZero(FixedLabel.EMPTY, y, loop, lineNum++));
            result.add(new Decrease(loop, y, lineNum++));
            result.add(new Increase(FixedLabel.EMPTY, x, lineNum++));
            result.add(new JumpNotZero(FixedLabel.EMPTY, y, loop, lineNum++));
        }

        // ---- GOTO_LABEL ----
        else if (instr instanceof GoToLabel gtl) {
            Label target = gtl.getTargetLabel();
            Variable dummy = freshTemp();
            result.add(new Increase(self, dummy, lineNum++));      // dummy = 1
            result.add(new JumpNotZero(FixedLabel.EMPTY, dummy, target, lineNum++));
        }

        // ---- JUMP_ZERO ----
        else if (instr instanceof JumpZero jz) {
            Variable v = jz.getVariable();
            Label target = jz.getTargetLabel();
            Label skip = freshLabel(currentLabels);
            currentLabels.add(skip);
            result.add(new JumpNotZero(self, v, skip, lineNum++));
            result.add(new GoToLabel(FixedLabel.EMPTY, target, lineNum++));
            result.add(new Neutral(skip, v, lineNum++)); // skip:
        }

        // ---- JUMP_EQUAL_CONSTANT ----
        else if (instr instanceof JumpEqualConstant jec) {
            Variable v = jec.getVariable();
            int k = jec.getConstant();
            Label target = jec.getTargetLabel();

            Variable tmp = freshTemp();
            result.add(new Assignment(self, tmp, v, lineNum++));
            result.add(new ConstantAssignment(FixedLabel.EMPTY, tmp, k, lineNum++));
            // subtract tmp - k loop
            Label loop = freshLabel(currentLabels);
            currentLabels.add(loop);
            result.add(new JumpNotZero(FixedLabel.EMPTY, tmp, loop, lineNum++));
            result.add(new Decrease(loop, tmp, lineNum++));
            result.add(new JumpNotZero(FixedLabel.EMPTY, tmp, loop, lineNum++));
            result.add(new JumpZero(FixedLabel.EMPTY, tmp, target, lineNum++));
        }

        // ---- JUMP_EQUAL_VARIABLE ----
        else if (instr instanceof JumpEqualVariable jev) {
            Variable v1 = jev.getVar1();
            Variable v2 = jev.getVar2();
            Label target = jev.getTargetLabel();

            Variable t1 = freshTemp();
            Variable t2 = freshTemp();
            result.add(new Assignment(self, t1, v1, lineNum++));
            result.add(new Assignment(FixedLabel.EMPTY, t2, v2, lineNum++));

            Label loop = freshLabel(currentLabels);
            currentLabels.add(loop);
            result.add(new JumpNotZero(FixedLabel.EMPTY, t2, loop, lineNum++));
            result.add(new Decrease(loop, t1, lineNum++));
            result.add(new Decrease(FixedLabel.EMPTY, t2, lineNum++));
            result.add(new JumpNotZero(FixedLabel.EMPTY, t2, loop, lineNum++));
            result.add(new JumpZero(FixedLabel.EMPTY, t1, target, lineNum++));
        } else {
            result.add(instr);
        }

        return result;
    }

    private static Label freshLabel(List<Label> labels) {
        int maxLabel = 0;
        if (labels != null) {
            maxLabel = labels.stream()
                    .map(Label::getNum)
                    .max(Integer::compareTo)
                    .orElse(0);
        }
        return new NumericLabel(maxLabel + 1);
    }

    private static Variable freshTemp() {
        return new Var("z" + (tempCounter++));
    }


    public void printHistory() {
        if (history.isEmpty()) {
            System.out.println("No runs recorded yet.");
            return;
        }
        for (RunRecord r : history) {
            System.out.printf("#%d | degree=%d | inputs=%s | y=%d | cycles=%d%n",
                    r.getRunId(), r.getDegree(), r.getInputs(), r.getResultY(), r.getCycles());
        }
    }
}
