package emulator.servlets;

import client.components.dashboard.userHistory.UserRunDTO;
import com.google.gson.Gson;
import emulator.utils.WebConstants;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.UserManagerDashboard;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


@WebServlet(name = "UserHistoryServlet", urlPatterns = {"/userhistory"})
public class UserHistoryServlet extends HttpServlet {

    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        try {

            String username = (String) request.getSession().getAttribute(WebConstants.USERNAME);
            if (username == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().println("{\"error\": \"User not logged in.\"}");
                return;
            }


            UserManagerDashboard userManager = (UserManagerDashboard) getServletContext().getAttribute("userManager");
            if (userManager == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("{\"error\": \"User manager not initialized.\"}");
                return;
            }


            UserManagerDashboard.UserData userDashboard = userManager.getUser(username);
            if (userDashboard == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().println("{\"error\": \"User not found.\"}");
                return;
            }


            List<UserRunDTO> userRuns = getUserRunHistory(userDashboard);


            String json = GSON.toJson(userRuns);
            PrintWriter out = response.getWriter();
            out.println(json);
            out.flush();
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("{\"error\": \"Server error: " + e.getMessage() + "\"}");
        }
    }


    private List<UserRunDTO> getUserRunHistory(UserManagerDashboard.UserData userDashboard) {
        List<UserRunDTO> runs = new ArrayList<>();

        return runs;
    }



}
