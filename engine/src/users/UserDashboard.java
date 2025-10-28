package users;

public class UserDashboard {
    private String username;
    private int mainProgramsUploaded;
    private int subfunctionsContributed;
    private int currentCredits;
    private int creditsUsed;
    private int numberOfRuns;

    // Constructor for Gson
    public UserDashboard() {
        this.username = "";
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

    // Getters
    public String getUsername() {
        return username;
    }

    public int getMainProgramsUploaded() {
        return mainProgramsUploaded;
    }

    public int getSubfunctionsContributed() {
        return subfunctionsContributed;
    }

    public int getCurrentCredits() {
        return currentCredits;
    }

    public int getCreditsUsed() {
        return creditsUsed;
    }

    public int getNumberOfRuns() {
        return numberOfRuns;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setMainProgramsUploaded(int mainProgramsUploaded) {
        this.mainProgramsUploaded = mainProgramsUploaded;
    }

    public void setSubfunctionsContributed(int subfunctionsContributed) {
        this.subfunctionsContributed = subfunctionsContributed;
    }

    public void setCurrentCredits(int currentCredits) {
        this.currentCredits = currentCredits;
    }

    public void setCreditsUsed(int creditsUsed) {
        this.creditsUsed = creditsUsed;
    }

    public void setNumberOfRuns(int numberOfRuns) {
        this.numberOfRuns = numberOfRuns;
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