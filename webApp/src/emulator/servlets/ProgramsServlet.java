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
import java.io.PrintWriter;

@WebServlet(name = "ProgramsServlet", urlPatterns = {WebConstants.PROGRAMS_PATH})
public class ProgramsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try (PrintWriter out = response.getWriter()) {
            Engine engine = ContextUtils.getEngine(getServletContext());

            synchronized (engine) {
                String programName = request.getParameter(WebConstants.PROGRAM_NAME);
                int programDegree = Integer.parseInt(request.getParameter(WebConstants.PROGRAM_DEGREE));

                if (!engine.isProgramExists(programName, programDegree)) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write("Program not found.");
                } else {
                    response.setStatus(HttpServletResponse.SC_OK);

                    ProgramDTO res = engine.getProgramDTO(programName, programDegree);
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(res);

                    out.print(jsonResponse);
                    out.flush();
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error loading program: " + e.getMessage());
        }
    }
}