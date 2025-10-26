package execute.dto;

import logic.program.Program;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DTO for transferring program structure (instructions, labels) to the client.
 * This is used when the client needs the actual program code.
 *
 * For program metadata (name, owner, statistics), use ProgramMetadataDTO instead.
 */
public class ProgramDTO {
    private String programName;
    private List<InstructionDTO> instructions;
    private Map<String, InstructionDTO> labels;
    private int maxDegree;

    public ProgramDTO(String programName, Map<String, InstructionDTO> labels, List<InstructionDTO> instructions) {
        this.programName = programName;
        this.instructions = instructions;
        this.labels = labels; // labels must map each label to its instruction
    }

    /**
     * Constructor that creates DTO from Program object
     * This is the constructor your existing code uses
     */
    public ProgramDTO(Program program) {
        this.programName = program.getName();
        this.instructions = program.getInstructions().stream()
                .map(InstructionDTO::new)
                .collect(Collectors.toList());
        this.labels = program.getLabels().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getLabel(),
                        e -> new InstructionDTO(e.getValue())
                ));
        this.maxDegree = program.maxDegree();
    }

    public String getProgramName() { return this.programName; }
    public List<InstructionDTO> getInstructions() { return this.instructions; }
    public Map<String, InstructionDTO> getLabels() { return this.labels; }
    public int getMaxDegree() { return this.maxDegree; }
}