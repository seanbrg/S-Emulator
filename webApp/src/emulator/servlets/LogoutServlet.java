package emulator.servlets;

import emulator.utils.ServletsUtils;
import emulator.utils.SessionUtils;
import emulator.utils.WebConstants;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.UserManagerDashboard;

import java.io.IOException;

@WebServlet(name = "LogoutServlet", urlPatterns = {WebConstants.LOGOUT_PATH})
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String usernameFromSession = SessionUtils.getUsername(request);
        UserManagerDashboard userManager = ServletsUtils.getUserManager(getServletContext());

        if (usernameFromSession != null) {

            userManager.removeUser(usernameFromSession);
            SessionUtils.clearSession(request);
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }
}