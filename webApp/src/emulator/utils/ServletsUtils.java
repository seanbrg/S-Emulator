package emulator.utils;

import jakarta.servlet.http.HttpServletResponse;
import users.UserManager;
import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.io.PrintWriter;

public class ServletsUtils {

    private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
    private static final Object userManagerLock = new Object();

    public static UserManager getUserManager(ServletContext servletContext) {
        synchronized (userManagerLock) {
            if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
            }
        }
        return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
    }

    public static void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print("Error loading program: " + message);
        }
    }
}