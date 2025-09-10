package execute;

import execute.dto.HistoryDTO;
import execute.dto.InstructionDTO;
import execute.dto.ProgramDTO;
import execute.dto.VariableDTO;
import execute.components.ProgramManager;
import execute.components.RunRecord;
import execute.components.XmlLoader;
import logic.program.Program;
import logic.variables.Var;
import logic.variables.Variable;
import logic.variables.VariableType;

import java.util.*;

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
    public String getProgramName() { return pm.getProgramName(); }

    @Override
    public void resetVars() {
        inputVarsMap.values().forEach(v -> v.setValue(0));
    }

    @Override
    public boolean validateProgram(int degree) {
        return pm.getProgram(degree).checkLabels();
    }

    @Override
    public int maxDegree() {
        return pm.maxDegree();
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
    public List<VariableDTO> getInputs() {
        return this.getVarByType().get(1);
    }

    @Override
    public int getCycles(int degree) {
        return pm.getProgram(degree).cycles();
    }

    @Override
    public boolean loadFromXML(String filePath) {
        Map<String, Variable> vars = new HashMap<>();
        Program program = XmlLoader.parse(filePath, vars, printMode);
        if (program != null) {
            this.fillOutVars(vars);
            pm.loadNewProgram(program);
            this.history.clear();
            if (printMode) System.out.println("Program '" + program.getName() + "' loaded successfully!");
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
    public List<InstructionDTO> getInstructionsList(String programName, int degree) {
        if (pm.isEmpty() || !pm.getProgramName().equals(programName)) {
            return Collections.emptyList();
        }
        return pm.getProgram(degree).getInstructions().stream()
                .map(InstructionDTO::new)
                .toList();
    }

    @Override
    public void printProgram(int degree) {
        if (pm.isEmpty()) {
            System.out.println("No program loaded.");
            return;
        }
        pm.printProgram(degree);
    }

    public void printHistory() {
        if (history.isEmpty()) {
            System.out.println("No runs recorded yet.");
            return;
        }
        for (HistoryDTO r : history) {
            System.out.printf("#%d | degree = %d | inputs = %s | y = %d | cycles = %d%n",
                    r.getNum(), r.getDegree(), r.getInputs().toString(), r.getOutput().getValue(), r.getCycles());
        }
    }

    @Override
    public long runProgram(int degree) {
        outputVar.setValue(0);
        tempVarsMap.values()
                .stream()
                .filter(Objects::nonNull)
                .forEach(v -> v.setValue(0));
        pm.runProgram(degree);
        return outputVar.getValue();
    }

    @Override
    public HistoryDTO runProgramAndRecord(int degree, List<VariableDTO> inputs) {
        VariableDTO output = new VariableDTO(VariableType.OUTPUT,0, this.runProgram(degree)) ;
        int cycles = pm.getProgramCycles(degree);

        runCounter++;
        ProgramDTO programDTO = new ProgramDTO(pm.getProgram(degree));
        HistoryDTO result = new HistoryDTO(runCounter, degree, cycles, programDTO, inputs, output);
        history.add(result);
        return result;
    }

}
