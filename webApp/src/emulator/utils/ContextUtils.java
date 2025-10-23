package emulator.utils;

import execute.Engine;
import execute.EngineImpl;
import jakarta.servlet.ServletContext;

public class ContextUtils {

    private static final Object engineLock = new Object();

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

}
