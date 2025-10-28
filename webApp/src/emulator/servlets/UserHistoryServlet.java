package emulator.servlets;

import client.components.dashboard.userHistory.UserRunHistory;
import com.google.gson.Gson;
import emulator.utils.ContextUtils;
import emulator.utils.WebConstants;
import execute.dto.HistoryDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static emulator.utils.ServletsUtils.sendError;

@WebServlet(name = "UserHistoryServlet", urlPatterns = {"/userhistory"})
public class UserHistoryServlet extends HttpServlet {

    private static final Gson GSON = new Gson();
    private static final String USER_HISTORY_MAP_KEY = "userHistoryMap";

    @SuppressWarnings("unchecked")
    private Map<String, List<UserRunHistory>> getOrInitUserHistoryMap() {
        ServletContext context = getServletContext();
        Map<String, List<UserRunHistory>> map =
                (Map<String, List<UserRunHistory>>) context.getAttribute(USER_HISTORY_MAP_KEY);

        if (map == null) {
            map = new HashMap<>();
            context.setAttribute(USER_HISTORY_MAP_KEY, map);
        }
        return map;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        String path = request.getPathInfo();

        // Handle /userhistory/run endpoint for detailed run information
        if ("/run".equals(path)) {
            handleGetRunDetails(request, response);
            return;
        }

        // Handle /userhistory endpoint for user history list
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

    private void handleGetRunDetails(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String runNumberStr = request.getParameter("runNumber");

        if (username == null || runNumberStr == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Both username and runNumber are required");
            return;
        }

        int runNumber;
        try {
            runNumber = Integer.parseInt(runNumberStr);
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "runNumber must be an integer");
            return;
        }

        Map<String, List<UserRunHistory>> userHistoryMap = getOrInitUserHistoryMap();

        HistoryDTO historyDTO = null;
        synchronized (userHistoryMap) {
            List<UserRunHistory> history = userHistoryMap.get(username);
            if (history != null && runNumber > 0 && runNumber <= history.size()) {
                UserRunHistory runHistory = history.get(runNumber - 1);
                historyDTO = runHistory.getHistoryDTO();
            }
        }

        if (historyDTO == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND,
                    "Run not found for user " + username + " at run number " + runNumber);
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            out.print(GSON.toJson(historyDTO));
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