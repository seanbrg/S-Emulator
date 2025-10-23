package emulator.utils;

import com.google.gson.Gson;

public class WebConstants {
    public static final String USERNAME = "username";
    public static final String ENGINE = "engine";
    public static final String PROGRAM_NAME = "programName";
    public static final String PROGRAM_DEGREE = "programDegree";

    // paths
    public final static String WILDCARD_PATH = "/*";
    private final static String SERVER_PATH = "http://localhost:8080/semulator";
    public final static String PROGRAMS_PATH = "/programs";
    public final static String PROGRAMS_W_PATH = PROGRAMS_PATH + WILDCARD_PATH;
    public final static String PROGRAMS_LIST_PATH = "/list";

    public final static String PROGRAMS_URL = SERVER_PATH + PROGRAMS_PATH;
    public static final String PROGRAMS_LIST_URL = PROGRAMS_URL + PROGRAMS_LIST_PATH;
}
