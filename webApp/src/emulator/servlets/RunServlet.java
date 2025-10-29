package emulator.servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import emulator.utils.ContextUtils;
import emulator.utils.ProgramRunStats;
import emulator.utils.SessionUtils;
import emulator.utils.WebConstants;
import execute.Engine;
import execute.dto.HistoryDTO;
import execute.dto.UserRunHistoryDTO;
import execute.dto.VariableDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import static emulator.utils.ServletsUtils.sendError;

@WebServlet(name = "RunServlet", urlPatterns = {WebConstants.RUN_PATH})
public class RunServlet extends HttpServlet {

    private static final Gson GSON = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        Engine engine = ContextUtils.getEngine(getServletContext());
        Map<String, ProgramRunStats> programStats = ContextUtils.getProgramStats(getServletContext());

        if (engine == null) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Engine not initialized.");
            return;
        }

        final String programName = request.getParameter(WebConstants.PROGRAM_NAME);
        final String degreeStr = request.getParameter(WebConstants.PROGRAM_DEGREE);

        // Require both if either is present
        if (programName == null || degreeStr == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Provide both 'programName' and 'programDegree', or neither to list all.");
            return;
        }

        final int degree;
        try {
            degree = Integer.parseInt(degreeStr);
        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "'programDegree' must be an integer.");
            return;
        }

        try {
            String ct = request.getContentType();
            if (ct == null || !ct.toLowerCase().contains("application/json")) {
                sendError(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Content-Type must be json.");
                return;
            }

            try (InputStream in = request.getInputStream();
                 InputStreamReader reader = new InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)) {

                Type listType = new TypeToken<List<VariableDTO>>() {}.getType();

                List<VariableDTO> inputsDto = GSON.fromJson(reader, listType);


                if (inputsDto == null) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: expected an array of variables.");
                    return;
                }

                synchronized (engine) {
                    if (!engine.isProgramExists(programName, degree)) {
                        sendError(response, HttpServletResponse.SC_NOT_FOUND,
                                "Program '" + programName + "' with degree " + degree + " not found.");
                        return;
                    }

                    // Execute the program
                    HistoryDTO resultDto = engine.runProgramAndRecord(programName, degree, inputsDto);

                    System.out.println("DEBUG: Full resultDto JSON: " + GSON.toJson(resultDto));

                    // Convert resultDto to UserRunHistory
                    UserRunHistoryDTO runEntry = new UserRunHistoryDTO();
                    runEntry.setRunNumber(resultDto.getNum());
                    runEntry.setMainProgram(resultDto.getProgram().isMainProgram());
                    runEntry.setProgramName(resultDto.getProgram().getProgramName());
                    runEntry.setArchitectureType(resultDto.getProgram().getArchitectureType());
                    runEntry.setRunLevel(resultDto.getDegree());
                    runEntry.setOutputValue(resultDto.getOutput() != null ? resultDto.getOutput().getValue() : 0);
                    runEntry.setCycles(resultDto.getCycles());
                    runEntry.setHistoryDTO(resultDto);

                    // Save runEntry to user history map in context
                    Map<String, List<UserRunHistoryDTO>> userHistoryMap = ContextUtils.getUserHistoryMap(getServletContext());
                    String username = SessionUtils.getUsername(request);

                    synchronized (userHistoryMap) {
                        List<UserRunHistoryDTO> thisUsersHistory = userHistoryMap.computeIfAbsent(username, k -> new ArrayList<>());
                        thisUsersHistory.add(runEntry);
                        System.out.println("DEBUG: Added runEntry to history for user " + username +
                                " (total runs=" + userHistoryMap.size() + ")");
                    }

                    // Calculate cost (you can adjust this formula based on your requirements)
                    // Example: cost = number of steps * degree * some multiplier
                    double executionCost = calculateExecutionCost(resultDto, degree);

                    // Record the execution statistics
                    synchronized (programStats) {
                        recordRunStats(programName, executionCost, programStats);
                    }

                    //System.out.println("Executed program '" + programName + "' degree " + degree + " successfully. " +
                            //"Inputs: " + inputsDto + " Result: " + resultDto.getOutput().getVarString() +
                            //" Cost: " + executionCost);

                    String jsonResponse = GSON.toJson(resultDto);
                    PrintWriter out = response.getWriter();
                    out.println(jsonResponse);
                    out.flush();
                }
                response.setStatus(HttpServletResponse.SC_OK);

            } catch (Exception e) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Error loading inputs: " + e.getMessage());
            }
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An error occurred while processing the request: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        String username = request.getParameter(WebConstants.USERNAME);
        if (username == null || username.isBlank()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing username parameter.");
            return;
        }

        Map<String, List<UserRunHistoryDTO>> userHistoryMap = ContextUtils.getUserHistoryMap(getServletContext());
        List<UserRunHistoryDTO> userHistory = userHistoryMap.getOrDefault(username, new ArrayList<>());

        //System.out.println("DEBUG: Returning " + userHistory.size() + " history entries for user " + username);

        String json = GSON.toJson(userHistory);
        PrintWriter out = response.getWriter();
        out.println(json);
        out.flush();
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // ------------------------- Helpers -------------------------
    private double calculateExecutionCost(HistoryDTO historyDto, int degree) {
        final double BASE_COST = 1.0;
        final double COST_PER_STEP = 0.5;
        final double DEGREE_MULTIPLIER = 1.5;
        int numberOfSteps = 1;
        return BASE_COST + (numberOfSteps * COST_PER_STEP) + (degree * DEGREE_MULTIPLIER);
    }

    public void recordRunStats(String programName, double cost, Map<String, ProgramRunStats> programStats) {
        ProgramRunStats stats = programStats.computeIfAbsent(programName, k -> new ProgramRunStats());
        stats.recordRun(cost);
    }
}
