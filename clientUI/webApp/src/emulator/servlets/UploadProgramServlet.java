package emulator.servlets;

import emulator.utils.ContextUtils;
import execute.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;

import static emulator.utils.ServletsUtils.sendError;

@WebServlet (name = "UploadProgramServlet", urlPatterns = {"/uploadProgram"})
public class UploadProgramServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Engine engine = ContextUtils.getEngine(getServletContext());

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
}
