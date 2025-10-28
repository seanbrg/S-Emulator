package users;

public class UserDashboard {
    private final String username;
    private int mainProgramsUploaded;
    private int subfunctionsContributed;
    private int currentCredits;
    private int creditsUsed;
    private int numberOfRuns;

    public UserDashboard(String username) {
        this.username = username;
        this.mainProgramsUploaded = 0;
        this.subfunctionsContributed = 0;
        this.currentCredits = 0;
        this.creditsUsed = 0;
        this.numberOfRuns = 0;
    }

    public UserDashboard(String username, int mainProgramsUploaded, int subfunctionsContributed,
                         int currentCredits, int creditsUsed, int numberOfRuns) {
        this.username = username;
        this.mainProgramsUploaded = mainProgramsUploaded;
        this.subfunctionsContributed = subfunctionsContributed;
        this.currentCredits = currentCredits;
        this.creditsUsed = creditsUsed;
        this.numberOfRuns = numberOfRuns;
    }

    public String getUsername() { return username; }

    public int getMainProgramsUploaded() { return mainProgramsUploaded; }
    public void setMainProgramsUploaded(int val) { this.mainProgramsUploaded = val; }
    public void incrementMainProgramsUploaded() { this.mainProgramsUploaded++; }

    public int getSubfunctionsContributed() { return subfunctionsContributed; }
    public void setSubfunctionsContributed(int val) { this.subfunctionsContributed = val; }
    public void incrementSubfunctionsContributed() { this.subfunctionsContributed++; }

    public int getCurrentCredits() { return currentCredits; }
    public void setCurrentCredits(int credits) { this.currentCredits = credits; }
    public void addCredits(int credits) { this.currentCredits += credits; }

    public boolean deductCredits(int credits) {
        if (this.currentCredits >= credits) {
            this.currentCredits -= credits;
            this.creditsUsed += credits;
            return true;
        }
        return false;
    }

    public int getCreditsUsed() { return creditsUsed; }
    public void setCreditsUsed(int val) { this.creditsUsed = val; }

    public int getNumberOfRuns() { return numberOfRuns; }
    public void setNumberOfRuns(int val) { this.numberOfRuns = val; }
    public void incrementNumberOfRuns() { this.numberOfRuns++; }

}
