package emulator.servlets;

import emulator.utils.ServletsUtils;
import emulator.utils.SessionUtils;
import emulator.utils.WebConstants;
import users.UserManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {WebConstants.LOGIN_PATH})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        String usernameFromSession = SessionUtils.getUsername(request);
        UserManager userManager = ServletsUtils.getUserManager(getServletContext());

        if (usernameFromSession == null) {
            // User is not logged in yet
            String usernameFromParameter = request.getParameter(WebConstants.USERNAME);

            if (usernameFromParameter == null || usernameFromParameter.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Username cannot be empty");
            } else {
                // Normalize the username value
                usernameFromParameter = usernameFromParameter.trim();

                synchronized (this) {
                    if (userManager.isUserExists(usernameFromParameter)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().println("Username " + usernameFromParameter +
                                " already exists. Please enter a different username.");
                    } else {
                        // Add the new user to the users list
                        userManager.addUser(usernameFromParameter);

                        // Set the username in a session
                        request.getSession(true).setAttribute(WebConstants.USERNAME, usernameFromParameter);

                        System.out.println("User logged in successfully: " + usernameFromParameter);
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                }
            }
        } else {
            // User is already logged in
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }
}
