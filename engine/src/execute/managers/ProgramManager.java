package execute.managers;

import logic.program.Program;

import java.util.ArrayList;
import java.util.List;

public class ProgramManager {
    List<Program> programExpansions = new ArrayList<Program>();

    public ProgramManager() {}

    public void loadNewProgram(Program program) {
        programExpansions.clear();
        programExpansions.add(program);
    }

    public Program getProgram(int degree) {
        if  (programExpansions.isEmpty()) {
            return null;
        }
        else if (degree >= programExpansions.size()) {
            this.expand(degree);
        }
        return programExpansions.get(degree);
    }

    private void expand(int degree) {

    }
}
