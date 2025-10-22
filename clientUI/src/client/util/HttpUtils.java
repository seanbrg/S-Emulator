package client.util;

import okhttp3.*;

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

    public static void get(String url, Callback callback) {
        runAsync(url, "GET", null, callback);
    }

    public static void post(String url, RequestBody body, Callback callback) {
        runAsync(url, "POST", body, callback);
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

    public static void shutdown() {
        System.out.println("Shutting down HTTP CLIENT");
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }

}
