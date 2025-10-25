package execute.components;

import logic.arguments.Argument;
import logic.arguments.QuoteArgument;
import logic.arguments.VarArgument;
import logic.instructions.Instruction;
import logic.instructions.api.basic.Decrease;
import logic.instructions.api.basic.Increase;
import logic.instructions.api.basic.JumpNotZero;
import logic.instructions.api.basic.Neutral;
import logic.instructions.api.synthetic.*;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.labels.NumericLabel;
import logic.program.Program;
import logic.program.SProgram;
import logic.variables.Var;
import logic.variables.Variable;
import logic.variables.VariableType;

import java.util.*;

public class ProgramManager {
    private Map<String, List<Program>> functionsExpansions;
    private Map<String, List<Program>> savedPrograms;
    private Map<String, Program> quotableFunctions;
    private Map<String, Integer> maxDegrees;

    private Program programInDebug;
    private int debugLine;
    private LabelGenerator labelGenerator;
    private Map<String, Variable> tempVarsMap;
    private int currentTemps;


    public ProgramManager(Map<String, Variable> tempVarsMap) {
        this.labelGenerator = new  LabelGenerator();
        this.functionsExpansions = new HashMap<>();
        this.savedPrograms = new HashMap<>();
        this.quotableFunctions = new HashMap<>();
        this.maxDegrees = new HashMap<>();
        this.tempVarsMap = tempVarsMap;
        this.currentTemps = 0;
    }

    public static long runFunction(Program function, List<Variable> argsVars) {
        Map<Integer, Variable> inputFunctionVars = function.getInputs();

        for (int i = 0; i < argsVars.size(); i++) {
            long input = argsVars.get(i).getValue();
            Variable inputVar = inputFunctionVars.get(i + 1);
            if (inputVar != null) inputVar.setValue(input);
        }

        function.run();
        return function.getOutput().getValue();
    }

    public String getFirstProgramName() {
        return savedPrograms.keySet().iterator().next();
    }

    public void loadNew(List<Program> programs) {
        savedPrograms.put(programs.getFirst().getName(), programs);
        for (Program func : programs) {
            functionsExpansions.put(func.getName(), new ArrayList<>(List.of(func)));
            maxDegrees.put(func.getName(), func.maxDegree());
        }
    }

    public void clear() {
        savedPrograms.clear();
        quotableFunctions.clear();
        functionsExpansions.clear();
        maxDegrees.clear();
        labelGenerator.clear();
        currentTemps = tempVarsMap.values()
                .stream().max(Comparator.comparing(Variable::getNum))
                .map(Variable::getNum).orElse(0);
    }

    public void refactorQuoteFunctions(List<Program> programs) {
        // deep-copy the quote functions to new ones with new vars and labels

        Set<Variable> usedVars = programs.getFirst().getVariables();
        Set<Label> usedLbls = new HashSet<>(programs.getFirst().getLabels().keySet());
        Map<Variable, Variable> newVarsMap = new HashMap<>();
        Map<Label, Label> newLblsMap = new HashMap<>();

        for (Program program : programs) {
            if (!quotableFunctions.containsKey(program.getName())) {
                String name = program.getName();
                String userStr = program.getUserStr();
                quotableFunctions.put(program.getName(), new SProgram(name, userStr));
            }
        }

        for (int i = 1; i < programs.size(); i++) {
            Map<Integer, Variable> inputVars = new HashMap<>(programs.get(i).getInputs());
            Variable outputVar = programs.get(i).getOutput();

            for (Variable quotedVar : programs.get(i).getVariables()) {
                if (usedVars.contains(quotedVar) && !newVarsMap.containsKey(quotedVar)) {
                    // a reused variable is encountered in the first time
                    Variable newVar = generateTempVar();
                    newVarsMap.put(quotedVar, newVar);
                    usedVars.add(newVar);

                    if (quotedVar.getType().equals(VariableType.OUTPUT)) {
                        outputVar = newVar;
                    }
                    else if (quotedVar.getType().equals(VariableType.INPUT)) {
                        inputVars.put(quotedVar.getNum(), newVar);
                    }
                }
            }

            Program editedFunc = quotableFunctions.get(programs.get(i).getName());
            editedFunc.setOutputVar(outputVar);
            editedFunc.setInputs(inputVars);

            for (Label quotedLbl : programs.get(i).getLabels().keySet()) {
                if (usedLbls.contains(quotedLbl) && !newLblsMap.containsKey(quotedLbl)) {
                    // a reused label is encountered in the first time
                    Label newLbl = labelGenerator.newLabel();
                    newLblsMap.put(quotedLbl, newLbl);
                    usedLbls.add(newLbl);
                }
            }
        }

        for (Label lbl : usedLbls) {
            if (!newLblsMap.containsKey(lbl)) {
                newLblsMap.put(lbl, lbl);
            }
        }
        for (Variable var : usedVars) {
            if (!newVarsMap.containsKey(var)) {
                newVarsMap.put(var, var);
            }
        }

        for (int i = 1; i < programs.size(); i++) {
            List<Instruction> newInstrList = new ArrayList<>();
            Map<Label, Instruction> newLabelsMap = new HashMap<>();
            for (Instruction instr : programs.get(i).getInstructions()) {
                Label oldSelfLabel = instr.getSelfLabel();
                Label newSelfLabel = oldSelfLabel;
                Label oldTgtLabel = instr.getTargetLabel();
                Label newTgtLabel = oldTgtLabel;

                if (oldSelfLabel instanceof NumericLabel) newSelfLabel = newLblsMap.get(oldSelfLabel);
                if (oldTgtLabel instanceof NumericLabel) newTgtLabel = newLblsMap.get(oldTgtLabel);

                Variable oldPrmVar = instr.getPrimaryVar();
                Variable oldScdVar = instr.getSecondaryVar();

                Instruction newInstr = copyInstr(instr, newSelfLabel, newTgtLabel,
                        newVarsMap.get(oldPrmVar), newVarsMap.get(oldScdVar), instr.getNum(), null, newVarsMap, newLblsMap);

                if (instr instanceof Quote quo) {
                    quo.setFunction(quotableFunctions.get(quo.getFunction().getName()));
                    quo.setArgs(recursiveFixArgs(quo.getArgs(), newVarsMap, newLblsMap));
                }

                if (newInstr != null) newInstrList.add(newInstr);
                if (newSelfLabel instanceof NumericLabel) newLabelsMap.put(newSelfLabel, newInstr);
            }
            Program editedFunc = quotableFunctions.get(programs.get(i).getName());
            editedFunc.setInstrList(newInstrList);
            editedFunc.setLabelMap(newLabelsMap);
        }
    }

    private Instruction copyInstr(Instruction instr, Label selfLabel, Label targetLabel,
                                  Variable primaryVar, Variable secondaryVar,
                                  int lineNum,
                                  Instruction parent,
                                  Map<Variable, Variable> newVarsMap,
                                  Map<Label, Label> newLblsMap) {

        return switch (instr.getData().name().toUpperCase()) {
            case "INCREASE" -> new Increase(selfLabel, primaryVar, lineNum, parent);
            case "DECREASE" -> new Decrease(selfLabel, primaryVar, lineNum, parent);
            case "JNZ" -> new JumpNotZero(selfLabel, primaryVar, targetLabel, lineNum, parent);
            case "NO_OP" -> new Neutral(selfLabel, primaryVar, lineNum, parent);
            case "ZERO_VARIABLE" -> new ZeroVariable(selfLabel, primaryVar, lineNum, parent);
            case "GOTO_LABEL" -> new GoToLabel(selfLabel, targetLabel, lineNum, parent);
            case "ASSIGNMENT" -> new Assignment(selfLabel, primaryVar, secondaryVar, lineNum, parent);
            case "CONSTANT_ASSIGNMENT" -> new ConstantAssignment(selfLabel, primaryVar, instr.getConst(), lineNum, parent);
            case "JUMP_ZERO" -> new JumpZero(selfLabel, primaryVar, targetLabel, lineNum, parent);
            case "JUMP_EQUAL_CONSTANT" -> new JumpEqualConstant(selfLabel, primaryVar, instr.getConst(), targetLabel, lineNum, parent);
            case "JUMP_EQUAL_VARIABLE" -> new JumpEqualVariable(selfLabel, primaryVar, secondaryVar, targetLabel, lineNum, parent);
            case "QUOTE" -> {
                String funcName = ((Quote)instr).getFunction().getName();
                Program newFunc = quotableFunctions.get(funcName);
                List<Argument> newArgs = recursiveFixArgs(((Quote)instr).getArgs(), newVarsMap, newLblsMap);
                yield new Quote(selfLabel, primaryVar, newFunc, newArgs, lineNum, parent);
            }
            case "JUMP_EQUAL_FUNCTION" -> {
                Quote oldQuote = ((JumpEqualFunction)instr).getQuoteArg().getQuoteInstruction();
                Quote newQuote = (Quote) copyInstr(oldQuote, oldQuote.getSelfLabel(), oldQuote.getTargetLabel(),
                        oldQuote.getPrimaryVar(), oldQuote.getSecondaryVar(), lineNum, parent, newVarsMap, newLblsMap);
                yield new JumpEqualFunction(selfLabel, new QuoteArgument(newQuote), primaryVar, targetLabel);
            }

            default -> null;
        };
    }

    private List<Argument> recursiveFixArgs(List<Argument> args,
                                            Map<Variable, Variable> newVarsMap, Map<Label, Label> newLblsMap) {
        List<Argument> newArgs = new ArrayList<>();
        for (Argument arg : args) {
            if (arg instanceof VarArgument varArg) {
                newArgs.add(varArg);
            }
            if (arg instanceof QuoteArgument quoteArg) {
                List<Argument> innerArgs = recursiveFixArgs(quoteArg.getQuoteInstruction().getArgs(), newVarsMap, newLblsMap);
                Program innerFunc = quotableFunctions.get(quoteArg.getQuoteInstruction().getFunction().getName());

                Label innerSelfLabel = quoteArg.getQuoteInstruction().getSelfLabel();
                if (newLblsMap != null) {
                    innerSelfLabel = newLblsMap.get(innerSelfLabel);
                }

                Variable innerVar = generateTempVar();
                newArgs.add(new QuoteArgument(new Quote(innerSelfLabel, innerVar, innerFunc, innerArgs)));
            }
        }
        return newArgs;
    }

    private Variable generateTempVar() {
        currentTemps++;
        Variable newVar = new Var(VariableType.TEMP, currentTemps, 0);
        tempVarsMap.put(newVar.getName(), newVar);
        return newVar;
    }

    public boolean isEmpty() {
        return savedPrograms.isEmpty();
    }

    public int maxDegree(String func) {
        return maxDegrees.get(func);
    }

    public Program getFunction(String func, int degree) {
        if (functionsExpansions.isEmpty() || !functionsExpansions.containsKey(func)) {
            System.err.println("Function " + func + " not found!");
            return null;
        }
        else {
            assert 0 <= degree && degree <= maxDegrees.get(func);
            if (degree >= functionsExpansions.get(func).size()) {
                expand(func, degree);
            }
            return functionsExpansions.get(func).get(degree);
        }
    }

    public List<Program> getProgramAndFunctions(String func) {
        if (savedPrograms.isEmpty() || !savedPrograms.containsKey(func)) {
            System.err.println("Function " + func + " not found!");
            return null;
        }
        else {
            return savedPrograms.get(func);
        }
    }

    public int getProgramCycles(String func, int degree) {
        if (functionsExpansions.isEmpty()) {
            return 0;
        }
        else {
            assert 0 <= degree && degree <= maxDegrees.get(func);
            if (degree >= functionsExpansions.get(func).size()) {
                expand(func, degree);
            }
            return functionsExpansions.get(func).get(degree).cycles();
        }
    }

    public void printProgram(String func, int degree) {
        assert 0 <= degree && degree <= maxDegrees.get(func);
        if (!functionsExpansions.isEmpty()) {
            if (degree >= functionsExpansions.get(func).size()) {
                expand(func, degree);
            }
            functionsExpansions.get(func).get(degree)
                    .getInstructions()
                    .forEach(instr -> { System.out.println(instr.getRepresentation()); } );
        }
    }

    public void runProgram(String func, int degree) {
        assert 0 <= degree && degree <= maxDegrees.get(func);

        if (!functionsExpansions.isEmpty()) {
            if (degree >= functionsExpansions.get(func).size()) {
                expand(func, degree);
            }
            functionsExpansions.get(func).get(degree).run();
        }
    }

    private void expand(String func, int degree) {
        assert 0 <= degree && degree <= maxDegrees.get(func);
        while (degree + 1 > functionsExpansions.get(func).size()) {
            this.expandOnce(func);
        }
    }

    private void expandOnce(String func) {
        Program currentProgram = functionsExpansions.get(func).getLast();
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

        functionsExpansions.get(func).add(new SProgram(currentProgram.getName(), newLabels, newInstructions, null));
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
        }

        // ---- QUOTE ----
        else if (instr instanceof Quote quo) {
            result.addAll(expandQuote(quo, lineNum));
        }

        // ---- JUMP_EQUAL_FUNCTION ----
        else if (instr instanceof JumpEqualFunction jef) {
            Quote oldQuote = jef.getQuoteArg().getQuoteInstruction();
            Variable v1 = oldQuote.getPrimaryVar();
            Variable v2 = jef.getPrimaryVar();
            Label tgt = jef.getTargetLabel();
            result.add(new Quote(self, v1, oldQuote.getFunction(), oldQuote.getArgs(), lineNum++, instr));
            result.add(new JumpEqualVariable(FixedLabel.EMPTY, v2, v1, tgt, lineNum, instr));
        }

        else {
            result.add(instr);
        }

        return result;
    }

    private List<Instruction> expandQuote(Quote quo, int lineNum) {
        List<Instruction> result = new ArrayList<>();
        Program func = quotableFunctions.get(quo.getFunction().getName());
        List<Instruction> funcInstrList = func.getInstructions();
        Map<Integer, Variable> funcInputs = func.getInputs();
        List<Argument> arguments = quo.getArgs();
        List<Variable> varArgs = new ArrayList<>();

        result.add(new Neutral(quo.getSelfLabel(), quo.getPrimaryVar(), lineNum++, quo));

        for (Argument arg : arguments) {
            if (arg instanceof VarArgument varArg) {
                varArgs.add(varArg.get());
            }
            else if (arg instanceof QuoteArgument quoteArg) {
                Quote inner = quoteArg.getQuoteInstruction();
                result.add(new Quote(FixedLabel.EMPTY, inner.getPrimaryVar(), inner.getFunction(), inner.getArgs(), lineNum++, quo));
                varArgs.add(inner.getPrimaryVar());
            }
        }

        for (int i = 0; i < varArgs.size(); i++) {
            Variable target = funcInputs.get(i + 1);
            Variable source = varArgs.get(i);

            result.add(new Assignment(FixedLabel.EMPTY, target, source, lineNum++, quo));
        }

        for (Instruction instr : funcInstrList) {
            result.add(copyInstr(instr, instr.getSelfLabel(), instr.getTargetLabel(),
                            instr.getPrimaryVar(), instr.getSecondaryVar(), lineNum++, quo,null, null));
        }

        Variable output = func.getOutput();
        result.add(new Assignment(FixedLabel.EMPTY, quo.getPrimaryVar(), output, lineNum, quo));

        return result;
    }

    public void debugStart(String func, int degree) {
        assert 0 <= degree && degree <= maxDegrees.get(func);

        if (!functionsExpansions.isEmpty()) {
            if (degree >= functionsExpansions.get(func).size()) {
                expand(func, degree);
            }
            programInDebug = functionsExpansions.get(func).get(degree);
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

        if (!functionsExpansions.isEmpty()) {
            if (degree >= functionsExpansions.get(func).size()) {
                expand(func, degree);
            }
             return functionsExpansions.get(func).get(degree).getLabels().keySet().stream().toList();
        }
        else return null;
    }

    public List<String> getFuncNamesList() {
        return new ArrayList<>(functionsExpansions.keySet());
    }

    public List<Program> getAllPrograms() {
        return savedPrograms.values().stream()
                .map(list -> list.stream().findFirst().orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }


}