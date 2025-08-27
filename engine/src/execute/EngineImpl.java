package execute;

import logic.instructions.Instruction;
import logic.program.Program;
import logic.variables.Variable;
import java.util.HashMap;
import java.util.Map;

public class EngineImpl implements Engine {
    Program currentProgram;
    Map<String, Variable> currentVars;

    public EngineImpl() {}

    @Override
    public boolean loadFromXML(String filePath) {
        Map<String, Variable> vars = new HashMap<>();
        Program program = XmlLoader.parse(filePath, vars);
        if (program != null) {
            this.currentProgram = program;
            this.currentVars = vars;
            System.out.println("Program '" + program.getName() + "' loaded successfully!");
            return true;
        } else {
            System.out.println("Program not loaded. Keeping previous program.");
            return false;
        }
    }

    @Override
    public void printProgram() {
        if (currentProgram == null) {
            System.out.println("No program loaded.");
            return;
        }
        currentProgram.getInstructions().forEach(Instruction::print);
    }

    @Override
    public long runProgram(int degree) {
        currentProgram.run(degree);
        return currentVars.get("y").getValue();
    }

    @Override
    public int maxDegree() {
        return currentProgram.maxDegree();
    }
}
