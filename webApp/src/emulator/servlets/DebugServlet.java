package emulator.servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import emulator.utils.ContextUtils;
import emulator.utils.WebConstants;
import execute.Engine;
import execute.dto.HistoryDTO;
import execute.dto.VariableDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;

import static emulator.utils.ServletsUtils.sendError;


@WebServlet(name = "DebugServlet", urlPatterns = {WebConstants.DEBUG_W_PATH})
public class DebugServlet extends HttpServlet {

    private static final Gson GSON = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Engine engine = ContextUtils.getEngine(getServletContext());
        if (engine == null) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Engine not initialized.");
            return;
        }

        final String path = request.getPathInfo();

        if (path.equals(WebConstants.DEBUG_START_PATH)) {
            // start debug session
            final String programName = request.getParameter(WebConstants.PROGRAM_NAME);
            final String degreeStr = request.getParameter(WebConstants.PROGRAM_DEGREE);
            if (programName == null || degreeStr == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Provide both 'programName' and 'programDegree'.");
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
                            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                                    "Program '" + programName + "' with degree " + degree + " does not exist.");
                            return;
                        }

                        response.setStatus(HttpServletResponse.SC_OK);
                        engine.debugStart(programName, degree, inputsDto);
                        HttpSession session = request.getSession();
                        session.setAttribute(WebConstants.PROGRAM_INPUTS, inputsDto);
                        session.setAttribute(WebConstants.PROGRAM_VARLIST, engine.getOutputs(programName, degree));
                        session.setAttribute(WebConstants.PROGRAM_NAME, programName);
                        session.setAttribute(WebConstants.PROGRAM_DEGREE, degree);

                    }
                } catch (Exception e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Error loading inputs: " + e.getMessage());
                }
            } catch (Exception e) {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "An error occurred while starting debug: " + e.getMessage());
            }
        }
        else if (path.equals(WebConstants.DEBUG_NEXT_PATH)) {
            // next debug step
            HttpSession session = request.getSession(false);
            if (session == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "No active debug session.");
                return;
            }
            String programName = (String) session.getAttribute(WebConstants.PROGRAM_NAME);
            Integer degree = (Integer) session.getAttribute(WebConstants.PROGRAM_DEGREE);
            if (programName == null || degree == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Debug session data missing.");
                return;
            }

            try {
                synchronized (engine) {
                    boolean hasMore = engine.debugStep(programName, degree);
                    int debugLine = engine.getDebugLine();
                    List<VariableDTO> variables = engine.getOutputs(programName, degree);

                    session.setAttribute(WebConstants.PROGRAM_VARLIST, engine.getOutputs(programName, degree));

                    DebugInfo debugInfo = new DebugInfo(hasMore, debugLine, variables);
                    String jsonResponse = GSON.toJson(debugInfo);
                    response.setStatus(HttpServletResponse.SC_OK);
                    PrintWriter out = response.getWriter();
                    out.println(jsonResponse);
                    out.flush();
                }
            } catch (Exception e) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Error during debug step: " + e.getMessage());
            }
        } else {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid path.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // get current debug state
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        try {
            Engine engine = ContextUtils.getEngine(getServletContext());
            if (engine == null) {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Engine not initialized.");
                return;
            }

            HttpSession session = request.getSession(false);
            if (session == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "No active debug session.");
                return;
            }

            String programName = (String) session.getAttribute(WebConstants.PROGRAM_NAME);
            Integer degree = (Integer) session.getAttribute(WebConstants.PROGRAM_DEGREE);
            List<VariableDTO> inputsDto = (List<VariableDTO>) session.getAttribute(WebConstants.PROGRAM_INPUTS);
            if (programName == null || degree == null || inputsDto == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Debug session data missing.");
                return;
            }

            synchronized (engine) {
                HistoryDTO historyDto = engine.recordCurrentState(programName, degree, inputsDto);
                String jsonResponse = GSON.toJson(historyDto);
                response.setStatus(HttpServletResponse.SC_OK);
                PrintWriter out = response.getWriter();
                out.println(jsonResponse);
                out.flush();
            }
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An error occurred while retrieving debug state: " + e.getMessage());
        }
    }

    public static class DebugInfo {
        private final boolean hasMore;
        private final int debugLine;
        private final List<VariableDTO> variables;

        public DebugInfo(boolean hasMore, int debugLine, List<VariableDTO> variables) {
            this.hasMore = hasMore;
            this.debugLine = debugLine;
            this.variables = variables;
        }

        public boolean isHasMore() {
            return hasMore;
        }

        public int getDebugLine() {
            return debugLine;
        }

        public List<VariableDTO> getVariables() {
            return variables;
        }
    }
}
