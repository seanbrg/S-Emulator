package emulator.utils;

public class WebConstants {
    // parameter names
    public static final String USERNAME = "username";
    public static final String ENGINE = "engine";
    public static final String PROGRAM_NAME = "programName";
    public static final String PROGRAM_DEGREE = "programDegree";
    public static final String PROGRAM_VARLIST = "programVarList";

    // Server configuration
    private final static String SERVER_PATH = "http://localhost:8080/semulator";

    // Authentication paths
    public final static String LOGIN_PATH = "/login";
    public final static String LOGOUT_PATH = "/logout";
    public final static String USERS_PATH = "/users";
    public final static String MAXDEGREE_PATH = "/maxdegree";

    // Programs paths
    public final static String PROGRAMS_PATH = "/programs";
    private final static String WILDCARD_PATH = "/*";
    public final static String PROGRAMS_W_PATH = PROGRAMS_PATH + WILDCARD_PATH;
    public final static String PROGRAMS_LIST_PATH = "/list";
    public final static String INPUTS_PATH = "/inputs";

    // Full URLs
    public final static String LOGIN_URL = SERVER_PATH + LOGIN_PATH;
    public final static String LOGOUT_URL = SERVER_PATH + LOGOUT_PATH;
    public final static String USERS_URL = SERVER_PATH + USERS_PATH;
    public final static String PROGRAMS_URL = SERVER_PATH + PROGRAMS_PATH;
    public final static String PROGRAMS_LIST_URL = PROGRAMS_URL + PROGRAMS_LIST_PATH;
    public final static String INPUTS_URL = SERVER_PATH + INPUTS_PATH;
    public final static String MAXDEGREE_URL = PROGRAMS_URL + MAXDEGREE_PATH;
}
