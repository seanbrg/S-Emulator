package client.util.refresh;

import client.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import emulator.utils.WebConstants;
import execute.dto.FunctionMetadataDTO;
import execute.dto.ProgramMetadataDTO;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class FunctionsListRefresher extends TimerTask {
    private StringProperty programFilterProperty;
    private final BooleanProperty autoUpdate;
    private final Consumer<String> httpStatusConsumer;
    private final Consumer<List<FunctionMetadataDTO>> functionsListUpdater;
    private static final Gson GSON = new Gson();

    public FunctionsListRefresher(StringProperty programFilterProperty,
                                  BooleanProperty autoUpdate,
                                  Consumer<String> httpStatusConsumer,
                                  Consumer<List<FunctionMetadataDTO>> functionsListUpdater) {
        this.programFilterProperty = programFilterProperty;
        this.autoUpdate = autoUpdate;
        this.httpStatusConsumer = httpStatusConsumer;
        this.functionsListUpdater = functionsListUpdater;
    }

    @Override
    public void run() {
        if (autoUpdate.get()) {
            httpStatusConsumer.accept("Updating functions...");

            String functionsUrl = WebConstants.PROGRAMS_FUNC_METADATA_URL + "?program=" + programFilterProperty.get();

            HttpUtils.getAsync(functionsUrl).thenAccept(json -> {
                // Parse JSON array of FunctionMetadataDTO
                try {
                    Type listType = new TypeToken<List<FunctionMetadataDTO>>() {}.getType();
                    List<FunctionMetadataDTO> functions = GSON.fromJson(json, listType);

                    functionsListUpdater.accept(functions);
                    httpStatusConsumer.accept("Functions updated.");

                } catch (Exception e) {
                    httpStatusConsumer.accept("Failed to parse functions list: " + e.getMessage());
                    return;
                }
            }).exceptionally(ex -> {
                httpStatusConsumer.accept("Failed to update functions: " + ex.getCause().getMessage());
                return null;
            });
        }
    }

    public void setProgramFilterProperty(String programFilterProperty) {
        this.programFilterProperty.set(programFilterProperty);
    }
}
