package emulator.servlets;

import emulator.utils.ServletsUtils;
import emulator.utils.SessionUtils;
import emulator.utils.WebConstants;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.UserManagerDashboard;

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
        UserManagerDashboard userManager = ServletsUtils.getUserManager(getServletContext());



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
                    // FIXED: Changed from isUserExists to userExists
                    if (userManager.userExists(usernameFromParameter)) {

                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().println("Username " + usernameFromParameter +
                                " already exists. Please enter a different username.");
                    } else {
                        // Add the new user to the users list
                        boolean added = userManager.addUser(usernameFromParameter);

                        if (added) {
                            // Set initial credits for new user
                            userManager.setCurrentCredits(usernameFromParameter, 50);



                            // Set the username in a session
                            request.getSession(true).setAttribute(WebConstants.USERNAME, usernameFromParameter);


                            response.setStatus(HttpServletResponse.SC_OK);
                        } else {
                            System.err.println("[LoginServlet] ERROR: Failed to add user");
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            response.getWriter().println("Failed to create user");
                        }
                    }
                }
            }
        } else {
            // User is already logged in

            response.setStatus(HttpServletResponse.SC_OK);
        }
    }
}