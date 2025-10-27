package emulator.servlets;

import emulator.utils.ServletsUtils;
import com.google.gson.Gson;
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
import java.util.Set;

@WebServlet(name = "UsersListServlet", urlPatterns = {"/users"})
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


            // Convert usernames to UserDashboard objects with statistics
            List<UserDashboard> usersDataList = new ArrayList<>();
            for (String username : usersList) {
                int mainPrograms = userManager.getMainProgramsCount(username);
                int subfunctions = userManager.getSubfunctionsCount(username);
                int currentCredits = userManager.getCurrentCredits(username);
                int creditsUsed = userManager.getCreditsUsed(username);
                int numberOfRuns = userManager.getNumberOfRuns(username);

                UserDashboard user = new UserDashboard(
                        username,
                        mainPrograms,
                        subfunctions,
                        currentCredits,
                        creditsUsed,
                        numberOfRuns
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
}