package execute.dto;

import logic.program.Program;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProgramDTO {
    private String programName;
    private List<InstructionDTO> instructions;
    private Map<LabelDTO, InstructionDTO> labels;
    private int maxDegree;

    public ProgramDTO(String programName, Map<LabelDTO, InstructionDTO> labels, List<InstructionDTO> instructions) {
        this.programName = programName;
        this.instructions = instructions;
        this.labels = labels; // labels must map each label to its instruction
    }

    public ProgramDTO(Program program) {
        this.programName = program.getName();
        this.instructions = program.getInstructions().stream()
                .map(InstructionDTO::new)
                .collect(Collectors.toList());
        this.labels = program.getLabels().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> new LabelDTO(e.getKey()),
                        e -> new InstructionDTO(e.getValue())
                ));
        this.maxDegree = program.maxDegree();
    }

    public String getProgramName() { return this.programName; }
    public List<InstructionDTO> getInstructions() { return this.instructions; }
    public Map<LabelDTO, InstructionDTO> getLabels() { return this.labels; }
    public int getMaxDegree() { return this.maxDegree; }

}
