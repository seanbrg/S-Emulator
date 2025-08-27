package execute;

public interface Engine {
    boolean loadFromXML(String filePath);

    void printProgram();

    long runProgram(int degree);

    int maxDegree();
}
