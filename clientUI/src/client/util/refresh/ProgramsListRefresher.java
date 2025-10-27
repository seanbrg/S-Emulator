package client.util.refresh;

import client.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import emulator.utils.WebConstants;
import execute.dto.ProgramMetadataDTO;
import javafx.beans.property.BooleanProperty;

import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class ProgramsListRefresher extends TimerTask {
    private final BooleanProperty autoUpdate;
    private final Consumer<String> httpStatusConsumer;
    private final Consumer<List<ProgramMetadataDTO>> programsListUpdater;
    private static final Gson GSON = new Gson();

    public ProgramsListRefresher(BooleanProperty autoUpdate,
                                 Consumer<String> httpStatusConsumer,
                                 Consumer<List<ProgramMetadataDTO>> programsListUpdater) {
        this.autoUpdate = autoUpdate;
        this.httpStatusConsumer = httpStatusConsumer;
        this.programsListUpdater = programsListUpdater;
    }

    @Override
    public void run() {
        if (autoUpdate.get()) {
            httpStatusConsumer.accept("Updating programs...");

            HttpUtils.getAsync(WebConstants.PROGRAMS_METADATA_URL).thenAccept(json -> {
                // Parse JSON array of programMetadataDTO
                try {
                    Type listType = new TypeToken<List<ProgramMetadataDTO>>() {
                    }.getType();
                    List<ProgramMetadataDTO> programs = GSON.fromJson(json, listType);

                    programsListUpdater.accept(programs);
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