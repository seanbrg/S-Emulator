package execute;

import execute.dto.VariableDTO;
import execute.managers.LabelGenerator;
import execute.managers.ProgramManager;
import execute.managers.RunRecord;
import execute.managers.XmlLoader;
import logic.program.Program;
import logic.variables.Var;
import logic.variables.Variable;
import logic.variables.VariableType;

import java.util.*;
import java.util.stream.Collectors;

public class EngineImpl implements Engine {
    private Map<String, Variable> currentVars;
    private LabelGenerator labelGenerator;
    private ProgramManager pm;
    private final List<RunRecord> history = new ArrayList<>();
    private int runCounter = 0;

    public EngineImpl() {
        this.labelGenerator = new LabelGenerator();
        this.pm = new ProgramManager(this.labelGenerator);

    }

    public boolean isLoaded() {
        return !pm.isEmpty();
    }

    @Override
    public void resetVars() {
        currentVars.values().forEach(v -> v.setValue(0)); // reset vars
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

        List<VariableDTO> xList = new ArrayList<>(currentVars.values()) // snapshot
                .stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getType() == VariableType.INPUT)
                .sorted(Comparator.comparing(Variable::getName))
                .map(VariableDTO::new)
                .toList();


        List<VariableDTO> yList = List.of(currentVars.values().stream()
                .filter(Objects::nonNull)
                .filter(var -> var.getType().equals(VariableType.OUTPUT))
                .map(VariableDTO::new)
                .findFirst()
                .orElse(new VariableDTO(VariableType.OUTPUT, 0, 0)));

        List<VariableDTO> zList = new ArrayList<>(currentVars.values())
                .stream()
                .filter(Objects::nonNull)
                .filter(var -> var.getType().equals(VariableType.TEMP))
                .sorted(Comparator.comparing(Variable::getName))
                .map(VariableDTO::new)
                .toList();

        return List.of(xList, yList, zList);
    }

    @Override
    public List<VariableDTO> getInputs() {
        return this.getVarByType().getFirst();
    }

    @Override
    public int getCycles(int degree) {
        return pm.getProgram(degree).cycles();
    }

    @Override
    public boolean loadFromXML(String filePath) {
        labelGenerator.clear();
        Map<String, Variable> vars = new HashMap<>();
        Program program = XmlLoader.parse(filePath, vars);
        if (program != null) {
            pm.loadNewProgram(program);
            this.currentVars = vars;
            this.history.clear();
            labelGenerator.loadInstructionLabels(program.getInstructions());
            System.out.println("Program '" + program.getName() + "' loaded successfully!");
            return true;
        } else {
            System.out.println("Program not loaded. Keeping previous program.");
            return false;
        }
    }

    @Override
    public void loadInputs(List<VariableDTO> inputVars) {
        for (VariableDTO variableDTO : inputVars) {
            if (currentVars.containsKey(variableDTO.getName())) {
                currentVars.get(variableDTO.getName()).setValue(variableDTO.getValue());
            }
            else {
                currentVars.put(variableDTO.getName(), new Var(variableDTO));
            }
        }
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
        for (RunRecord r : history) {
            System.out.printf("#%d | degree = %d | inputs = %s | y = %d | cycles = %d%n",
                    r.getRunId(), r.getDegree(), r.getInputs(), r.getResultY(), r.getCycles());
        }
    }

    @Override
    public long runProgram(int degree) {
        pm.runProgram(degree);
        return currentVars.get("y").getValue();
    }

    @Override
    public long runProgramAndRecord(int degree, List<Long> inputs) {
        long result = this.runProgram(degree);
        int cycles = pm.getProgramCycles(degree);

        runCounter++;
        history.add(new RunRecord(runCounter, degree, inputs, result, cycles));

        return result;
    }

}
