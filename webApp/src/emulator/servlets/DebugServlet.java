package emulator.servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import emulator.utils.ContextUtils;
import emulator.utils.WebConstants;
import execute.Engine;
import execute.dto.VariableDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

import static emulator.utils.ServletsUtils.sendError;


@WebServlet(name = "DebugServlet", urlPatterns = {WebConstants.DEBUG_W_PATH})
public class DebugServlet extends HttpServlet {

    private static final Gson GSON = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Engine engine = ContextUtils.getEngine(getServletContext());
        if (engine == null) { sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Engine not initialized."); return; }

        final String path = request.getPathInfo(); // null, "/", or "/list"
        final String name = request.getParameter(WebConstants.PROGRAM_NAME);
        final String degreeStr = request.getParameter(WebConstants.PROGRAM_DEGREE);

        if (!path.equals(WebConstants.DEBUG_START_PATH)) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid path.");
        }

        if (name == null || degreeStr == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Provide both 'programName' and 'programDegree'.");
            return;
        }

        final int degree;
        try { degree = Integer.parseInt(degreeStr); }
        catch (NumberFormatException e) {
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
                    if (!engine.isProgramExists(name, degree)) {
                        sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                                "Program '" + name + "' with degree " + degree + " does not exist.");
                        return;
                    }

                    engine.debugStart(name, degree, inputsDto);
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            } catch (Exception e) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Error loading inputs: " + e.getMessage());
            }
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An error occurred while starting debug: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }
}
