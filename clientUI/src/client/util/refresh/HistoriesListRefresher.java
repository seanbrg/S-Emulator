package client.util.refresh;

import client.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import emulator.utils.WebConstants;
import execute.dto.FunctionMetadataDTO;
import execute.dto.UserRunHistoryDTO;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class HistoriesListRefresher extends TimerTask {
    private StringProperty userFilterProperty;
    private final BooleanProperty autoUpdate;
    private final Consumer<String> httpStatusConsumer;
    private final Consumer<List<UserRunHistoryDTO>> historyListUpdater;

    private static final Gson GSON = new Gson();

    public HistoriesListRefresher(StringProperty userFilterProperty,
                                  BooleanProperty autoUpdate,
                                  Consumer<String> httpStatusConsumer,
                                  Consumer<List<UserRunHistoryDTO>> historyListUpdater) {
        this.userFilterProperty = userFilterProperty;
        this.autoUpdate = autoUpdate;
        this.httpStatusConsumer = httpStatusConsumer;
        this.historyListUpdater = historyListUpdater;
    }

    @Override
    public void run() {
        if (autoUpdate.get()) {
            httpStatusConsumer.accept("Updating histories...");

            String userName = URLEncoder.encode(userFilterProperty.getValue(), StandardCharsets.UTF_8);
            String historiesUrl = WebConstants.USERS_HISTORY_URL + "?" + WebConstants.USERNAME + "=" + userName;

            HttpUtils.getAsync(historiesUrl).thenAccept(json -> {
                // Parse JSON array of UserRunHistoryDTO
                try {
                    Type listType = new TypeToken<List<UserRunHistoryDTO>>() {}.getType();
                    List<UserRunHistoryDTO> histories = GSON.fromJson(json, listType);

                    historyListUpdater.accept(histories);
                    httpStatusConsumer.accept("Histories updated.");

                } catch (Exception e) {
                    httpStatusConsumer.accept("Failed to parse histories list: " + e.getMessage());
                    return;
                }
            }).exceptionally(ex -> {
                httpStatusConsumer.accept("Failed to update histories: " + ex.getCause().getMessage());
                return null;
            });
        }
    }
}