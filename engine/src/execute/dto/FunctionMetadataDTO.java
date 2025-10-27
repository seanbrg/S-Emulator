package execute.dto;

/**
 * DTO for transferring program metadata to the client for display in tables
 */
public class FunctionMetadataDTO {
    private String name;
    private String program;
    private String uploadedBy;
    private int numberOfInstructions;
    private int maxDegree;

    public FunctionMetadataDTO() {
    }

    public FunctionMetadataDTO(String name, String program, String uploadedBy,
                               int numberOfInstructions, int maxDegree) {
        this.name = name;
        this.program = program;
        this.uploadedBy = uploadedBy;
        this.numberOfInstructions = numberOfInstructions;
        this.maxDegree = maxDegree;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProgram() { return program; }

    public void setProgram(String program) { this.program = program; }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public int getNumberOfInstructions() {
        return numberOfInstructions;
    }

    public void setNumberOfInstructions(int numberOfInstructions) {
        this.numberOfInstructions = numberOfInstructions;
    }

    public int getMaxDegree() {
        return maxDegree;
    }

    public void setMaxDegree(int maxDegree) {
        this.maxDegree = maxDegree;
    }

    @Override
    public String toString() {
        return "ProgramMetadataDTO{" +
                "name='" + name + '\'' +
                ", program='" + program + '\'' +
                ", uploadedBy='" + uploadedBy + '\'' +
                ", numberOfInstructions=" + numberOfInstructions +
                ", maxDegree=" + maxDegree +
                '}';
    }
}
