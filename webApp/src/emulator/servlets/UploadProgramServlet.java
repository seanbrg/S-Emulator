package emulator.servlets;

import emulator.utils.ContextUtils;
import emulator.utils.WebConstants;
import execute.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

import static emulator.utils.ServletsUtils.sendError;

@WebServlet (name = "UploadProgramServlet", urlPatterns = {WebConstants.UPLOAD_PATH})
public class UploadProgramServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try (InputStream in = request.getInputStream()) {
            Engine engine = ContextUtils.getEngine(getServletContext());

            if (in == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "No input stream found in request.");
                return;
            }
            synchronized (engine) {
                engine.loadFromStream(in);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error loading program: " + e.getMessage());
        }
    }
}
