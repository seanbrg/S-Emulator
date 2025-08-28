package execute;

import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.instructions.api.basic.Decrease;
import logic.instructions.api.basic.Increase;
import logic.instructions.api.basic.JumpNotZero;
import logic.instructions.api.basic.Neutral;
import logic.instructions.api.synthetic.*;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.labels.NumericLabel;
import logic.program.Program;
import logic.variables.Var;
import logic.variables.Variable;
import logic.variables.VariableType;

import java.util.*;
import java.util.stream.Collectors;

public class EngineImpl implements Engine {
    Program currentProgram;
    Map<String, Variable> currentVars;

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
            System.out.println(instr.getRepresentation(i + 1));
        }
    }

    @Override
    public long runProgram(int degree) {
        currentProgram.run(degree);
        long result = currentVars.get("y").getValue();
        currentVars.values().forEach(v -> v.setValue(0)); // reset vars
        return result;
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

    // ========= expandProgram =========

    public void expandProgram(int degree) {
        if (currentProgram == null) {
            System.out.println("No program loaded.");
            return;
        }

        List<ExpandedInstruction> expanded = expandRecursive(
                currentProgram.getInstructions(), degree, new ArrayList<>()
        );

        for (int i = 0; i < expanded.size(); i++) {
            System.out.println(expanded.get(i).getRepresentation(i + 1));
        }
    }

    private List<ExpandedInstruction> expandRecursive(List<Instruction> instrs,
                                                      int degree,
                                                      List<Instruction> history) {
        List<ExpandedInstruction> result = new ArrayList<>();

        for (Instruction instr : instrs) {
            if (instr.getDegree() > 0 && degree > 0) {
                // expand synthetic instruction
                List<Instruction> expansion = expandOne(instr); //todoo
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

    private List<Instruction> expandOne(Instruction instr) {
        List<Instruction> result = new ArrayList<>();
        Label self = instr.getSelfLabel();

        // ---- ZERO_VARIABLE ----
        if (instr instanceof ZeroVariable zv) {
            Variable v = zv.getVariable();
            Label loop = freshLabel();
            result.add(new JumpNotZero(self, v, loop));
            result.add(new Decrease(loop, v));
            result.add(new JumpNotZero(FixedLabel.EMPTY, v, loop));
        }

        // ---- CONSTANT_ASSIGNMENT ----
        else if (instr instanceof ConstantAssignment ca) {
            Variable v = ca.getVariable();
            int k = ca.getConstant();
            result.add(new ZeroVariable(self, v));
            for (int i = 0; i < k; i++) {
                result.add(new Increase(FixedLabel.EMPTY, v));
            }
        }

        // ---- ASSIGNMENT ----
        else if (instr instanceof Assignment asg) {
            Variable x = asg.getX();
            Variable y = asg.getY();
            Label loop = freshLabel();
            result.add(new ZeroVariable(self, x));
            result.add(new JumpNotZero(FixedLabel.EMPTY, y, loop));
            result.add(new Decrease(loop, y));
            result.add(new Increase(FixedLabel.EMPTY, x));
            result.add(new JumpNotZero(FixedLabel.EMPTY, y, loop));
        }

        // ---- GOTO_LABEL ----
        else if (instr instanceof GoToLabel gtl) {
            Label target = gtl.getTargetLabel();
            Variable dummy = freshTemp();
            result.add(new Increase(self, dummy));      // dummy = 1
            result.add(new JumpNotZero(FixedLabel.EMPTY, dummy, target));
        }

        // ---- JUMP_ZERO ----
        else if (instr instanceof JumpZero jz) {
            Variable v = jz.getVariable();
            Label target = jz.getTargetLabel();
            Label skip = freshLabel();
            result.add(new JumpNotZero(self, v, skip));
            result.add(new GoToLabel(FixedLabel.EMPTY, target));
            result.add(new Neutral(skip, v)); // skip:
        }

        // ---- JUMP_EQUAL_CONSTANT ----
        else if (instr instanceof JumpEqualConstant jec) {
            Variable v = jec.getVariable();
            int k = jec.getConstant();
            Label target = jec.getTargetLabel();

            Variable tmp = freshTemp();
            result.add(new Assignment(self, tmp, v));
            result.add(new ConstantAssignment(FixedLabel.EMPTY, tmp, k));
            // subtract tmp - k loop
            Label loop = freshLabel();
            result.add(new JumpNotZero(FixedLabel.EMPTY, tmp, loop));
            result.add(new Decrease(loop, tmp));
            result.add(new JumpNotZero(FixedLabel.EMPTY, tmp, loop));
            result.add(new JumpZero(FixedLabel.EMPTY, tmp, target));
        }

        // ---- JUMP_EQUAL_VARIABLE ----
        else if (instr instanceof JumpEqualVariable jev) {
            Variable v1 = jev.getVar1();
            Variable v2 = jev.getVar2();
            Label target = jev.getTargetLabel();

            Variable t1 = freshTemp();
            Variable t2 = freshTemp();
            result.add(new Assignment(self, t1, v1));
            result.add(new Assignment(FixedLabel.EMPTY, t2, v2));

            Label loop = freshLabel();
            result.add(new JumpNotZero(FixedLabel.EMPTY, t2, loop));
            result.add(new Decrease(loop, t1));
            result.add(new Decrease(FixedLabel.EMPTY, t2));
            result.add(new JumpNotZero(FixedLabel.EMPTY, t2, loop));
            result.add(new JumpZero(FixedLabel.EMPTY, t1, target));
        }

        else {
            result.add(instr);
        }

        return result;
    }


    private static class ExpandedInstruction {
        private final Instruction instruction;
        private final List<Instruction> history;

        ExpandedInstruction(Instruction instr, List<Instruction> parentHistory) {
            this.instruction = instr;
            this.history = new ArrayList<>(parentHistory);
        }

        String getRepresentation(int num) {
            StringBuilder sb = new StringBuilder(instruction.getRepresentation(num));
            for (Instruction h : history) {
                sb.append("  <<<  ").append(h.getRepresentation(-1));
            }
            return sb.toString();
        }
    }
    private static int labelCounter = 1;
    private static int tempCounter = 1;

    private static Label freshLabel() {
        return new NumericLabel(labelCounter++);
    }

    private static Variable freshTemp() {
        return new Var("z" + (tempCounter++));
    }

}
