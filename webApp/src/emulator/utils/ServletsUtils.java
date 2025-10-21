package emulator.utils;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class ServletsUtils {

    /**
     * Sends an error message to the client with the given HTTP status code and message.
     * Sets the response content type to text/plain and closes the writer.
     *
     * @param response the HttpServletResponse to send through
     * @param status   HTTP status code (e.g., 400, 404, 500)
     * @param message  error description to send in the response body
     */
    public static void sendError(HttpServletResponse response, int status, String message) {
        response.setStatus(status);
        response.setContentType("text/plain; charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.write(message);
            out.flush();
        } catch (IOException e) {
            // If writing the response fails, log to stderr as last resort
            System.err.println("Failed to send error response: " + e.getMessage());
        }
    }
}
