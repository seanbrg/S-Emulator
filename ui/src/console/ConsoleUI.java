package console;

import execute.EngineImpl;
import logic.labels.Label;
import logic.variables.Variable;

import java.util.*;

public class ConsoleUI {
    private final EngineImpl engine = new EngineImpl();
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new ConsoleUI().mainMenu();
    }

    private void mainMenu() {
        while (true) {
            System.out.println("\n==== S-Emulator ====");
            System.out.println("1. Load Program from XML");
            System.out.println("2. Show Program");
            System.out.println("3. Expand Program");
            System.out.println("4. Run Program");
            System.out.println("5. Show History/Statistics");
            System.out.println("6. Exit");
            System.out.print("Choose option: ");

            String choice = scanner.nextLine().trim();



            switch (choice) {
                case "1" -> loadXml();
                case "2" -> engine.printProgram();
                case "3" -> expandProgram();
                case "4" -> runProgram();
                case "5" -> engine.printHistory();
                case "6" -> {
                    System.out.println("Exiting S-Emulator");
                    return;
                }
                default -> System.out.println("Invalid choice, please try again.");
            }
        }
    }

    private void loadXml() {
        System.out.print("Enter full XML file path: ");
        String path = scanner.nextLine();
        engine.loadFromXML(path);
    }


    private void expandProgram() {
        int degree = engine.maxDegree();
        System.out.printf("Program maximum degree is: %d.%n", degree);
        System.out.print("Please choose expansion degree (0.." + degree + "): ");

        int chosen;
        try {
            chosen = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice.");
            return;
        }

        if (chosen < 0 || chosen > degree) {
            System.out.println("Invalid degree, please try again.");
        } else {
            engine.printExpandProgram(chosen);
        }
    }


    private void runProgram() {
        int degree = engine.maxDegree();
        System.out.printf("Current Program maximum degree is: %d.%n", degree);
        System.out.printf("Please choose Program degree from 0 to %d:%n", degree);

        int inputDegree;
        try {
            inputDegree = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice, please try again.");
            return;
        }

        if (0 <= inputDegree && inputDegree <= degree) {
            List<Variable> inputVariables = engine.getInputs();
            System.out.println("The current program's input variables are:");
            inputVariables.stream().map(Variable::getName).forEach(s -> System.out.print(s + " "));
            System.out.println('\n' + "Please enter inputs separated by commas:");
            String input =  scanner.nextLine();
            List<Long> inputNumbers;
            if (!input.isEmpty()) {
                inputNumbers = Arrays.stream(input.split(","))
                        .map(String::trim)          // remove spaces
                        .map(Long::parseLong)     // convert to int
                        .toList();                  // Java 16+, else use Collectors.toList()

            }
            else inputNumbers = new ArrayList<>();

            int diff = inputNumbers.size() - inputVariables.size();
            if (diff < 0) {
                // inputNumbers is shorter -> pad with zeros
                inputNumbers.addAll(Collections.nCopies(-diff, 0L));
            } else if (diff > 0) {
                // inputNumbers is longer -> trim
                inputNumbers = inputNumbers.subList(0, inputVariables.size());
            }

            for (int i = 0; i < inputNumbers.size(); i++) {
                inputVariables.get(i).setValue(inputNumbers.get(i));
            }

            long result = engine.runProgram(inputDegree, inputNumbers);
            System.out.println("Program ran successfully:");

            if (inputDegree > 0) engine.printExpandProgram(inputDegree);
            else engine.printProgram();

            System.out.printf("Output: y = %d%n", result);
            System.out.println("Variables:");

            List<List<Variable>> varByType = engine.getVarByType();
            varByType.forEach(list ->
                    list.forEach(var ->
                            System.out.println(var.getName() + " = " + var.getValue())));

            engine.resetVars();

            System.out.printf("Cycles: %d", engine.getCycles(inputDegree));
        } else {
            System.out.println("Invalid choice, please try again.");
        }
    }
}
