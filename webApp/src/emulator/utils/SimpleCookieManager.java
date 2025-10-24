package emulator.utils;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class SimpleCookieManager implements CookieJar {

    private final HashMap<String, List<Cookie>> cookies = new HashMap<>();
    private Consumer<String> logConsumer;

    public void setLogData(Consumer<String> logConsumer) {
        this.logConsumer = logConsumer;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        String host = url.host();
        this.cookies.put(host, cookies);

        if (logConsumer != null) {
            cookies.forEach(cookie ->
                    logConsumer.accept("Saving cookie: " + cookie.name() + "=" + cookie.value()));
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        String host = url.host();
        List<Cookie> cookiesForHost = cookies.get(host);

        if (cookiesForHost != null && logConsumer != null) {
            cookiesForHost.forEach(cookie ->
                    logConsumer.accept("Loading cookie: " + cookie.name() + "=" + cookie.value()));
        }

        return cookiesForHost != null ? cookiesForHost : new ArrayList<>();
    }

    public void removeCookiesOf(String domain) {
        cookies.remove(domain);
    }
}
