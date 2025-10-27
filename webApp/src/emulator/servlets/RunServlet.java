package emulator.servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import emulator.utils.ContextUtils;
import emulator.utils.ProgramRunStats;
import emulator.utils.WebConstants;
import execute.Engine;
import execute.dto.HistoryDTO;
import execute.dto.VariableDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

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

        if (programName != null || degreeStr != null) {
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

                    Type listType = new TypeToken<List<VariableDTO>>() {
                    }.getType();
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

                        // Calculate cost (you can adjust this formula based on your requirements)
                        // Example: cost = number of steps * degree * some multiplier
                        double executionCost = calculateExecutionCost(resultDto, degree);

                        // Record the execution statistics
                        synchronized (programStats) {
                            recordRunStats(programName, executionCost, programStats);
                        }

                        System.out.println("Executed program '" + programName + "' degree " + degree + " successfully. " +
                                "Inputs: " + inputsDto + " Result: " + resultDto.getOutput().getVarString() +
                                " Cost: " + executionCost);

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
    }

    /**
     * Calculate the cost of executing a program
     * You can adjust this formula based on your requirements
     */
    private double calculateExecutionCost(HistoryDTO historyDto, int degree) {
        // Example cost calculation:
        // Base cost per execution + cost per step + multiplier for degree
        final double BASE_COST = 1.0;
        final double COST_PER_STEP = 0.5;
        final double DEGREE_MULTIPLIER = 1.5;

        // Get number of steps from history (if available in your HistoryDTO)
        // Adjust this based on your actual HistoryDTO structure
        int numberOfSteps = 1; // Default to 1 if not available

        // Calculate cost
        double cost = BASE_COST + (numberOfSteps * COST_PER_STEP) + (degree * DEGREE_MULTIPLIER);

        return cost;
    }

    public void recordRunStats(String programName, double cost, Map<String, ProgramRunStats> programStats) {
        ProgramRunStats stats = programStats.computeIfAbsent(
                programName,
                k -> new ProgramRunStats()
        );
        stats.recordRun(cost);
    }

}