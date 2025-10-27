package emulator.utils;

import execute.Engine;
import execute.EngineImpl;
import jakarta.servlet.ServletContext;

import java.util.HashMap;
import java.util.Map;

public class ContextUtils {

    private static final Object engineLock = new Object();
    private static final Object statsLock = new Object();
    private static final Object ownersLock = new Object();

    public static Engine getEngine(ServletContext servletContext) {

        synchronized (engineLock) {
            if (servletContext.getAttribute(WebConstants.ENGINE) == null) { // create new engine if not exists
                Engine engine = new EngineImpl();
                engine.setPrintMode(false);
                servletContext.setAttribute(WebConstants.ENGINE, engine);
            }
        }
        return (Engine) servletContext.getAttribute(WebConstants.ENGINE);
    }

    public static Map<String, ProgramRunStats> getProgramStats(ServletContext servletContext) {
        synchronized (statsLock) {
            if (servletContext.getAttribute(WebConstants.PROGRAM_STATS) == null) {
                Map<String, ProgramRunStats> programStats = new HashMap<>();
                servletContext.setAttribute(WebConstants.PROGRAM_STATS, programStats);
            }
        }
        return (Map<String, ProgramRunStats>) servletContext.getAttribute(WebConstants.PROGRAM_STATS);
    }

    public static Map<String, String> getProgramOwners(ServletContext servletContext) {
        synchronized (ownersLock) {
            if (servletContext.getAttribute(WebConstants.PROGRAM_OWNERS) == null) {
                Map<String, String> programOwners = new HashMap<>();
                servletContext.setAttribute(WebConstants.PROGRAM_OWNERS, programOwners);
            }
        }
        return (Map<String, String>) servletContext.getAttribute(WebConstants.PROGRAM_OWNERS);
    }
}
