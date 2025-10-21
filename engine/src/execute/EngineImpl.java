package execute;

import execute.components.XmlFileLoader;
import execute.dto.*;
import execute.components.ProgramManager;
import logic.program.Program;
import logic.variables.Var;
import logic.variables.Variable;
import logic.variables.VariableType;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class EngineImpl implements Engine {
    private Map<String, Variable> inputVarsMap;
    private Map<String, Variable> tempVarsMap;
    private Variable outputVar;
    private ProgramManager pm;
    private final List<HistoryDTO> history;
    private int runCounter;
    boolean printMode;

    public EngineImpl() {
        this.tempVarsMap = new HashMap<>();
        this.inputVarsMap = new HashMap<>();
        this.pm = new ProgramManager(tempVarsMap);
        this.history = new ArrayList<>();
        printMode = true;
        runCounter = 0;
    }

    @Override
    public void setPrintMode(boolean mode) { this.printMode = mode; }

    @Override
    public boolean isLoaded() {
        return !pm.isEmpty();
    }

    @Override
    public String getMainProgramName() { return pm.getMainProgramName(); }

    @Override
    public void resetVars() {
        inputVarsMap.values().forEach(v -> v.setValue(0));
    }

    @Override
    public boolean validateProgram(String program, int degree) {
        return pm.getProgram(program, degree).checkLabels();
    }

    @Override
    public int maxDegree(String func) {
        return pm.maxDegree(func);
    }

    @Override
    public List<List<VariableDTO>> getVarByType() {

        List<VariableDTO> xList = inputVarsMap.values() // snapshot
                .stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Variable::getName))
                .map(VariableDTO::new)
                .toList();


        List<VariableDTO> yList = List.of(new VariableDTO(outputVar));

        List<VariableDTO> zList = tempVarsMap.values()
                .stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Variable::getName))
                .map(VariableDTO::new)
                .toList();

        return List.of(yList, xList, zList);
    }

    @Override
    public List<VariableDTO> getOutputs(String func, int degree) {
        List<VariableDTO> result = new ArrayList<>();
        Set<String> varStr = pm.getProgram(func, degree).getVariables()
                .stream().map(Variable::getName).collect(Collectors.toSet());
        List<List<VariableDTO>> varsByType = this.getVarByType();

        for (List<VariableDTO> list : varsByType) {
            for (VariableDTO variableDTO : list) {
                if (varStr.contains(variableDTO.getName())) {
                    result.add(variableDTO);
                }
            }
        }

        return result;
    }

    @Override
    public List<VariableDTO> getInputs(String func, int degree) {
        Program function = pm.getProgram(func, degree);

        Set<Variable> innerFunctionVars = function.getVariables();
        List<VariableDTO> result = new ArrayList<>();

        for (Variable var : innerFunctionVars) {
            if (var.getType().equals(VariableType.INPUT)) {
                result.add(new VariableDTO(var));
            }
        }

        return result;
    }

    @Override
    public int getCycles(String program, int degree) {
        return pm.getProgram(program, degree).cycles();
    }

    @Override
    public boolean loadFromXML(String filePath) {
        Map<String, Variable> vars = new HashMap<>();
        XmlFileLoader loader = new XmlFileLoader();
        List<Program> programs = loader.parseDoc(filePath, vars, printMode);

        if (programs != null) {
            this.fillOutVars(vars);
            pm.refactorQuoteFunctions(programs);
            pm.loadNew(programs);
            this.history.clear();
            if (printMode) System.out.println("Program '" + programs.getLast().getName() + "' loaded successfully!");
            return true;
        } else {
            if (printMode) System.out.println("Program not loaded. Keeping previous program.");
            return false;
        }
    }

    @Override
    public boolean loadFromStream(InputStream inputStream) {
        Map<String, Variable> vars = new HashMap<>();
        XmlFileLoader loader = new XmlFileLoader();
        List<Program> programs = loader.parseStream(inputStream, vars, printMode);

        if (programs != null) {
            this.fillOutVars(vars);
            pm.refactorQuoteFunctions(programs);
            pm.loadNew(programs);
            this.history.clear();
            if (printMode) System.out.println("Program '" + programs.getLast().getName() + "' loaded successfully!");
            return true;
        } else {
            if (printMode) System.out.println("Program not loaded. Keeping previous program.");
            return false;
        }
    }

    @Override
    public void fillOutVars(Map<String, Variable> vars) {
        for (Variable variable : vars.values()) {
            if (variable.getType() == VariableType.INPUT) {
                this.inputVarsMap.put(variable.getName(), variable);
            }
            else if (variable.getType() == VariableType.OUTPUT) {
                this.outputVar = variable;
            }
            else if (variable.getType() == VariableType.TEMP) {
                this.tempVarsMap.put(variable.getName(), variable);
            }
        }
    }

    @Override
    public void loadInputs(List<VariableDTO> inputVarsDTO) {
        for (VariableDTO variableDTO : inputVarsDTO) {
            if (inputVarsMap.containsKey(variableDTO.getName())) {
                inputVarsMap.get(variableDTO.getName()).setValue(variableDTO.getValue());
            }
            else {
                inputVarsMap.put(variableDTO.getName(), new Var(variableDTO));
            }
        }
    }

    @Override
    public List<InstructionDTO> getInstructionsList(String program, int degree) {
        if (pm.isEmpty()) return Collections.emptyList();
        else return pm.getProgram(program, degree).getInstructions().stream()
                .map(InstructionDTO::new)
                .toList();
    }

    @Override
    public List<InstructionDTO> getInstrParents(InstructionDTO selectedInstr) {
        List<InstructionDTO> result = new ArrayList<>();

        for (InstructionDTO current = selectedInstr; current != null; current = current.getParent()) {
            result.add(current);
        }

        return result;
    }

    @Override
    public void printProgram(String program, int degree) {
        if (pm.isEmpty()) {
            System.out.println("No program loaded.");
            return;
        }
        pm.printProgram(program, degree);
    }

    public void printHistory() {
        if (history.isEmpty()) {
            System.out.println("No runs recorded yet.");
            return;
        }
        for (HistoryDTO r : history) {
            System.out.printf("#%d | degree = %d | inputs = %s | outputs = %s | cycles = %d%n",
                    r.getNum(), r.getDegree(), r.getInputs().toString(), r.getOutputAndTemps().getFirst().getValue(), r.getCycles());
        }
    }

    @Override
    public long runProgram(String programName, int degree) {
        outputVar.setValue(0);
        tempVarsMap.values()
                .stream()
                .filter(Objects::nonNull)
                .forEach(v -> v.setValue(0));
        pm.runProgram(programName, degree);
        return outputVar.getValue();
    }

    @Override
    public HistoryDTO runProgramAndRecord(String program, int degree, List<VariableDTO> inputs) {
        // Load input values into the program
        loadInputs(inputs);

        // Capture actual input variable values after loading
        List<VariableDTO> currentInputs = pm.getProgram(program, degree).getVariables().stream()
                .filter(v -> v.getType() == VariableType.INPUT)
                .map(v -> new VariableDTO(v.getName(), v.getValue()))
                .toList();

        // Reset outputs and temp variables
        outputVar.setValue(0);
        tempVarsMap.values().forEach(v -> v.setValue(0));
        int maxDegree = pm.maxDegree(program);

        // Run the program
        pm.runProgram(program, degree);

        // Capture output variables
        List<VariableDTO> outputs = getOutputs(program, degree);

        // Get cycles
        int cycles = pm.getProgramCycles(program, degree);

        // Increment run counter
        runCounter++;

        // Wrap program in DTO
        ProgramDTO programDTO = new ProgramDTO(pm.getProgram(program, degree));

        // Create history entry with actual input/output values
        HistoryDTO result = new HistoryDTO(runCounter, degree, maxDegree, cycles, programDTO, currentInputs, outputs);

        // Optional print
        if (printMode) {
            System.out.printf("Run #%d complete: inputs: %s, outputs: %s, cycles = %d%n",
                    runCounter, currentInputs.toString(), outputs.toString(), cycles);
        }

        // Add to history
        history.add(result);

        return result;
    }



    @Override
    public HistoryDTO recordCurrentState(String programName, int degree, List<VariableDTO> inputs) {
        runCounter++;
        int cycles = pm.getProgramCycles(programName, degree);
        int maxDegree = pm.maxDegree(programName);

        HistoryDTO result = new HistoryDTO(runCounter, degree, maxDegree, cycles,
                new ProgramDTO(pm.getProgram(programName, degree)),
                inputs,
                getOutputs(programName, degree));

        history.add(result);
        return result;
    }

    @Override
    public void debugStart(String programName, int degree, List<VariableDTO> inputs) {
        loadInputs(inputs);
        outputVar.setValue(0);
        tempVarsMap.values()
                .stream()
                .filter(Objects::nonNull)
                .forEach(v -> v.setValue(0));
        pm.debugStart(programName, degree);
    }

    @Override
    public boolean debugStep(String programName, int degree) {
        return pm.debugStep();
    }

    @Override
    public int getDebugLine() {
        return pm.getDebugLine();
    }

    @Override
    public List<LabelDTO> getLabels(String programName, int degree) {
        return pm.getLabels(programName, degree).stream()
                .map(LabelDTO::new)
                .toList();
    }

    @Override
    public List<String> getFuncNamesList() {
        return pm.getFuncNamesList();
    }

    @Override
    public void clear() {
        pm.clear();
        inputVarsMap.clear();
        tempVarsMap.clear();
        history.clear();
        outputVar = null;
    }

}
