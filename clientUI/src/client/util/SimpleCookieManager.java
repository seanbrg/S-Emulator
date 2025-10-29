package client.util;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class SimpleCookieManager implements CookieJar {

    private final HashMap<String, List<Cookie>> cookies = new HashMap<>();
    private Consumer<String> logConsumer;

    public void setLogData(Consumer<String> logConsumer) {
        this.logConsumer = logConsumer;
    }

    @Override
    public synchronized void saveFromResponse(HttpUrl url, List<Cookie> responseCookies) {
        String host = url.host();
        List<Cookie> existing = cookies.computeIfAbsent(host, k -> new ArrayList<>());

        // merge: replace cookies with same name/domain/path, keep others
        for (Cookie newC : responseCookies) {
            Iterator<Cookie> it = existing.iterator();
            while (it.hasNext()) {
                Cookie ex = it.next();
                if (ex.name().equals(newC.name()) &&
                        ex.domain().equals(newC.domain()) &&
                        ex.path().equals(newC.path())) {
                    it.remove();
                }
            }
            existing.add(newC);
            if (logConsumer != null) {
                logConsumer.accept("Saving cookie: " + newC.name() + "=" + newC.value() + " for host " + host);
            }
        }
    }

    @Override
    public synchronized List<Cookie> loadForRequest(HttpUrl url) {
        String host = url.host();
        List<Cookie> list = cookies.get(host);
        if (list == null) return new ArrayList<>();

        // remove expired cookies
        long now = System.currentTimeMillis();
        Iterator<Cookie> it = list.iterator();
        while (it.hasNext()) {
            Cookie c = it.next();
            if (c.expiresAt() < now) {
                it.remove();
                if (logConsumer != null) logConsumer.accept("Removed expired cookie: " + c.name() + " for host " + host);
            }
        }

        // defensive copy to avoid external mutation
        List<Cookie> copy = new ArrayList<>(list);
        if (logConsumer != null) {
            copy.forEach(c -> logConsumer.accept("Loading cookie: " + c.name() + "=" + c.value() + " for host " + host));
        }
        return copy;
    }

    public synchronized void removeCookiesOf(String domain) {
        cookies.remove(domain);
    }

    // Clears all stored cookies
    public synchronized void clearAll() {
        cookies.clear();
        if (logConsumer != null) logConsumer.accept("All cookies cleared");
    }

    // Returns a defensive copy of cookies for a domain
    public synchronized List<Cookie> getCookiesForDomain(String domain) {
        List<Cookie> list = cookies.get(domain);
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    // Returns the value of a cookie by name for a domain, or null if missing
    public synchronized String getCookieValue(String domain, String name) {
        List<Cookie> list = cookies.get(domain);
        if (list == null) return null;
        long now = System.currentTimeMillis();
        for (Cookie c : list) {
            if (c.name().equals(name) && c.expiresAt() >= now) {
                return c.value();
            }
        }
        return null;
    }

    // Programmatic helper to create and save a cookie (maxAgeSeconds <= 0 => session cookie)
    public synchronized void putCookie(String domain, String name, String value, long maxAgeSeconds) {
        Cookie.Builder b = new Cookie.Builder()
                .name(name)
                .value(value)
                .domain(domain)
                .path("/");

        if (maxAgeSeconds > 0) {
            long expiresAt = System.currentTimeMillis() + maxAgeSeconds * 1000L;
            b = b.expiresAt(expiresAt);
        }
        Cookie c = b.build();

        List<Cookie> existing = cookies.computeIfAbsent(domain, k -> new ArrayList<>());
        Iterator<Cookie> it = existing.iterator();
        while (it.hasNext()) {
            Cookie ex = it.next();
            if (ex.name().equals(c.name()) && ex.domain().equals(c.domain()) && ex.path().equals(c.path())) {
                it.remove();
            }
        }
        existing.add(c);
        if (logConsumer != null) logConsumer.accept("Programmatically added cookie: " + c.name() + "=" + c.value() + " for domain " + domain);
    }

    // Returns a `Cookie` header string for manual inspection or when using alternative HTTP clients
    public synchronized String getCookieHeader(String domain) {
        List<Cookie> list = cookies.get(domain);
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        long now = System.currentTimeMillis();
        boolean first = true;
        for (Cookie c : list) {
            if (c.expiresAt() < now) continue;
            if (!first) sb.append("; ");
            sb.append(c.name()).append("=").append(c.value());
            first = false;
        }
        return sb.toString();
    }
}