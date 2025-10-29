package execute.dto;

public class UserRunHistoryDTO {
    private String username;
    private int runNumber;
    private boolean isMainProgram;
    private String programName;
    private String architectureType;
    private int runLevel;
    private long outputValue;
    private int cycles;
    private long timestamp;
    private HistoryDTO historyDTO;

    public UserRunHistoryDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    public UserRunHistoryDTO(String username, int runNumber, boolean isMainProgram,
                          String programName, String architectureType,
                          int runLevel, long outputValue, int cycles) {
        this.username = username;
        this.runNumber = runNumber;
        this.isMainProgram = isMainProgram;
        this.programName = programName;
        this.architectureType = architectureType;
        this.runLevel = runLevel;
        this.outputValue = outputValue;
        this.cycles = cycles;
        this.timestamp = System.currentTimeMillis();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getRunNumber() { return runNumber; }
    public void setRunNumber(int runNumber) { this.runNumber = runNumber; }

    public boolean isMainProgram() { return isMainProgram; }
    public void setMainProgram(boolean mainProgram) { isMainProgram = mainProgram; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public String getArchitectureType() { return architectureType; }
    public void setArchitectureType(String architectureType) { this.architectureType = architectureType; }

    public int getRunLevel() { return runLevel; }
    public void setRunLevel(int runLevel) { this.runLevel = runLevel; }

    public long getOutputValue() { return outputValue; }
    public void setOutputValue(long outputValue) { this.outputValue = outputValue; }

    public int getCycles() { return cycles; }
    public void setCycles(int cycles) { this.cycles = cycles; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public HistoryDTO getHistoryDTO() { return historyDTO; }
    public void setHistoryDTO(HistoryDTO historyDTO) { this.historyDTO = historyDTO; }
}
