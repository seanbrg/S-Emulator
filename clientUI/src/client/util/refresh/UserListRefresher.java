package client.util.refresh;

import client.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import emulator.utils.WebConstants;
import javafx.beans.property.BooleanProperty;
import users.UserDashboard;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class UserListRefresher extends TimerTask {
    private final BooleanProperty autoUpdate;
    private final Consumer<String> httpStatusConsumer;
    private final Consumer<List<UserDashboard>> usersListUpdater;
    private static final Gson GSON = new Gson();

    public UserListRefresher(BooleanProperty autoUpdate,
                             Consumer<String> httpStatusConsumer,
                             Consumer<List<UserDashboard>> usersListUpdater) {
        this.autoUpdate = autoUpdate;
        this.httpStatusConsumer = httpStatusConsumer;
        this.usersListUpdater = usersListUpdater;
    }

    @Override
    public void run() {
        if (!autoUpdate.get()) {

            return;
        }

        final String url = WebConstants.USERS_URL;



        try {
            HttpUtils.getAsync(url).thenAccept(json -> {


                try {
                    if (json == null || json.trim().isEmpty()) {

                        httpStatusConsumer.accept("No users data received.");
                        usersListUpdater.accept(Collections.emptyList());
                        return;
                    }


                    Type listType = new TypeToken<List<UserDashboard>>() {
                    }.getType();
                    List<UserDashboard> usersList = GSON.fromJson(json, listType);


                    usersListUpdater.accept(usersList);


                } catch (Exception e) {

                    e.printStackTrace();
                    httpStatusConsumer.accept("Failed to parse users list: " + e.getMessage());
                    usersListUpdater.accept(Collections.emptyList());
                }
            }).exceptionally(ex -> {

                if (ex.getCause() != null) {
                    System.err.println("[UserListRefresher] Cause: " + ex.getCause().getMessage());
                }

                ex.printStackTrace();
                httpStatusConsumer.accept("Failed to update users: " + ex.getMessage());
                usersListUpdater.accept(Collections.emptyList());
                return null;
            });
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}