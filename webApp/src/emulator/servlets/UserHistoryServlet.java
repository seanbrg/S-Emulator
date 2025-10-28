package emulator.servlets;

import client.components.dashboard.userHistory.UserRunHistory;
import com.google.gson.Gson;
import emulator.utils.ServletsUtils;
import emulator.utils.WebConstants;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static emulator.utils.ServletsUtils.sendError;

@WebServlet(name = "UserHistoryServlet", urlPatterns = {"/userhistory"})
public class UserHistoryServlet extends HttpServlet {

    private static final Gson GSON = new Gson();

    @SuppressWarnings("unchecked")
    private Map<String, List<UserRunHistory>> getOrInitUserHistoryMap() {
        Map<String, List<UserRunHistory>> map =
                (Map<String, List<UserRunHistory>>) getServletContext().getAttribute("userHistoryMap");

        if (map == null) {
            map = new ConcurrentHashMap<>();
            getServletContext().setAttribute("userHistoryMap", map);

        }
        return map;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        String targetUsername = request.getParameter("username");

        HttpSession session = request.getSession(false);
        String currentUser = session != null ? (String) session.getAttribute("username") : null;

        if (targetUsername == null || targetUsername.trim().isEmpty()) {
            targetUsername = currentUser;
        }

        if (targetUsername == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "No username specified and no user logged in");
            return;
        }

        Map<String, List<UserRunHistory>> userHistoryMap = getOrInitUserHistoryMap();

        List<UserRunHistory> history;
        synchronized (userHistoryMap) {
            history = userHistoryMap.getOrDefault(targetUsername, new ArrayList<>());
        }


        try (PrintWriter out = response.getWriter()) {
            out.print(GSON.toJson(history));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        HttpSession session = request.getSession(false);
        String username = session != null ? (String) session.getAttribute("username") : null;

        if (username == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        try {
            UserRunHistory runEntry = GSON.fromJson(request.getReader(), UserRunHistory.class);

            if (runEntry == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid run history data");
                return;
            }

            Map<String, List<UserRunHistory>> userHistoryMap = getOrInitUserHistoryMap();

            synchronized (userHistoryMap) {
                List<UserRunHistory> userHistory =
                        userHistoryMap.computeIfAbsent(username, k -> new ArrayList<>());

                runEntry.setRunNumber(userHistory.size() + 1);
                runEntry.setUsername(username);

                userHistory.add(runEntry);

            }

            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.print(GSON.toJson(runEntry));
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Error recording run history: " + e.getMessage());
        }
    }
}
