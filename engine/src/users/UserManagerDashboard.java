package users;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserManagerDashboard {
    private final Map<String, UserDashboard> users;

    public UserManagerDashboard() {
        this.users = new ConcurrentHashMap<>();
    }

    // Add a new user
    public synchronized boolean addUser(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        if (users.containsKey(username)) return false;

        users.put(username, new UserDashboard(username));
        return true;
    }

    // Remove a user
    public synchronized boolean removeUser(String username) {
        return users.remove(username) != null;
    }

    // Check if user exists
    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    public UserDashboard getUser(String username) {
        return users.get(username);
    }

    public Set<String> getUsers() {
        return users.keySet();
    }

    public int getUserCount() {
        return users.size();
    }

    // Convenience methods to access user statistics
    public int getMainProgramsCount(String username) {
        UserDashboard user = users.get(username);
        return user != null ? user.getMainProgramsUploaded() : 0;
    }

    public int getSubfunctionsCount(String username) {
        UserDashboard user = users.get(username);
        return user != null ? user.getSubfunctionsContributed() : 0;
    }

    public int getCurrentCredits(String username) {
        UserDashboard user = users.get(username);
        return user != null ? user.getCurrentCredits() : 0;
    }

    public int getCreditsUsed(String username) {
        UserDashboard user = users.get(username);
        return user != null ? user.getCreditsUsed() : 0;
    }

    public int getNumberOfRuns(String username) {
        UserDashboard user = users.get(username);
        return user != null ? user.getNumberOfRuns() : 0;
    }

    // Update methods
    public void incrementMainPrograms(String username) {
        UserDashboard user = users.get(username);
        if (user != null) user.incrementMainProgramsUploaded();
    }

    public void incrementSubfunctions(String username) {
        UserDashboard user = users.get(username);
        if (user != null) user.incrementSubfunctionsContributed();
    }

    public void addCredits(String username, int credits) {
        UserDashboard user = users.get(username);
        if (user != null) user.addCredits(credits);
    }

    public boolean deductCredits(String username, int credits) {
        UserDashboard user = users.get(username);
        return user != null && user.deductCredits(credits);
    }

    public void incrementRuns(String username) {
        UserDashboard user = users.get(username);
        if (user != null) user.incrementNumberOfRuns();
    }

    public void setCurrentCredits(String username, int credits) {
        UserDashboard user = users.get(username);
        if (user != null) user.setCurrentCredits(credits);
    }

    // Clear all users
    public synchronized void clearAll() {
        users.clear();
    }
}
