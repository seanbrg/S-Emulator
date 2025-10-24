package emulator.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static emulator.utils.ServletsUtils.sendError;

@WebServlet (name = "InputServlet", urlPatterns = {WebConstants.INPUTS_PATH})
public class InputServlet extends HttpServlet {
    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        Engine engine = ContextUtils.getEngine(getServletContext());
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
                synchronized (engine) {
                    if (!engine.isProgramExists(programName, degree)) {
                        sendError(response, HttpServletResponse.SC_NOT_FOUND,
                                "Program '" + programName + "' with degree " + degree + " not found.");
                        return;
                    }
                    List<VariableDTO> varsDto = engine.getInputs(programName, degree); // <- the main point of this block
                    try (PrintWriter out = response.getWriter()) {
                        out.print(GSON.toJson(varsDto));
                    }
                }
            } catch (Exception e) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Error retrieving inputs: " + e.getMessage());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Engine engine = ContextUtils.getEngine(getServletContext());
        if (engine == null) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Engine not initialized.");
            return;
        }

        // Optional but helpful: verify content type
        String ct = request.getContentType();
        if (ct == null || !ct.toLowerCase().contains("application/json")) {
            sendError(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Content-Type must be json.");
            return;
        }
        if (request.getContentLengthLong() == 0) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Empty request executionStage.");
            return;
        }

        try (InputStream in = request.getInputStream();
             InputStreamReader reader = new InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)) {

                Type listType = new TypeToken<List<VariableDTO>>() {}.getType();

                List<VariableDTO> varsDto = GSON.fromJson(reader, listType);
                if (varsDto == null) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: expected an array of variables.");
                    return;
                }

                synchronized (engine) {
                    engine.loadInputs(varsDto);
                }
                response.setStatus(HttpServletResponse.SC_OK);

        } catch (JsonSyntaxException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON: " + e.getMessage());
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Error loading inputs: " + e.getMessage());
        }
    }


}