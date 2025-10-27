package users;

public class UserDashboard {
    private final String username;
    private int mainProgramsUploaded;
    private int subfunctionsContributed;
    private int currentCredits;
    private int creditsUsed;
    private int numberOfRuns;

    // Constructor for basic user (used when only username is known)
    public UserDashboard(String username) {
        this.username = username;
        this.mainProgramsUploaded = 0;
        this.subfunctionsContributed = 0;
        this.currentCredits = 0;
        this.creditsUsed = 0;
        this.numberOfRuns = 0;
    }

    // Constructor with all fields (used when deserializing from JSON)
    public UserDashboard(String username, int mainProgramsUploaded, int subfunctionsContributed,
                int currentCredits, int creditsUsed, int numberOfRuns) {
        this.username = username;
        this.mainProgramsUploaded = mainProgramsUploaded;
        this.subfunctionsContributed = subfunctionsContributed;
        this.currentCredits = currentCredits;
        this.creditsUsed = creditsUsed;
        this.numberOfRuns = numberOfRuns;
    }

    public String getUsername() {
        return username;
    }

    public int getMainProgramsUploaded() {
        return mainProgramsUploaded;
    }

    public void setMainProgramsUploaded(int mainProgramsUploaded) {
        this.mainProgramsUploaded = mainProgramsUploaded;
    }

    public void incrementMainProgramsUploaded() {
        this.mainProgramsUploaded++;
    }

    public int getSubfunctionsContributed() {
        return subfunctionsContributed;
    }

    public void setSubfunctionsContributed(int subfunctionsContributed) {
        this.subfunctionsContributed = subfunctionsContributed;
    }

    public void incrementSubfunctionsContributed() {
        this.subfunctionsContributed++;
    }

    public int getCurrentCredits() {
        return currentCredits;
    }

    public void setCurrentCredits(int currentCredits) {
        this.currentCredits = currentCredits;
    }

    public void addCredits(int credits) {
        this.currentCredits += credits;
    }

    public boolean deductCredits(int credits) {
        if (this.currentCredits >= credits) {
            this.currentCredits -= credits;
            this.creditsUsed += credits;
            return true;
        }
        return false;
    }

    public int getCreditsUsed() {
        return creditsUsed;
    }

    public void setCreditsUsed(int creditsUsed) {
        this.creditsUsed = creditsUsed;
    }

    public int getNumberOfRuns() {
        return numberOfRuns;
    }

    public void setNumberOfRuns(int numberOfRuns) {
        this.numberOfRuns = numberOfRuns;
    }

    public void incrementNumberOfRuns() {
        this.numberOfRuns++;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", mainPrograms=" + mainProgramsUploaded +
                ", subfunctions=" + subfunctionsContributed +
                ", currentCredits=" + currentCredits +
                ", creditsUsed=" + creditsUsed +
                ", numberOfRuns=" + numberOfRuns +
                '}';
    }

}
