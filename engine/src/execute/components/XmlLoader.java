package execute.components;

import logic.instructions.*;
import logic.instructions.api.basic.*;

import logic.instructions.api.synthetic.*;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.labels.NumericLabel;
import logic.program.Program;
import logic.program.SProgram;
import logic.variables.Var;
import logic.variables.Variable;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.List;
import java.util.Map;

public class XmlLoader {

    public static Program parse(String filePath, Map<String, Variable> vars,
                                List<Instruction> instructions, Map<Label, Instruction> labels) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Error: File does not exist.");
            return null;
        }
        if (!filePath.toLowerCase().endsWith(".xml")) {
            System.out.println("Error: File is not an XML file.");
            return null;
        }

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            String programName = doc.getDocumentElement().getAttribute("name");

            // Match the actual XML tag <S-Instruction>
            NodeList instrNodes = doc.getElementsByTagName("S-Instruction");
            System.out.println("Found " + instrNodes.getLength() + " instructions in XML");

            for (int i = 0; i < instrNodes.getLength(); i++) {
                Element instrElem = (Element) instrNodes.item(i);

                String instrName = instrElem.getAttribute("name");   // e.g. DECREASE, INCREASE
                String type = instrElem.getAttribute("type");        // e.g. basic / synthetic

                // Extract <S-Variable>
                String varName = "";
                Variable var = null;
                NodeList varNodes = instrElem.getElementsByTagName("S-Variable");
                if (varNodes.getLength() > 0) {
                    varName = varNodes.item(0).getTextContent().trim();
                    if (!vars.containsKey(varName)) {
                        vars.put(varName, new Var(varName));
                    }
                    var = vars.get(varName);
                }

                // Extract <S-Label>
                String labelName = "";
                Label selfLabel = null;
                NodeList labelNodes = instrElem.getElementsByTagName("S-Label");
                if (labelNodes.getLength() > 0) {
                    labelName = labelNodes.item(0).getTextContent().trim();
                    selfLabel = parseLabel(labelName);
                }
                else {
                    selfLabel = FixedLabel.EMPTY;
                }

                // Extract <S-Instruction-Argument> (e.g., JNZLabel)
                Label targetLabel = FixedLabel.EMPTY;  // default if no targetLabel provided
                NodeList argParents = instrElem.getElementsByTagName("S-Instruction-Arguments");
                if (argParents.getLength() > 0) {
                    Element argsElem = (Element) argParents.item(0);
                    NodeList argNodes = argsElem.getElementsByTagName("S-Instruction-Argument");
                    for (int j = 0; j < argNodes.getLength(); j++) {
                        Element argElem = (Element) argNodes.item(j);
                        String argName  = argElem.getAttribute("name");
                        String argValue = argElem.getAttribute("value");

                        // Handle known arguments
                        if ("JNZLabel".equalsIgnoreCase(argName)) {
                            targetLabel = parseLabel(argValue);
                        }

                        // Add more arguments here as needed:
                        // else if ("SomeOtherArg".equalsIgnoreCase(argName)) { ... }
                    }
                }

                Instruction instr = createInstruction(instrName, var, selfLabel, targetLabel);
                if (instr != null) {
                    instructions.add(instr);
                    labels.put(selfLabel, instr);
                } else {
                    System.out.println("Unknown instruction name: " + instrName + " (type=" + type + ")");
                }
            }

            return new SProgram(programName, labels, instructions);

        } catch (Exception e) {
            System.out.println("Error parsing XML: " + e.getMessage());
            return null;
        }
    }

    private static Instruction createInstruction(String name, Variable var, Label selfLabel, Label target) {
        return switch (name.toUpperCase()) {
            case "INCREASE" -> new Increase(selfLabel,var);
            case "DECREASE" -> new Decrease(selfLabel, var);
            case "JUMP_NOT_ZERO" -> new JumpNotZero(selfLabel, var, target);
            case "NEUTRAL" -> new Neutral(selfLabel, var);
            case "ZERO_VARIABLE" -> new ZeroVariable(selfLabel, var);
            case "GOTO_LABEL" -> new GoToLabel(selfLabel, target);
            //case "ASSIGNMENT" -> new Assignment(selfLabel, ); needs 2 variables
            //case "CONSTANT_ASSIGNMENT" ->  new ConstantAssignment(selfLabel, var); needs constant
            case "JUMP_ZERO" -> new JumpZero(selfLabel, var, target);
            //case "JUMP_EQUAL_CONSTANT" -> new JumpEqualConstant() needs constant
            //case "JUMP_EQUAL_VARIABLE" -> new JumpEqualVariable() needs 2 variables
            default -> null;
        };
    }

    private static Label parseLabel(String labelValue) {
        String v = labelValue.trim();
        Label target = null;
        char c0 = Character.toUpperCase(v.charAt(0));
        switch (c0) {
            case 'L' -> {
                int n = Integer.parseInt(v.substring(1));
                target = new NumericLabel(n);
            }
            case 'E' -> target = FixedLabel.EXIT;
            default  -> target = FixedLabel.EMPTY;
        }

        return target;
    }

}
