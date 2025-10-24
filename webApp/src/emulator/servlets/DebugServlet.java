package emulator.servlets;

import emulator.utils.WebConstants;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;


@WebServlet(name = "DebugServlet", urlPatterns = {WebConstants.DEBUG_PATH})
public class DebugServlet extends HttpServlet {
}
