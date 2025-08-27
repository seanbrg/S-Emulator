package execute.components;

import logic.instructions.api.*;

import logic.program.Program;
import logic.program.SProgram;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;

public class XmlLoader {

    public static Program parse(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("❌ Error: File does not exist.");
            return null;
        }
        if (!filePath.toLowerCase().endsWith(".xml")) {
            System.out.println("❌ Error: File is not an XML file.");
            return null;
        }

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            String programName = doc.getDocumentElement().getAttribute("name");
            Program program = new SProgram(programName);

            // Match the actual XML tag <S-Instruction>
            NodeList instrNodes = doc.getElementsByTagName("S-Instruction");
            System.out.println("Found " + instrNodes.getLength() + " instructions in XML");

            for (int i = 0; i < instrNodes.getLength(); i++) {
                Element instrElem = (Element) instrNodes.item(i);

                String instrName = instrElem.getAttribute("name");   // e.g. DECREASE, INCREASE
                String type = instrElem.getAttribute("type");        // e.g. basic / synthetic

                // Extract <S-Variable>
                String variable = "";
                NodeList varNodes = instrElem.getElementsByTagName("S-Variable");
                if (varNodes.getLength() > 0) {
                    variable = varNodes.item(0).getTextContent().trim();
                }

                // Extract <S-Label>
                String label = "";
                NodeList labelNodes = instrElem.getElementsByTagName("S-Label");
                if (labelNodes.getLength() > 0) {
                    label = labelNodes.item(0).getTextContent().trim();
                }

                SInstruction instr = createInstruction(instrName, variable, label);
                if (instr != null) {
                    program.addInstruction(instr);
                } else {
                    System.out.println("⚠ Unknown instruction name: " + instrName + " (type=" + type + ")");
                }
            }

            return program;

        } catch (Exception e) {
            System.out.println("❌ Error parsing XML: " + e.getMessage());
            return null;
        }
    }

    private static SInstruction createInstruction(String name, String variable, String label) {
        return switch (name.toUpperCase()) {
            case "INCREASE" -> new IncreaseInstruction(variable, label);
            case "DECREASE" -> new DecreaseInstruction(variable, label);
            case "JUMP_NOT_ZERO" -> new JumpNotZeroInstruction(variable, label);
            case "NEUTRAL" -> new NoOpInstruction(variable, label);
            default -> null;
        };
    }

}
