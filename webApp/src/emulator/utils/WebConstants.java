package emulator.utils;

public class WebConstants {
    // parameter names
    public static final String USERNAME = "username";
    public static final String ENGINE = "engine";
    public static final String PROGRAM_NAME = "programName";
    public static final String PROGRAM_DEGREE = "programDegree";
    public static final String PROGRAM_VARLIST = "programVarList";
    public static final String PROGRAM_INPUTS = "programInputs";
    public static final String PROGRAM_STATS = "programStats";
    public static final String PROGRAM_OWNERS = "programOwners";
    public static final String HISTORIES = "histories";
    public static final String PROGRAM_ARCH = "arch";

    // Server configuration
    private final static String SERVER_PATH = "http://localhost:8080/semulator";

    // Authentication paths
    public final static String LOGIN_PATH = "/login";
    public final static String LOGOUT_PATH = "/logout";
    public final static String USERS_PATH = "/users";
    public final static String MAXDEGREE_PATH = "/maxdegree";
    public final static String RUN_PATH = "/run";
    public final static String DEBUG_PATH = "/debug";
    public static final String DEBUG_START_PATH = "/start";
    public static final String DEBUG_NEXT_PATH = "/next";
    public static final String USERS_HISTORY_PATH = "/userHistory";

    // Programs paths
    public final static String PROGRAMS_PATH = "/programs";
    private final static String WILDCARD_PATH = "/*";
    public final static String PROGRAMS_W_PATH = PROGRAMS_PATH + WILDCARD_PATH;
    public final static String PROGRAMS_LIST_PATH = "/list";
    public static final String PROGRAMS_FUNC_PATH = "/func";
    public static final String PROGRAMS_METADATA_PATH = "/metadata";
    public final static String INPUTS_PATH = "/inputs";
    public final static String DEBUG_W_PATH = DEBUG_PATH + WILDCARD_PATH;
    public static final String PROGRAMS_FUNC_METADATA_PATH = PROGRAMS_FUNC_PATH + PROGRAMS_METADATA_PATH;

    // Full URLs
    public final static String LOGIN_URL = SERVER_PATH + LOGIN_PATH;
    public final static String LOGOUT_URL = SERVER_PATH + LOGOUT_PATH;
    public final static String USERS_URL = SERVER_PATH + USERS_PATH;
    public final static String PROGRAMS_URL = SERVER_PATH + PROGRAMS_PATH;
    public final static String PROGRAMS_LIST_URL = PROGRAMS_URL + PROGRAMS_LIST_PATH;
    public final static String PROGRAMS_FUNC_URL = PROGRAMS_URL + PROGRAMS_FUNC_PATH;
    public final static String PROGRAMS_METADATA_URL = PROGRAMS_URL + PROGRAMS_METADATA_PATH;
    public static final String PROGRAMS_FUNC_METADATA_URL = PROGRAMS_URL + PROGRAMS_FUNC_METADATA_PATH;
    public final static String INPUTS_URL = SERVER_PATH + INPUTS_PATH;
    public final static String MAXDEGREE_URL = PROGRAMS_URL + MAXDEGREE_PATH;
    public final static String RUN_URL = SERVER_PATH + RUN_PATH;
    public final static String DEBUG_URL = SERVER_PATH + DEBUG_PATH;
    public final static String DEBUG_START_URL = DEBUG_URL + DEBUG_START_PATH;
    public final static String DEBUG_NEXT_URL = DEBUG_URL + DEBUG_NEXT_PATH;
    public static final String USERS_HISTORY_URL = SERVER_PATH + USERS_HISTORY_PATH;
}
