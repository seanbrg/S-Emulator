package client.components.dashboard.userHistory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class UserHistoryController {

    @FXML private TableView<UserRunDTO> runsTable;
    @FXML private TableColumn<UserRunDTO, Integer> runNumberColumn;
    @FXML private TableColumn<UserRunDTO, String> runTypeColumn;
    @FXML private TableColumn<UserRunDTO, String> programNameColumn;
    @FXML private TableColumn<UserRunDTO, String> architectureTypeColumn;
    @FXML private TableColumn<UserRunDTO, Integer> runLevelColumn;
    @FXML private TableColumn<UserRunDTO, Double> finalYValueColumn;
    @FXML private TableColumn<UserRunDTO, Integer> cyclesCountColumn;

    private final ObservableList<UserRunDTO> runDataList = FXCollections.observableArrayList();
    private final OkHttpClient httpClient = new OkHttpClient();
    private final Gson gson = new Gson();

    @FXML
    public void initialize() {
        runNumberColumn.setCellValueFactory(new PropertyValueFactory<>("runNumber"));
        runTypeColumn.setCellValueFactory(new PropertyValueFactory<>("runType"));
        programNameColumn.setCellValueFactory(new PropertyValueFactory<>("programName"));
        architectureTypeColumn.setCellValueFactory(new PropertyValueFactory<>("architectureType"));
        runLevelColumn.setCellValueFactory(new PropertyValueFactory<>("runLevel"));
        finalYValueColumn.setCellValueFactory(new PropertyValueFactory<>("finalYValue"));
        cyclesCountColumn.setCellValueFactory(new PropertyValueFactory<>("cyclesCount"));

        runsTable.setItems(runDataList);

        loadRunsFromServer();
    }

    private void loadRunsFromServer() {
        String url = "http://localhost:8080/emulator/userhistory"; // Adjust if needed

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showError("Failed to load runs: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Platform.runLater(() -> showError("Server returned error: " + response.code()));
                    return;
                }

                String json = response.body().string();
                Type listType = new TypeToken<List<UserRunDTO>>() {}.getType();
                List<UserRunDTO> runs = gson.fromJson(json, listType);

                Platform.runLater(() -> {
                    runDataList.setAll(runs);
                });
            }
        });
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

