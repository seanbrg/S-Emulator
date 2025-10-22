package emulator.utils;

public class WebConstants {
    public static final String USERNAME = "username";
    public static final String ENGINE = "engine";
    public static final String PROGRAM_NAME = "programName";
    public static final String PROGRAM_DEGREE = "programDegree";

    // paths
    private final static String SERVER_PATH = "http://localhost:8080/semulator";
    public final static String UPLOAD_PATH = "/uploadProgram";
    public final static String PROGRAMS_PATH = "/programs";

    public final static String UPLOAD_URL = SERVER_PATH + UPLOAD_PATH;
    public final static String PROGRAMS_URL = SERVER_PATH + PROGRAMS_PATH;
}
