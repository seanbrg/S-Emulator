package emulator.servlets;

import emulator.utils.ContextUtils;
import emulator.utils.ServletsUtils;
import com.google.gson.Gson;
import emulator.utils.WebConstants;
import users.UserDashboard;
import users.UserManagerDashboard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet(name = "UsersListServlet", urlPatterns = {WebConstants.USERS_PATH})
public class UsersListServlet extends HttpServlet {

    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            UserManagerDashboard userManager = ServletsUtils.getUserManager(getServletContext());

            if (userManager == null) {
                System.err.println("[UsersListServlet] ERROR: UserManager is NULL!");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println(GSON.toJson(new ArrayList<>()));
                return;
            }

            Set<String> usersList = userManager.getUsers();
            Map<String, Integer> userRunCounts = ContextUtils.getUserRunCounts(getServletContext());

            // Convert usernames to UserDashboard objects with statistics
            List<UserDashboard> usersDataList = new ArrayList<>();
            for (String username : usersList) {
                int runs = userRunCounts.getOrDefault(username, 0);

                UserDashboard user = new UserDashboard(
                        username,
                        userManager.getMainProgramsCount(username),
                        userManager.getSubfunctionsCount(username),
                        userManager.getCurrentCredits(username),
                        userManager.getCreditsUsed(username),
                        runs
                );

                usersDataList.add(user);
            }

            String json = GSON.toJson(usersDataList);
            out.println(json);
            out.flush();

        } catch (Exception e) {
            System.err.println("[UsersListServlet] EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.println(GSON.toJson(new ArrayList<>()));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String creditsStr = request.getParameter("credits");

        try (PrintWriter out = response.getWriter()) {

            if (username == null || creditsStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println(GSON.toJson("Missing username or credits parameter"));
                return;
            }

            int credits;
            try {
                credits = Integer.parseInt(creditsStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println(GSON.toJson("Invalid credits value"));
                return;
            }

            UserManagerDashboard userManager = ServletsUtils.getUserManager(getServletContext());
            if (userManager == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println(GSON.toJson("UserManager is not available"));
                return;
            }

            if (!userManager.userExists(username)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println(GSON.toJson("User not found"));
                return;
            }

            // Add credits to the user, add to used credits if negative
            userManager.addCredits(username, credits);
            if (credits < 0) {
                userManager.addCreditsUsed(username, -credits);
            }

            // Return updated credits
            int newCredits = userManager.getCurrentCredits(username);
            out.println(GSON.toJson(newCredits));

        } catch (Exception e) {
            System.err.println("[UsersListServlet] EXCEPTION in POST: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.println(GSON.toJson("Error updating credits"));
            }
        }
    }
}