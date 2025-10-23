package emulator.servlets;

import com.google.gson.Gson;
import emulator.utils.ContextUtils;
import emulator.utils.WebConstants;
import execute.Engine;
import execute.dto.ProgramDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import static emulator.utils.ServletsUtils.sendError;

@WebServlet(name = "ProgramsServlet", urlPatterns = {WebConstants.PROGRAMS_PATH})
public class ProgramsServlet extends HttpServlet {
    private static Gson GSON = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try (PrintWriter out = response.getWriter()) {
            Engine engine = ContextUtils.getEngine(getServletContext());

            if (engine == null) {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Engine not initialized.");
                return;
            }

            synchronized (engine) {
                String path = request.getPathInfo();

                if (path == null || path.equals("/")) {
                    // return all programs
                    response.setStatus(HttpServletResponse.SC_OK);

                    ProgramDTO[] allPrograms = engine.getAllProgramDTOs();
                    String jsonResponse = GSON.toJson(allPrograms);
                    out.print(jsonResponse);
                    out.flush();
                }
                else if (path.split("/").length != 1) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid URL format. To get a specific program, use /programs?programName={name}&programDegree={degree} or /programs to get all programs.");
                }
                else if (request.getParameter(WebConstants.PROGRAM_NAME) == null ||
                        request.getParameter(WebConstants.PROGRAM_DEGREE) == null) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Missing parameters. Please provide both 'programName' and 'programDegree'.");
                }
                else {
                    // continue to get specific program

                    String programName = request.getParameter(WebConstants.PROGRAM_NAME);
                    try {
                        int programDegree = Integer.parseInt(request.getParameter(WebConstants.PROGRAM_DEGREE));
                        if (!engine.isProgramExists(programName, programDegree)) {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            out.write("Program not found.");
                        } else {
                            response.setStatus(HttpServletResponse.SC_OK);

                            ProgramDTO res = engine.getProgramDTO(programName, programDegree);
                            String jsonResponse = GSON.toJson(res);

                            out.print(jsonResponse);
                            out.flush();
                        }
                    } catch (NumberFormatException e) {
                        sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                                "Invalid 'programDegree' parameter. It must be an integer.");
                    }
                }
            }
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Error retrieving program: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try (InputStream in = request.getInputStream()) {
            Engine engine = ContextUtils.getEngine(getServletContext());

            if (engine == null) {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Engine not initialized.");
                return;
            }

            synchronized (engine) {
                engine.loadFromStream(in);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Error loading program: " + e.getMessage());
        }
    }
}