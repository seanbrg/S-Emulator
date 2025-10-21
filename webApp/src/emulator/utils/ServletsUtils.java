package emulator.utils;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class ServletsUtils {
    public static void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print("Error loading program: " + message);
        }
    }
}
