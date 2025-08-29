package console;

import execute.EngineImpl;
import logic.variables.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
            System.out.println("Please enter inputs:");
            List<Long> inputs = new ArrayList<>();

            for (Variable var : engine.getInputs()) {
                System.out.print(var.getName() + ": ");
                long inputVal;
                try {
                    inputVal = Long.parseLong(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input, please try again.");
                    return;
                }
                if (inputVal < 0) {
                    System.out.println("Invalid input, please try again.");
                    return;
                }
                var.setValue(inputVal);
                inputs.add(inputVal);
            }

            long result = engine.runProgram(inputDegree, inputs);
            System.out.printf("Program exited with result y = %d%n", result);
        } else {
            System.out.println("Invalid choice, please try again.");
        }
    }
}
