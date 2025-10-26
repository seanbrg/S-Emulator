package execute.dto;

/**
 * DTO for transferring program metadata to the client for display in tables
 */
public class ProgramMetadataDTO {
    private String name;
    private String uploadedBy;
    private int numberOfInstructions;
    private int maxDegree;
    private int runCount;
    private double averageCost;

    public ProgramMetadataDTO() {
    }

    public ProgramMetadataDTO(String name, String uploadedBy, int numberOfInstructions,
                              int maxDegree, int runCount, double averageCost) {
        this.name = name;
        this.uploadedBy = uploadedBy;
        this.numberOfInstructions = numberOfInstructions;
        this.maxDegree = maxDegree;
        this.runCount = runCount;
        this.averageCost = averageCost;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public int getRunCount() {
        return runCount;
    }

    public void setRunCount(int runCount) {
        this.runCount = runCount;
    }

    public double getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(double averageCost) {
        this.averageCost = averageCost;
    }

    @Override
    public String toString() {
        return "ProgramMetadataDTO{" +
                "name='" + name + '\'' +
                ", uploadedBy='" + uploadedBy + '\'' +
                ", numberOfInstructions=" + numberOfInstructions +
                ", maxDegree=" + maxDegree +
                ", runCount=" + runCount +
                ", averageCost=" + averageCost +
                '}';
    }
}
