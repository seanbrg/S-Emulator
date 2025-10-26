package emulator.servlets;

import com.google.gson.Gson;
import emulator.utils.ContextUtils;
import emulator.utils.WebConstants;
import execute.Engine;
import execute.dto.ProgramDTO;
import execute.dto.ProgramMetadataDTO;
import execute.dto.VariableDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import static emulator.utils.ServletsUtils.sendError;

@WebServlet(name = "ProgramsServlet", urlPatterns = {WebConstants.PROGRAMS_W_PATH})
public class ProgramsServlet extends HttpServlet {
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

        final String path = request.getPathInfo(); // null, "/", "/list", "/metadata", etc.
        final String programName = request.getParameter(WebConstants.PROGRAM_NAME);
        final String degreeStr = request.getParameter(WebConstants.PROGRAM_DEGREE);
        final String varListStr = request.getParameter(WebConstants.PROGRAM_VARLIST);

        // Exact with query params pecific program
        if (path == null || "/".equals(path)) {
            if (programName != null || degreeStr != null) {
                // Require both if either is present
                if (programName == null || degreeStr == null) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Provide both 'programName' and 'programDegree', or neither to list all.");
                    return;
                }

                final int degree;
                try { degree = Integer.parseInt(degreeStr); }
                catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "'programDegree' must be an integer.");
                    return;
                }

                boolean varList = false;
                if (varListStr != null) {
                    try { varList = Boolean.parseBoolean(varListStr); }
                    catch (Exception e) {
                        sendError(response, HttpServletResponse.SC_BAD_REQUEST, "'programVarList' must be true or false.");
                        return;
                    }
                }

                if (varList) {
                    // return variable list of specific program
                    List<VariableDTO> varsDto;
                    try {
                        synchronized (engine) {
                            if (!engine.isProgramExists(programName, degree)) {
                                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Program " + programName + " not found.");
                                return;
                            }
                            varsDto = engine.getOutputs(programName, degree);
                        }
                        PrintWriter out = response.getWriter();
                        out.print(GSON.toJson(varsDto));
                    } catch (Exception e) {
                        sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                                "Error getting variable list: " + e.getMessage());
                    }
                    return;
                }
                else {
                    // return program DTO
                    ProgramDTO dto;
                    synchronized (engine) {
                        if (!engine.isProgramExists(programName, degree)) {
                            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Program not found.");
                            return;
                        }
                        dto = engine.getProgramDTO(programName, degree);
                    }
                    try (PrintWriter out = response.getWriter()) {
                        out.print(GSON.toJson(dto));
                    }
                    return;
                }
            }


            List<ProgramDTO> allPrograms;
            synchronized (engine) { allPrograms = engine.getAllProgramDTOs(); }
            try (PrintWriter out = response.getWriter()) {
                out.print(GSON.toJson(allPrograms));
            }
            return;
        }

        // list all programs with metadata
        if (WebConstants.PROGRAMS_METADATA_PATH.equals(path)) {  // /metadata
            List<ProgramMetadataDTO> metadata;
            synchronized (engine) {
                metadata = engine.getAllProgramMetadata();
            }
            try (PrintWriter out = response.getWriter()) {
                out.print(GSON.toJson(metadata));
            }
            return;
        }


        if (WebConstants.PROGRAMS_LIST_PATH.equals(path)) { // /list
            List<String> names;
            synchronized (engine) { names = engine.getAllProgramNames(); }
            try (PrintWriter out = response.getWriter()) {
                out.print(GSON.toJson(names));
            }
            return;
        }


        if (WebConstants.PROGRAMS_FUNC_PATH.equals(path)) { // /func
            if (programName != null) {
                ProgramDTO function;

                try {
                    synchronized (engine) {
                        if (!engine.isProgramExists(programName, 0)) {
                            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Program not found.");
                            return;
                        }
                        function = engine.getProgramDTO(programName, 0);
                    }
                    PrintWriter out = response.getWriter();
                    out.print(GSON.toJson(function));
                    return;
                } catch (Exception e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Error getting program function: " + e.getMessage());
                    return;
                }
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Provide 'programName' to get its function.");
                return;
            }
        }

        if (WebConstants.MAXDEGREE_PATH.equals(path)) {
            if (programName != null) {
                int maxDegree;

                try {
                    PrintWriter out = response.getWriter();
                    synchronized (engine) {
                        if (!engine.isProgramExists(programName, 0)) {
                            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Program not found.");
                            return;
                        }
                        maxDegree = engine.maxDegree(programName);
                    }
                    out.print(GSON.toJson(maxDegree));
                    return;
                } catch (Exception e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Error getting max degree: " + e.getMessage());
                    return;
                }
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Provide 'programName' to get its max degree.");
                return;
            }
        }


        sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid URL. " +
                "Use /semulator/programs, /semulator/programs?programName=&programDegree=, /semulator/programs/list, or /semulator/programs/metadata.");
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

            // Get username from session
            HttpSession session = request.getSession(false);
            String userName = session != null ? (String) session.getAttribute("username") : "Unknown";

            synchronized (engine) {
                engine.loadFromStream(in, userName);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Error loading program: " + e.getMessage());
        }
    }
}