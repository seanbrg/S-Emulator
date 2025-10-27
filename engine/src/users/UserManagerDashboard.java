package users;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserManagerDashboard {
    private final Map<String, UserData> users;

    public UserManagerDashboard() {
        this.users = new ConcurrentHashMap<>();
    }


    public synchronized boolean addUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        if (users.containsKey(username)) {
            return false;
        }

        users.put(username, new UserData(username));
        return true;
    }


    public synchronized boolean removeUser(String username) {
        return users.remove(username) != null;
    }


    public boolean userExists(String username) {
        return users.containsKey(username);
    }


    public boolean isUserExists(String username) {
        return userExists(username);
    }


    public UserData getUser(String username) {
        return users.get(username);
    }


    public Set<String> getUsers() {
        return users.keySet();
    }


    public int getUserCount() {
        return users.size();
    }


    public int getMainProgramsCount(String username) {
        UserData user = users.get(username);
        return user != null ? user.getMainProgramsUploaded() : 0;
    }

    public int getSubfunctionsCount(String username) {
        UserData user = users.get(username);
        return user != null ? user.getSubfunctionsContributed() : 0;
    }


    public int getCurrentCredits(String username) {
        UserData user = users.get(username);
        return user != null ? user.getCurrentCredits() : 0;
    }


    public int getCreditsUsed(String username) {
        UserData user = users.get(username);
        return user != null ? user.getCreditsUsed() : 0;
    }


    public int getNumberOfRuns(String username) {
        UserData user = users.get(username);
        return user != null ? user.getNumberOfRuns() : 0;
    }


    public void incrementMainPrograms(String username) {
        UserData user = users.get(username);
        if (user != null) {
            user.incrementMainProgramsUploaded();
        }
    }

    public void incrementSubfunctions(String username) {
        UserData user = users.get(username);
        if (user != null) {
            user.incrementSubfunctionsContributed();
        }
    }


    public void addCredits(String username, int credits) {
        UserData user = users.get(username);
        if (user != null) {
            user.addCredits(credits);
        }
    }

    public boolean deductCredits(String username, int credits) {
        UserData user = users.get(username);
        if (user != null) {
            return user.deductCredits(credits);
        }
        return false;
    }


    public void incrementRuns(String username) {
        UserData user = users.get(username);
        if (user != null) {
            user.incrementNumberOfRuns();
        }
    }


    public void setCurrentCredits(String username, int credits) {
        UserData user = users.get(username);
        if (user != null) {
            user.setCurrentCredits(credits);
        }
    }

    public synchronized void clearAll() {
        users.clear();
    }

    // Internal class to hold user data
    public static class UserData {
        private final String username;
        private int mainProgramsUploaded;
        private int subfunctionsContributed;
        private int currentCredits;
        private int creditsUsed;
        private int numberOfRuns;

        public UserData(String username) {
            this.username = username;
            this.mainProgramsUploaded = 0;
            this.subfunctionsContributed = 0;
            this.currentCredits = 0;
            this.creditsUsed = 0;
            this.numberOfRuns = 0;
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

}
