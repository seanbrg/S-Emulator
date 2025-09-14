package execute.dto;

import java.util.List;

public class HistoryDTO {
    private int num;
    private int degree;
    private int cycles;
    private ProgramDTO program;
    private List<VariableDTO> inputVariables;
    private List<VariableDTO> outputVariables;

    public HistoryDTO(int num, int degree, int cycles, ProgramDTO program, List<VariableDTO> inputVariables, List<VariableDTO> outputVariables) {
        this.num = num;
        this.degree = degree;
        this.cycles = cycles;
        this.program = program;
        this.inputVariables = inputVariables;
        this.outputVariables = outputVariables;
    }

    public int getNum() { return this.num; }
    public int getDegree() { return this.degree; }
    public int getCycles() { return this.cycles; }
    public ProgramDTO getProgram() { return this.program; }
    public List<VariableDTO> getInputs() { return this.inputVariables; }
    public List<VariableDTO> getOutputs() { return this.outputVariables; }

}
