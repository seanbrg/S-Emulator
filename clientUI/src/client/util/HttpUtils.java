package client.util;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class HttpUtils {

    private final static client.util.SimpleCookieManager simpleCookieManager = new client.util.SimpleCookieManager();
    private final static OkHttpClient HTTP_CLIENT =
            new OkHttpClient.Builder()
                    .cookieJar(simpleCookieManager)
                    .followRedirects(false)
                    .build();

    public static void setCookieManagerLoggingFacility(Consumer<String> logConsumer) {
        simpleCookieManager.setLogData(logConsumer);
    }

    public static void removeCookiesOf(String domain) {
        simpleCookieManager.removeCookiesOf(domain);
    }

    public static void getAsync(String url, Callback callback) {
        runAsync(url, "GET", null, callback);
    }

    public static void postAsync(String url, RequestBody body, Callback callback) {
        runAsync(url, "POST", body, callback);
    }

    public static Response getSync(String url) throws IOException {
        Request req = new Request.Builder().url(url).get().build();
        return HTTP_CLIENT.newCall(req).execute(); // blocking
    }

    public static Response postSync(String url, RequestBody body) throws IOException {
        Request req = new Request.Builder().url(url).post(body).build();
        return HTTP_CLIENT.newCall(req).execute(); // blocking
    }

    private static void runAsync(String url, String method, RequestBody body, Callback callback) {
        Request.Builder builder = new Request.Builder().url(url);

        // Default to GET if no body or method
        if (method == null || method.isEmpty()) {
            builder.get();
        } else {
            // OkHttp automatically checks if body is null when required
            builder.method(method.toUpperCase(), body);
        }

        Call call = HTTP_CLIENT.newCall(builder.build());
        call.enqueue(callback);
    }

    /**
     * Wraps HttpUtils.getAsync in a CompletableFuture that yields the response body as String.
     * Ensures the Response is closed and errors are propagated.
     */
    public static CompletableFuture<String> getAsyncBody(String url) {
        CompletableFuture<String> fut = new CompletableFuture<>();
        HttpUtils.getAsync(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                fut.completeExceptionally(e);
            }
            @Override
            public void onResponse(Call call, Response response) {
                try (response) {
                    if (!response.isSuccessful()) {
                        String body = response.body() != null ? response.body().string() : "<no body>";
                        fut.completeExceptionally(new IOException("HTTP " + response.code() + " for " + url + " body=" + body));
                        return;
                    }
                    if (response.body() == null) {
                        fut.completeExceptionally(new IOException("Empty body for " + url));
                        return;
                    }
                    fut.complete(response.body().string());
                } catch (Exception ex) {
                    fut.completeExceptionally(ex);
                }
            }
        });
        return fut;
    }

    public static void shutdown() {
        System.out.println("Shutting down HTTP CLIENT");
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }

}
