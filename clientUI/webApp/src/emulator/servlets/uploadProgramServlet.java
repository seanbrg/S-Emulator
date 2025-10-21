package emulator.servlets;

import emulator.utils.Constants;
import emulator.utils.ContextUtils;
import execute.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;

@WebServlet (name = "uploadProgramServlet", urlPatterns = {"/uploadProgram"})
public class uploadProgramServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Engine engine = ContextUtils.getEngine(getServletContext());
        Boolean printMode = Boolean.parseBoolean(request.getParameter(Constants.PRINTMODE));

        try (InputStream in = request.getInputStream()) {
            if (in == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "No input stream found in request.");
                return;
            }
            engine.loadFromStream(in);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error loading program: " + e.getMessage());
        }
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print("Error loading program: " + message);
        }
    }
}
