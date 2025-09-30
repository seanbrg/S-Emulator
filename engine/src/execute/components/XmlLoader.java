package execute.components;

import logic.arguments.Argument;
import logic.arguments.QuoteArgument;
import logic.arguments.VarArgument;
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
import logic.variables.VariableType;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class XmlLoader {

    private List<Program> programs;

    public XmlLoader() {
        this.programs = new ArrayList<>();
    }

    public List<Program> parse(String filePath, Map<String, Variable> varsMap, boolean printMode) {

        //printMode = true;  // for debug

        File file = new File(filePath);
        if (!file.exists()) {
            if (printMode) System.out.println("Error: File does not exist.");
            return null;
        }
        if (!filePath.toLowerCase().endsWith(".xml")) {
            if (printMode) System.out.println("Error: File is not an XML file.");
            return null;
        }

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            Element rootElement = doc.getDocumentElement();
            String programName = rootElement.getAttribute("name");
            List<Instruction> instructions;
            Map<Label, Instruction> labels = new HashMap<>();
            this.programs = new ArrayList<>();

            doc.getDocumentElement().normalize();
            // Get the main program's S-Instructions block
            Element mainInstrBlock = (Element) rootElement.getElementsByTagName("S-Instructions").item(0);

            NodeList funcNodes = rootElement.getElementsByTagName("S-Function");
            if (printMode) System.out.println("Found " + funcNodes.getLength() + " functions in XML");
            for (int i = 0; i < funcNodes.getLength(); i++) {
                Element funcElem = (Element) funcNodes.item(i);
                Program funcProgram = parseNewFunction(funcElem);
                if (funcProgram != null) programs.add(funcProgram);
                else return null;
            }
            for (int i = 0; i < funcNodes.getLength(); i++) {
                Element funcElem = (Element) funcNodes.item(i);
                Boolean result = parseFunctionInstructions(funcElem, varsMap, labels, printMode);
                if (!result) return null;
            }

            NodeList instrNodes = mainInstrBlock.getElementsByTagName("S-Instruction");
            if (printMode) System.out.println("Found " + instrNodes.getLength() + " instructions in main program");
            instructions = parseInstructions(instrNodes, varsMap, labels, printMode);
            Program result = new SProgram(programName, labels, instructions, null);
            if (!result.checkLabels()) {
                if (printMode) System.out.println("Error: Program has invalid labels.");
                return null;
            }
            programs.addFirst(result);

            if (printMode) System.out.println("Program loaded successfully.");
            return programs;

        } catch (Exception e) {
            if (printMode) {
                System.out.println("Error parsing XML: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }

    private List<Instruction> parseInstructions(NodeList instrNodes, Map<String, Variable> varsMap, Map<Label, Instruction> labels, boolean printMode) {
        List<Instruction> instructions = new ArrayList<>();

        for (int i = 0; i < instrNodes.getLength(); i++) {
            Element instrElem = (Element) instrNodes.item(i);

            String instrName = instrElem.getAttribute("name");
            String type = instrElem.getAttribute("type");

            // variable (main one if exists)
            String varName = "";
            Variable var = null;
            NodeList varNodes = instrElem.getElementsByTagName("S-Variable");
            if (varNodes.getLength() > 0) {
                varName = varNodes.item(0).getTextContent().trim();
                if (!varsMap.containsKey(varName)) {
                    varsMap.put(varName, new Var(varName));
                }
                var = varsMap.get(varName);
            }

            // label of the instruction
            Label selfLabel = FixedLabel.EMPTY;
            NodeList labelNodes = instrElem.getElementsByTagName("S-Label");
            if (labelNodes.getLength() > 0) {
                String labelName = labelNodes.item(0).getTextContent().trim();
                selfLabel = parseLabel(labelName);
            }

            // collect all arguments into a map
            Map<String, String> args = new HashMap<>();
            NodeList argParents = instrElem.getElementsByTagName("S-Instruction-Arguments");
            if (argParents.getLength() > 0) {
                Element argsElem = (Element) argParents.item(0);
                NodeList argNodes = argsElem.getElementsByTagName("S-Instruction-Argument");
                for (int j = 0; j < argNodes.getLength(); j++) {
                    Element argElem = (Element) argNodes.item(j);
                    String argName = argElem.getAttribute("name");
                    String argValue = argElem.getAttribute("value");
                    args.put(argName, argValue);
                }
            }

            // some instructions have a target label directly
            Label targetLabel = FixedLabel.EMPTY;
            if (args.containsKey("JNZLabel")) {
                targetLabel = parseLabel(args.get("JNZLabel"));
            }

            Instruction instr = createInstruction(instrName, var, selfLabel, targetLabel, args, varsMap);
            if (instr != null) {
                instructions.add(instr);
                if (selfLabel != FixedLabel.EMPTY) {
                    labels.put(selfLabel, instr);
                }
            } else {
                if (printMode) System.out.println("Unknown instruction name: " + instrName + " (type=" + type + ")");
            }
        }
        return instructions;
    }

    private Program parseNewFunction(Element funcElem) {
        String funcName = funcElem.getAttribute("name");
        String userStr = funcElem.getAttribute("user-string");

        return new SProgram(funcName, userStr);
    }

    private Boolean parseFunctionInstructions(Element funcElem, Map<String, Variable> varsMap, Map<Label, Instruction> labels, boolean printMode) {
        String funcName = funcElem.getAttribute("name");
        String userStr = funcElem.getAttribute("user-string");

        NodeList instrNodes = funcElem.getElementsByTagName("S-Instruction");
        List<Instruction> funcInstructions = parseInstructions(instrNodes, varsMap, labels, printMode);

        Program func = programs.stream().filter(x -> x.getName().equals(funcName)).findFirst().orElse(null);

        if (func == null) {
            if (printMode) System.out.println("Error: Function definition not found for " + funcName);
            return false;
        }
        func.setInstrList(funcInstructions);
        func.setLabelMap(labels);
        func.findVariables();

        if (!func.checkLabels()) {
            if (printMode) System.out.println("Error: Program has invalid labels.");
            return false;
        }
        else return true;
    }

    private Instruction createInstruction(String name,
                                                 Variable var,
                                                 Label selfLabel,
                                                 Label target,
                                                 Map<String, String> args,
                                                 Map<String, Variable> vars) {
        return switch (name.toUpperCase()) {
            case "INCREASE" -> new Increase(selfLabel, var);
            case "DECREASE" -> new Decrease(selfLabel, var);
            case "JUMP_NOT_ZERO", "JNZ" -> new JumpNotZero(selfLabel, var, target);
            case "NEUTRAL", "NO_OP" -> new Neutral(selfLabel, var);

            // ---- Synthetic ----
            case "ZERO_VARIABLE" -> new ZeroVariable(selfLabel, var);

            case "GOTO_LABEL" -> {
                String lbl = args.get("gotoLabel");
                Label tgt = parseLabel(lbl);
                yield new GoToLabel(selfLabel, tgt);
            }

            case "ASSIGNMENT" -> {
                // main variable from <S-Variable> is target (y)
                String assignedVarName = args.get("assignedVariable");
                Variable targetVar = var; // <S-Variable> → y
                Variable sourceVar = vars.computeIfAbsent(assignedVarName, Var::new);
                yield new Assignment(selfLabel, targetVar, sourceVar);
            }

            case "CONSTANT_ASSIGNMENT" -> {
                int k = Integer.parseInt(args.get("constantValue"));
                yield new ConstantAssignment(selfLabel, var, k);
            }

            case "JUMP_ZERO" -> {
                String lbl = args.get("JZLabel");
                Label tgt = parseLabel(lbl);
                yield new JumpZero(selfLabel, var, tgt);
            }

            case "JUMP_EQUAL_CONSTANT" -> {
                String lbl = args.get("JEConstantLabel");
                int k = Integer.parseInt(args.get("constantValue"));
                Label tgt = parseLabel(lbl);
                yield new JumpEqualConstant(selfLabel, var, k, tgt);
            }

            case "JUMP_EQUAL_VARIABLE" -> {
                String otherVarName = args.get("variableName");
                Variable otherVar = vars.computeIfAbsent(otherVarName, Var::new);
                String lbl = args.get("JEVariableLabel");
                Label tgt = parseLabel(lbl);
                yield new JumpEqualVariable(selfLabel, var, otherVar, tgt);
            }

            case "QUOTE" -> {
                String funcNameStr = args.get("functionName");
                String funcArgsStr = args.get("functionArguments");
                yield generateQuote(selfLabel, var, funcNameStr, funcArgsStr, vars);
            }

            case "JUMP_EQUAL_FUNCTION" -> {
                String lbl = args.get("JEFunctionLabel");
                String funcNameStr = args.get("functionName");
                String funcArgsStr = args.get("functionArguments");
                Variable tmp = generateTempVar(vars);
                Label tgt = parseLabel(lbl);
                QuoteArgument quoteArg = new QuoteArgument(generateQuote(FixedLabel.EMPTY, tmp, funcNameStr, funcArgsStr, vars));
                yield new JumpEqualFunction(selfLabel, quoteArg, var, tgt);
            }

            default -> null;
        };
    }

    private Quote generateQuote(Label selfLabel, Variable var, String funcNameStr, String funcArgsStr, Map<String, Variable> vars) {
        Program func = programs.stream().filter(p -> p.getName().equals(funcNameStr)).findFirst().orElse(null);

        if (func == null) {
            throw new IllegalArgumentException("Function not found: " + funcNameStr);
        } else {
            List<Argument> funcArgs = new ArrayList<>();
            if (funcArgsStr != null && !funcArgsStr.isEmpty()) {
                // split at top level commas
                List<String> tokens = splitTopLevel(funcArgsStr);
                for (String token : tokens) {
                    String trimmed = token.trim();
                    if (isVarName(trimmed)) {
                        // variable → VarArgument
                        Variable v = vars.computeIfAbsent(trimmed, Var::new);
                        funcArgs.add(new VarArgument(v));
                    } else if (trimmed.startsWith("(") && trimmed.endsWith(")")) {
                        // sub-function -> recursively build QuoteArgument
                        String inside = trimmed.substring(1, trimmed.length() - 1);
                        int firstComma = findTopLevelComma(inside);
                        String subFuncName;
                        String subFuncArgs;
                        if (firstComma == -1) {
                            subFuncName = inside;
                            subFuncArgs = "";
                        } else {
                            subFuncName = inside.substring(0, firstComma).trim();
                            subFuncArgs = inside.substring(firstComma + 1).trim();
                        }
                        Variable tmp = generateTempVar(vars);
                        Quote subQuote = generateQuote(FixedLabel.EMPTY, tmp, subFuncName, subFuncArgs, vars);
                        funcArgs.add(new QuoteArgument(subQuote));
                    } else {
                        throw new IllegalArgumentException("Unknown argument format: " + trimmed);
                    }
                }
            }
            return new Quote(selfLabel, var, func, funcArgs);
        }
    }

    private Variable generateTempVar(Map<String, Variable> vars) {
        int maxTmp = 0;
        for (Variable v : vars.values()) {
            if (v.getType().equals(VariableType.OUTPUT) && v.getNum() > maxTmp) {
                maxTmp = v.getNum();
            }
        }
        Variable v = new Var("z" + (maxTmp + 1));
        vars.put(v.getName(), v);
        return v;
    }

    // helper: split only on top-level commas
    private List<String> splitTopLevel(String s) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        StringBuilder cur = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == ',' && depth == 0) {
                result.add(cur.toString());
                cur.setLength(0);
            } else {
                if (c == '(') depth++;
                if (c == ')') depth--;
                cur.append(c);
            }
        }
        if (cur.length() > 0) result.add(cur.toString());
        return result;
    }

    // helper: find first comma at top level
    private int findTopLevelComma(String s) {
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ',' && depth == 0) return i;
            if (c == '(') depth++;
            if (c == ')') depth--;
        }
        return -1;
    }


    private boolean isVarName(String trimmedName) {
        if (trimmedName.isEmpty()) return false;
        char c0 = trimmedName.charAt(0);
        String numStr = trimmedName.substring(1);
        return (c0 == 'x' || c0 == 'y' || c0 == 'z') && numStr.chars().allMatch(Character::isDigit);
    }


    private static Label parseLabel(String labelValue) {
        if (labelValue == null || labelValue.isEmpty()) return FixedLabel.EMPTY;

        String v = labelValue.trim();
        Label target;
        char c0 = Character.toUpperCase(v.charAt(0));
        switch (c0) {
            case 'L' -> {
                int n = Integer.parseInt(v.substring(1));
                target = new NumericLabel(n);
            }
            case 'E' -> target = FixedLabel.EXIT;
            default -> target = FixedLabel.EMPTY;
        }
        return target;
    }
}
