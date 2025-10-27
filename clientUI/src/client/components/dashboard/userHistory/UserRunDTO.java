package client.components.dashboard.userHistory;

public class UserRunDTO {
    private int runNumber;
    private String runType; // "Main" or "Auxiliary"
    private String programName;
    private String architectureType;
    private int runLevel;
    private double finalYValue;
    private int cyclesCount;

    public UserRunDTO(int runNumber, String runType, String programName, String architectureType,
                      int runLevel, double finalYValue, int cyclesCount) {
        this.runNumber = runNumber;
        this.runType = runType;
        this.programName = programName;
        this.architectureType = architectureType;
        this.runLevel = runLevel;
        this.finalYValue = finalYValue;
        this.cyclesCount = cyclesCount;
    }

    public int getRunNumber() { return runNumber; }
    public String getRunType() { return runType; }
    public String getProgramName() { return programName; }
    public String getArchitectureType() { return architectureType; }
    public int getRunLevel() { return runLevel; }
    public double getFinalYValue() { return finalYValue; }
    public int getCyclesCount() { return cyclesCount; }
}

