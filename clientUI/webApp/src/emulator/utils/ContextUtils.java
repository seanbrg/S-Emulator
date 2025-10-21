package emulator.utils;

import execute.Engine;
import execute.EngineImpl;
import jakarta.servlet.ServletContext;

public class ContextUtils {

    private static final Object engineLock = new Object();

    public static Engine getEngine(ServletContext servletContext) {

        synchronized (engineLock) {
            if (servletContext.getAttribute(Constants.ENGINE) == null) {
                servletContext.setAttribute(Constants.ENGINE, new EngineImpl());
            }
        }
        return (Engine) servletContext.getAttribute(Constants.ENGINE);
    }


}
