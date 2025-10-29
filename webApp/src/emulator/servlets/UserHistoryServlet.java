package emulator.servlets;

import com.google.gson.Gson;
import emulator.utils.ContextUtils;
import emulator.utils.SessionUtils;
import emulator.utils.WebConstants;
import execute.dto.HistoryDTO;
import execute.dto.UserRunHistoryDTO;
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
import java.util.stream.Collectors;

import static emulator.utils.ServletsUtils.sendError;

@WebServlet(name = "UserHistoryServlet", urlPatterns = {WebConstants.USERS_HISTORY_PATH})
public class UserHistoryServlet extends HttpServlet {

    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        String path = request.getPathInfo();

        /*
        // Handle /userHistory/run endpoint for detailed run information
        if (WebConstants.RUN_PATH.equals(path)) {
            handleGetRunDetails(request, response);
            return;
        }
        */


        // Handle /userHistory endpoint for user history list
        String targetUsername = request.getParameter(WebConstants.USERNAME);
        Map<String, List<UserRunHistoryDTO>> userHistoryMap = ContextUtils.getUserHistoryMap(getServletContext());
        List<UserRunHistoryDTO> history;

        try {
            synchronized (userHistoryMap) {
                if (userHistoryMap != null) {
                    if (targetUsername == null || targetUsername.isEmpty()) {
                        if (userHistoryMap.isEmpty()) {
                            history = new ArrayList<>();
                        } else {
                            history = userHistoryMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
                        }
                    } else {
                        List<UserRunHistoryDTO> historyTmp = userHistoryMap.get(targetUsername);
                        history = (historyTmp == null) ? new ArrayList<>() : historyTmp;
                    }
                } else {
                    history = new ArrayList<>();
                }
                String jsonResponse = GSON.toJson(history);
                PrintWriter out = response.getWriter();
                out.println(jsonResponse);
                out.flush();
                }
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Error retrieving history: " + e.getMessage());
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
            UserRunHistoryDTO runEntry = GSON.fromJson(request.getReader(), UserRunHistoryDTO.class);

            if (runEntry == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid run history data");
                return;
            }

            Map<String, List<UserRunHistoryDTO>> userHistoryMap = ContextUtils.getUserHistoryMap(getServletContext());

            synchronized (userHistoryMap) {
                List<UserRunHistoryDTO> userHistory =
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