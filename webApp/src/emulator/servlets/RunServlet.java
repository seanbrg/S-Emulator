package emulator.servlets;

import com.google.gson.Gson;
import emulator.utils.ContextUtils;
import emulator.utils.WebConstants;
import execute.Engine;
import execute.dto.HistoryDTO;
import execute.dto.VariableDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static emulator.utils.ServletsUtils.sendError;


@WebServlet(name = "RunServlet", urlPatterns = {WebConstants.RUN_PATH})
public class RunServlet extends HttpServlet {

    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        Engine engine = ContextUtils.getEngine(getServletContext());
        if (engine == null) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Engine not initialized.");
            return;
        }

        final String programName = req.getParameter(WebConstants.PROGRAM_NAME);
        final String degreeStr = req.getParameter(WebConstants.PROGRAM_DEGREE);

        if (programName != null || degreeStr != null) {
            // Require both if either is present
            if (programName == null || degreeStr == null) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Provide both 'programName' and 'programDegree', or neither to list all.");
                return;
            }
            final int degree;
            try {
                degree = Integer.parseInt(degreeStr);
            } catch (NumberFormatException e) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "'programDegree' must be an integer.");
                return;
            }

            try {
                synchronized (engine) {
                    if (!engine.isProgramExists(programName, degree)) {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND,
                                "Program '" + programName + "' with degree " + degree + " not found.");
                        return;
                    }
                    List<VariableDTO> inputs = engine.getInputs(programName, degree);
                    HistoryDTO resultDto = engine.runProgramAndRecord(programName, degree, inputs);
                    String jsonResponse = GSON.toJson(resultDto);
                    PrintWriter out = resp.getWriter();
                    out.println(jsonResponse);
                    out.flush();
                }
            } catch (Exception e) {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "An error occurred while processing the request: " + e.getMessage());
            }
        }
    }
}
