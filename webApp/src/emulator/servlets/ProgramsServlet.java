package emulator.servlets;

import emulator.utils.ContextUtils;
import emulator.utils.WebConstants;
import execute.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "ProgramsServlet", urlPatterns = {WebConstants.PROGRAMS_PATH})
public class ProgramsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Engine engine = ContextUtils.getEngine(getServletContext());


    }
}
