package client.components.dashboard.dashboardHeader;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.*;
import java.io.File;
import java.io.IOException;

public class DashboardHeaderController {

    @FXML
    private Button loadFileButton;

    @FXML
    private Label filePath;

    // Adjust this to match your servlet base URL
    private static final String PROGRAMS_SERVLET_URL = "http://localhost:8080/semulator/programs";

    @FXML
    public void initialize() {
        loadFileButton.setOnAction(event -> onLoadFileClicked());
    }

    private void onLoadFileClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Program File");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );

        Stage stage = (Stage) loadFileButton.getScene().getWindow(); //Finds the current window
        File selectedFile = fileChooser.showOpenDialog(stage); //Opens the file chooser dialog

        if (selectedFile != null) {
            filePath.setText(selectedFile.getAbsolutePath());//Displays its full path in the label
            uploadProgramFile(selectedFile);
        } else {
            filePath.setText("No file selected");
        }
    }

    private void uploadProgramFile(File file) {
        OkHttpClient client = new OkHttpClient();

        //Wraps the file’s contents into a RequestBody object so OkHttp can send it in a POST request
        //"application/octet-stream" means “raw binary data”.
        RequestBody requestBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));

        //Builds an HTTP POST request to your servlet, attaching the file data as the body.
        Request request = new Request.Builder()
                .url(PROGRAMS_SERVLET_URL)
                .post(requestBody)
                .build();

        //Sends the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Upload failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    System.out.println("File uploaded successfully!");
                } else {
                    System.out.println("Upload failed. Server responded with code: " + response.code());
                }
            }
        });
    }
}
