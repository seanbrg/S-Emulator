package execute.components;

import logic.instructions.Instruction;
import logic.instructions.api.basic.Decrease;
import logic.instructions.api.basic.Increase;
import logic.instructions.api.basic.JumpNotZero;
import logic.instructions.api.basic.Neutral;
import logic.instructions.api.synthetic.*;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.program.Program;
import logic.program.SProgram;
import logic.variables.Var;
import logic.variables.Variable;
import logic.variables.VariableType;

import java.util.*;
import java.util.stream.Collectors;

public class ProgramManager {
    private Map<String, List<Program>> savedFunctions;
    private Map<String, Integer> maxDegrees;
    private String mainName;

    private Program programInDebug;
    private int debugLine;
    private LabelGenerator labelGenerator;
    private Map<String, Variable> tempVarsMap;
    private int currentTemps;


    public ProgramManager(Map<String, Variable> tempVarsMap) {
        this.labelGenerator = new  LabelGenerator();
        this.savedFunctions = new HashMap<>();
        this.maxDegrees = new HashMap<>();
        this.tempVarsMap = tempVarsMap;
        this.currentTemps = 0;
    }

    public static long runFunction(Program function, List<Variable> argsVars) {
        Set<Variable> innerFunctionVars = function.getVariables();
        Map<Integer, Variable> inputFunctionVars = new HashMap<>();

        for (Variable var : innerFunctionVars) {
            if (var.getType().equals(VariableType.INPUT)) {
                inputFunctionVars.put(var.getNum() - 1, var);
            }
        }

        Variable output = innerFunctionVars.stream()
                .filter(v -> v.getType().equals(VariableType.OUTPUT))
                .findFirst()
                .orElse(null);

        for (int i = 0; i < argsVars.size(); i++) {
            long input = argsVars.get(i).getValue();
            Variable inputVar = inputFunctionVars.get(i);
            if (inputVar != null) inputVar.setValue(input);
        }

        function.run();
        return output.getValue();
    }

    public String getMainProgramName() {
        return mainName;
    }

    public void loadNew(List<Program> programs) {
        this.clear();
        mainName = programs.getFirst().getName();
        for (Program func : programs) {
            savedFunctions.put(func.getName(), new ArrayList<>(List.of(func)));
            maxDegrees.put(func.getName(), func.maxDegree());
        }
    }

    public void clear() {
        savedFunctions.clear();
        maxDegrees.clear();
        labelGenerator.clear();
        currentTemps = tempVarsMap.values()
                .stream().max(Comparator.comparing(Variable::getNum))
                .map(Variable::getNum).orElse(0);
    }

    private Variable generateTempVar() {
        currentTemps++;
        Variable newVar = new Var(VariableType.TEMP, currentTemps, 0);
        tempVarsMap.put(newVar.getName(), newVar);
        return newVar;
    }

    public boolean isEmpty() {
        return savedFunctions.isEmpty();
    }

    public int maxDegree(String func) {
        return maxDegrees.get(func);
    }

    public Program getProgram(String func, int degree) {
        if (savedFunctions.isEmpty() || !savedFunctions.containsKey(func)) {
            System.err.println("Function " + func + " not found!");
            return null;
        }
        else {
            assert 0 <= degree && degree <= maxDegrees.get(func);
            if (degree >= savedFunctions.get(func).size()) {
                expand(func, degree);
            }
            return savedFunctions.get(func).get(degree);
        }
    }


    public int getProgramCycles(String func, int degree) {
        if (savedFunctions.isEmpty()) {
            return 0;
        }
        else {
            assert 0 <= degree && degree <= maxDegrees.get(func);
            if (degree >= savedFunctions.get(func).size()) {
                expand(func, degree);
            }
            return savedFunctions.get(func).get(degree).cycles();
        }
    }

    public void printProgram(String func, int degree) {
        assert 0 <= degree && degree <= maxDegrees.get(func);
        if (!savedFunctions.isEmpty()) {
            if (degree >= savedFunctions.get(func).size()) {
                expand(func, degree);
            }
            savedFunctions.get(func).get(degree)
                    .getInstructions()
                    .forEach(instr -> { System.out.println(instr.getRepresentation()); } );
        }
    }

    public void runProgram(String func, int degree) {
        assert 0 <= degree && degree <= maxDegrees.get(func);

        if (!savedFunctions.isEmpty()) {
            if (degree >= savedFunctions.get(func).size()) {
                expand(func, degree);
            }
            savedFunctions.get(func).get(degree).run();
        }
    }

    private void expand(String func, int degree) {
        assert 0 <= degree && degree <= maxDegrees.get(func);
        while (degree + 1 > savedFunctions.get(func).size()) {
            this.expandOnce(func);
        }
    }

    private void expandOnce(String func) {
        Program currentProgram = savedFunctions.get(func).getLast();
        List<Instruction> currentInstructions = currentProgram.getInstructions();
        List<Instruction> newInstructions = new ArrayList<>();

        labelGenerator.clear();
        labelGenerator.loadInstructionLabels(currentInstructions);

        for (Instruction instr : currentInstructions) {
            List<Variable> instrVars = instr.getVars();
            for (Variable v : instrVars) {
                if (v.getType() == VariableType.TEMP) {
                    tempVarsMap.put(v.getName(), v);
                }
            }
        }

        int lineNum = 1;
        for (Instruction instr: currentInstructions) {
            List<Instruction> expansion = this.expandInstruction(instr, lineNum);
            lineNum += expansion.size();
            newInstructions.addAll(expansion);
        }

        List<Label> currentLabels = labelGenerator.getLabels();
        Map<Label, Instruction> newLabels = new HashMap<>();
        for (Label label : currentLabels) {
            newInstructions.stream()
                    .filter(instr -> instr.getSelfLabel().equals(label))
                    .findFirst().ifPresent(labeledInstr -> newLabels.put(label, labeledInstr));
        }

        savedFunctions.get(func).add(new SProgram(currentProgram.getName(), newLabels, newInstructions, null));
    }

    private List<Instruction> expandInstruction(Instruction instr, int lineNum) {
        List<Instruction> result = new ArrayList<>();
        Label self = instr.getSelfLabel();
        //labelGenerator.addLabel(self);

        // ---- ZERO_VARIABLE ----
        if (instr instanceof ZeroVariable zv) {
            Variable v = zv.getVariable();
            Label loop = labelGenerator.newLabel();
            result.add(new JumpNotZero(self, v, loop, lineNum++, instr));
            result.add(new Decrease(loop, v, lineNum++, instr));
            result.add(new JumpNotZero(FixedLabel.EMPTY, v, loop, lineNum, instr));
        }

        // ---- CONSTANT_ASSIGNMENT ----
        else if (instr instanceof ConstantAssignment ca) {
            Variable v = ca.getVariable();
            int k = ca.getConstant();
            result.add(new ZeroVariable(self, v, lineNum++, instr));
            for (int i = 0; i < k; i++) {
                result.add(new Increase(FixedLabel.EMPTY, v, lineNum++, instr));
            }
        }

        // ---- ASSIGNMENT ----
        else if (instr instanceof Assignment asg) {
            Variable x = asg.getX();
            Variable y = asg.getY();
            Variable z = this.generateTempVar();
            Label l1 = labelGenerator.newLabel();
            Label l2 = labelGenerator.newLabel();
            Label l3 = labelGenerator.newLabel();

            result.add(new ZeroVariable(self, x, lineNum++, instr));
            result.add(new JumpNotZero(FixedLabel.EMPTY, y, l1, lineNum++, instr));
            result.add(new GoToLabel(FixedLabel.EMPTY, l3, lineNum++, instr));
            result.add(new Decrease(l1, y, lineNum++, instr));
            result.add(new Increase(FixedLabel.EMPTY, z, lineNum++, instr));
            result.add(new JumpNotZero(FixedLabel.EMPTY, y, l1, lineNum++, instr));
            result.add(new Decrease(l2, z, lineNum++, instr));
            result.add(new Increase(FixedLabel.EMPTY, x, lineNum++, instr));
            result.add(new Increase(FixedLabel.EMPTY, y, lineNum++, instr));
            result.add(new JumpNotZero(FixedLabel.EMPTY, z, l2, lineNum++, instr));
            result.add(new Neutral(l3, x, lineNum, instr));
        }

        // ---- GOTO_LABEL ----
        else if (instr instanceof GoToLabel gtl) {
            Label target = gtl.getTargetLabel();
            Variable dummy = this.generateTempVar();
            result.add(new Increase(self, dummy, lineNum++, instr));      // dummy = 1
            result.add(new JumpNotZero(FixedLabel.EMPTY, dummy, target, lineNum, instr));
        }

        // ---- JUMP_ZERO ----
        else if (instr instanceof JumpZero jz) {
            Variable v = jz.getVariable();
            Label target = jz.getTargetLabel();
            Label skip = labelGenerator.newLabel();
            result.add(new JumpNotZero(self, v, skip, lineNum++, instr));
            result.add(new GoToLabel(FixedLabel.EMPTY, target, lineNum++, instr));
            result.add(new Neutral(skip, v, lineNum, instr)); // skip:
        }

        // ---- JUMP_EQUAL_CONSTANT ----
        else if (instr instanceof JumpEqualConstant jec) {
            Variable v = jec.getVariable();
            int k = jec.getConstant();
            Label target = jec.getTargetLabel();

            Variable tmp = this.generateTempVar();
            result.add(new Assignment(self, tmp, v, lineNum++, instr));
            result.add(new ConstantAssignment(FixedLabel.EMPTY, tmp, k, lineNum++, instr));
            // subtract tmp - k loop
            Label loop = labelGenerator.newLabel();
            result.add(new JumpNotZero(FixedLabel.EMPTY, tmp, loop, lineNum++, instr));
            result.add(new Decrease(loop, tmp, lineNum++, instr));
            result.add(new JumpNotZero(FixedLabel.EMPTY, tmp, loop, lineNum++, instr));
            result.add(new JumpZero(FixedLabel.EMPTY, tmp, target, lineNum, instr));
        }

        // ---- JUMP_EQUAL_VARIABLE ----
        else if (instr instanceof JumpEqualVariable jev) {
            Variable v1 = jev.getVar1();
            Variable v2 = jev.getVar2();
            Label target = jev.getTargetLabel();

            Variable t1 = this.generateTempVar();
            Variable t2 = this.generateTempVar();
            result.add(new Assignment(self, t1, v1, lineNum++, instr));
            result.add(new Assignment(FixedLabel.EMPTY, t2, v2, lineNum++, instr));

            Label loop = labelGenerator.newLabel();
            result.add(new JumpNotZero(FixedLabel.EMPTY, t2, loop, lineNum++, instr));
            result.add(new Decrease(loop, t1, lineNum++, instr));
            result.add(new Decrease(FixedLabel.EMPTY, t2, lineNum++, instr));
            result.add(new JumpNotZero(FixedLabel.EMPTY, t2, loop, lineNum++, instr));
            result.add(new JumpZero(FixedLabel.EMPTY, t1, target, lineNum, instr));
        } else {
            result.add(instr);
        }

        return result;
    }

    public void debugStart(String func, int degree) {
        assert 0 <= degree && degree <= maxDegrees.get(func);

        if (!savedFunctions.isEmpty()) {
            if (degree >= savedFunctions.get(func).size()) {
                expand(func, degree);
            }
            programInDebug = savedFunctions.get(func).get(degree);
            debugLine = 1;
        }
    }

    public boolean debugStep() {
        int size = programInDebug.getInstructions().size();

        if (debugLine <= size) {
            Instruction instr = programInDebug.getInstructions().get(debugLine - 1);
            Label nextLabel = instr.execute();

            if (nextLabel.equals(FixedLabel.EMPTY)) {
                debugLine++;
                return debugLine <= size;
            } else if (nextLabel.equals(FixedLabel.EXIT)) {
                debugLine = size;
                return false;
            } else {
                OptionalInt nextLine = programInDebug.getInstructions().stream()
                        .filter(i -> i.getSelfLabel().equals(nextLabel))
                        .mapToInt(Instruction::getNum).findFirst();
                debugLine = nextLine.orElse(debugLine + 1);
                return debugLine <= size;
            }
        } else {
            return false;
        }
    }

    public int getDebugLine() {
        return debugLine;
    }

    public List<Label> getLabels(String func, int degree) {
        assert 0 <= degree && degree <= maxDegrees.get(func);

        if (!savedFunctions.isEmpty()) {
            if (degree >= savedFunctions.get(func).size()) {
                expand(func, degree);
            }
             return savedFunctions.get(func).get(degree).getLabels().keySet().stream().toList();
        }
        else return null;
    }

    public List<String> getFuncNamesList() {
        return new ArrayList<>(savedFunctions.keySet());
    }
}