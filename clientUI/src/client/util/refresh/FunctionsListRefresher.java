package client.util.refresh;

import client.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import emulator.utils.WebConstants;
import execute.dto.FunctionMetadataDTO;
import execute.dto.ProgramMetadataDTO;
import javafx.beans.property.BooleanProperty;

import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class FunctionsListRefresher extends TimerTask {
    private final BooleanProperty autoUpdate;
    private final Consumer<String> httpStatusConsumer;
    private final Consumer<List<FunctionMetadataDTO>> functionsListUpdater;
    private static final Gson GSON = new Gson();

    public FunctionsListRefresher(BooleanProperty autoUpdate,
                                 Consumer<String> httpStatusConsumer,
                                 Consumer<List<FunctionMetadataDTO>> functionsListUpdater) {
        this.autoUpdate = autoUpdate;
        this.httpStatusConsumer = httpStatusConsumer;
        this.functionsListUpdater = functionsListUpdater;
    }

    @Override
    public void run() {
        if (autoUpdate.get()) {
            httpStatusConsumer.accept("Updating programs...");

            HttpUtils.getAsync(WebConstants.PROGRAMS_FUNC_METADATA_URL).thenAccept(json -> {
                // Parse JSON array of FunctionMetadataDTO
                try {
                    Type listType = new TypeToken<List<FunctionMetadataDTO>>() {}.getType();
                    List<FunctionMetadataDTO> functions = GSON.fromJson(json, listType);

                    functionsListUpdater.accept(functions);
                    httpStatusConsumer.accept("Programs updated.");

                } catch (Exception e) {
                    httpStatusConsumer.accept("Failed to parse programs list: " + e.getMessage());
                    return;
                }
            }).exceptionally(ex -> {
                httpStatusConsumer.accept("Failed to update programs: " + ex.getCause().getMessage());
                return null;
            });
        }
    }
}
