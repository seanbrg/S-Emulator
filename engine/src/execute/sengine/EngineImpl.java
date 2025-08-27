package execute.sengine;

import execute.components.XmlLoader;
import logic.instructions.Instruction;
import logic.labels.Label;
import logic.program.Program;
import logic.variables.Variable;

import java.util.List;
import java.util.Map;

public class EngineImpl implements Engine {
    List<Instruction> instructions;
    Map<Label, Instruction> labels;
    Program currentProgram;
    Map<String, Variable> vars;

    public EngineImpl() {}

    @Override
    public boolean loadFromXML(String filePath) {
        Program program = XmlLoader.parse(filePath, vars, instructions, labels);
        if (program != null) {
            this.currentProgram = program;
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
}
