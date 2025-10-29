package client.util;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class HttpUtils {

    // shared cookie manager used across all requests
    private static final emulator.utils.SimpleCookieManager COOKIE_JAR = new emulator.utils.SimpleCookieManager();
    private static Gson GSON = new Gson();


    // shared OkHttpClient so cookies (JSESSIONID) are persisted between calls
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .cookieJar(COOKIE_JAR)
            .build();

    public static void setCookieManagerLoggingFacility(Consumer<String> logConsumer) {
        COOKIE_JAR.setLogData(logConsumer);
    }

    public static Response getSync(String url) throws IOException {
        Request req = new Request.Builder().url(url).get().build();
        return CLIENT.newCall(req).execute(); // blocking
    }

    public static Response postSync(String url, RequestBody body) throws IOException {
        Request req = new Request.Builder().url(url).post(body).build();
        return CLIENT.newCall(req).execute(); // blocking
    }

    private static void runAsync(String url, String method, RequestBody body, Callback callback) {
        Request.Builder builder = new Request.Builder().url(url);

        // Default to GET if no executionStage or method
        if (method == null || method.isEmpty()) {
            builder.get();
        } else {
            // OkHttp automatically checks if executionStage is null when required
            builder.method(method.toUpperCase(), body);
        }

        Call call = CLIENT.newCall(builder.build());
        call.enqueue(callback);
    }

    /**
     * Wraps HttpUtils.getAsync in a CompletableFuture that yields the response executionStage as String.
     * Ensures the Response is closed and errors are propagated.
     */

    public static CompletableFuture<String> getAsync(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        CompletableFuture<String> future = new CompletableFuture<>();
        CLIENT.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { future.completeExceptionally(e); }
            @Override public void onResponse(Call call, Response response) {
                try (ResponseBody body = response.body()) {
                    if (!response.isSuccessful()) {
                        future.completeExceptionally(new IOException("HTTP " + response.code() + ": " + response.message()));
                        return;
                    }
                    future.complete(body == null ? null : body.string());
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }
        });
        return future;
    }

    public static CompletableFuture<String> postAsync(String url, RequestBody requestBody) {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        CompletableFuture<String> future = new CompletableFuture<>();
        CLIENT.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { future.completeExceptionally(e); }
            @Override public void onResponse(Call call, Response response) {
                try (ResponseBody body = response.body()) {
                    if (!response.isSuccessful()) {
                        future.completeExceptionally(new IOException("HTTP " + response.code() + ": " + response.message()));
                        return;
                    }
                    future.complete(body == null ? null : body.string());
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }
        });
        return future;
    }

    // optional helper to inspect or clear cookies from UI/tests
    public static emulator.utils.SimpleCookieManager getCookieManager() {
        return COOKIE_JAR;
    }

    public static RequestBody jsonBody(Object obj) {
        MediaType mt = MediaType.parse("application/json");
        return RequestBody.create(GSON.toJson(obj), mt);
    }


    public static void shutdown() {
        System.out.println("Shutting down HTTP CLIENT");
        CLIENT.dispatcher().executorService().shutdown();
        CLIENT.connectionPool().evictAll();
    }

    public static void showAlert(String title, String content, Scene scene) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);

            if (scene != null && scene.getWindow() != null) {
                alert.initOwner(scene.getWindow());
                alert.getDialogPane().getStylesheets().addAll(scene.getStylesheets());
            }

            try {
                Stage dlg = (Stage) alert.getDialogPane().getScene().getWindow();
                dlg.getIcons().add(new Image(Objects.requireNonNull(HttpUtils.class.getResourceAsStream("/client/resources/images/icon.png"))));
            } catch (Exception ignored) { }

            alert.showAndWait();
        });
    }
}