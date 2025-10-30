package execute.dto;

import java.util.List;

public class HistoryDTO {
    private int num;
    private int degree;
    private int maxDegree;
    private int cycles;
    private ProgramDTO program;
    private List<VariableDTO> inputVariables;
    private List<VariableDTO> outputAndTempVariables;
    private VariableDTO output;
    private String type;

    public HistoryDTO(int num, int degree, int maxDegree, int cycles, ProgramDTO program, List<VariableDTO> inputVariables, List<VariableDTO> outputVariables) {
        this.num = num;
        this.degree = degree;
        this.maxDegree = maxDegree;
        this.cycles = cycles;
        this.program = program;
        this.inputVariables = inputVariables;
        this.outputAndTempVariables = outputVariables;
        this.output = outputVariables.stream().filter(VariableDTO::isOutput).findFirst().orElse(null);
        this.type = "";
    }

    public int getNum() { return this.num; }
    public int getDegree() { return this.degree; }
    public int getMaxDegree() { return this.maxDegree; }
    public int getCycles() { return this.cycles; }
    public ProgramDTO getProgram() { return this.program; }
    public List<VariableDTO> getInputs() { return this.inputVariables; }
    public List<VariableDTO> getOutputAndTemps() { return this.outputAndTempVariables; }
    public VariableDTO getOutput() { return this.output; }
    public void setType(String type) { this.type = type; }
    public String getType() { return type; }
    public boolean isMainProgram() { return (this.type.equals("Program")); }


}
