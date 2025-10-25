package client.util;

import com.google.gson.Gson;

public class Constants {
    public static final String LOGIN_FXML = "/client/components/login/login.fxml";
    public static final String DASHBOARD_FXML = "/client/components/dashboard/dashboardStage/dashboardStage.fxml";
    public static final int REFRESH_RATE = 500; // milliseconds
    public static final Gson GSON_INSTANCE = new Gson();
    public static final int ARCH_COST_I = 5;
    public static final int ARCH_COST_II = 100;
    public static final int ARCH_COST_III = 500;
    public static final int ARCH_COST_IV = 1000;

    // Replace with your actual server URL and port
    public static final String BASE_URL = "http://localhost:8080/semulator";

    // API endpoint for available users
    public static final String AVAILABLE_USERS_URL = BASE_URL + "/available-users";
}
